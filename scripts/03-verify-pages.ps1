# 页面验收脚本（curl 统一封装版）：结果写入 verify.log
# 用法：应用启动后执行
#   powershell -ExecutionPolicy Bypass -File scripts\03-verify-pages.ps1
# 每条请求都记录：HTTP 状态码、Location、curl 退出码、是否产出了响应头文件

$ErrorActionPreference = "Continue"
$base    = "http://127.0.0.1:8080"
$root    = Split-Path -Parent $PSScriptRoot
$logPath = Join-Path $root "verify.log"
$work    = Join-Path $root "verify-tmp"
New-Item -ItemType Directory -Force -Path $work | Out-Null
$cookie  = Join-Path $work "cookie.txt"
$errFile = Join-Path $work "curl-err.txt"
Remove-Item $errFile -ErrorAction SilentlyContinue
$results = New-Object System.Collections.Generic.List[string]
$script:lastExit = 0

function Add-Result {
    param($name, $ok, $detail)
    if ($ok) { $results.Add("$name : PASS : $detail") }
    else     { $results.Add("$name : FAIL : $detail") }
}

function Call-Curl {
    param($tag, $url, $method = "GET", $body = $null, $withCookie = $true, $saveCookie = $false)
    $h = Join-Path $work "$tag-h.txt"
    $b = Join-Path $work "$tag-b.html"
    Remove-Item $h -ErrorAction SilentlyContinue
    Remove-Item $b -ErrorAction SilentlyContinue
    $p = @("-s", "-D", $h, "-o", $b)
    if ($withCookie) { $p += @("-b", $cookie) }
    if ($saveCookie) { $p += @("-c", $cookie) }
    if ($method -eq "POST") { $p += @("-d", $body) }
    $p += $url
    curl.exe @p 2>> $errFile
    $script:lastExit = $LASTEXITCODE

    $code = -1; $loc = ""
    if (Test-Path $h) {
        $hh = Get-Content -Path $h -Raw -ErrorAction SilentlyContinue
        if ($hh -match "HTTP/1\.[01]\s+(\d{3})") { $code = [int]$Matches[1] }
        if ($hh -match "(?im)^Location:\s*(\S.*)$") { $loc = $Matches[1].Trim() }
    }
    $bodyText = ""
    if (Test-Path $b) { $bodyText = [string](Get-Content -Path $b -Raw -Encoding UTF8 -ErrorAction SilentlyContinue) }

    return @{
        Code      = $code
        Location  = $loc
        Body      = $bodyText
        Exit      = $script:lastExit
        HeaderOk  = (Test-Path $h)
    }
}

function Detail($r) {
    return "code=$($r.Code) exit=$($r.Exit) header=$($r.HeaderOk) location=$($r.Location)"
}

# 0. 端口检查：读 TCP 连接表，不新建 socket
$listenPid = -1
try {
    $tcp = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction Stop
    if ($null -ne $tcp) { $listenPid = [int]$tcp[0].OwningProcess }
} catch { $listenPid = -1 }
Add-Result "INFO port-8080" ($listenPid -gt 0) "listenPid=$listenPid"
if ($listenPid -le 0) {
    $results | Out-File -FilePath $logPath -Encoding utf8
    Write-Host "port 8080 not listening, start the jar first."
    exit 1
}

# 1. 登录页能正常渲染（未登录访问 /login）
$r0 = Call-Curl "p0" "$base/login" "GET" $null $false $false
Add-Result "P0 login-page" ($r0.Code -eq 200) (Detail $r0)

# 2. 未登录访问 /task：应 302 到 /login
$r1 = Call-Curl "d1" "$base/task" "GET" $null $false $false
Add-Result "D1 no-login-task" ($r1.Code -eq 302 -and $r1.Location -like "*login*") (Detail $r1)

# 3. 登录 admin / 123456：应 302 到 /index，并落 cookie
$r2 = Call-Curl "d2" "$base/doLogin" "POST" "username=admin&password=123456" $true $true
Add-Result "LOGIN admin" ($r2.Code -eq 302 -and $r2.Location -like "*index*") (Detail $r2)

# 4. 登录后列表：应 200，含检索表单（name="keyword" / name="size"）
$r3 = Call-Curl "d3" "$base/task?current=1&size=5" "GET" $null $true $false
$hasFilter = $r3.Body.Contains('name="keyword"')
$hasPager  = $r3.Body.Contains('class="pager"') -or $r3.Body.Contains('name="size"')
Add-Result "D2 task-list" ($r3.Code -eq 200 -and $hasFilter -and $hasPager) ((Detail $r3) + " filter=$hasFilter pager=$hasPager")

# 5. 新增表单页能正常渲染
$r6 = Call-Curl "d6" "$base/task/add" "GET" $null $true $false
Add-Result "D6 add-form" ($r6.Code -eq 200) (Detail $r6)

# 6. 新增时标题留空：应 200 停在表单页，且输出字段级错误
$r4 = Call-Curl "d4" "$base/task/save" "POST" "opt=add&title=%20&content=&priority=2&status=0" $true $false
$hasFieldError = $r4.Body.Contains('field-error')
Add-Result "D5 blank-title" ($r4.Code -eq 200 -and $hasFieldError) ((Detail $r4) + " fieldError=$hasFieldError")

# 7. 关键字检索：应 200
$r5 = Call-Curl "d5" "$base/task?keyword=zzz_NX_404&current=1&size=5" "GET" $null $true $false
Add-Result "D3 keyword-search" ($r5.Code -eq 200) (Detail $r5)

# 8. 退出后再访问 /task：应再次 302 到 /login
$r7 = Call-Curl "d7" "$base/logout" "GET" $null $true $false
$r8 = Call-Curl "d8" "$base/task" "GET" $null $true $false
Add-Result "D9 logout-then-blocked" ($r8.Code -eq 302 -and $r8.Location -like "*login*") (Detail $r8)

$results | Out-File -FilePath $logPath -Encoding utf8
Write-Host "verify.log written: $logPath"
$results | ForEach-Object { Write-Host $_ }
