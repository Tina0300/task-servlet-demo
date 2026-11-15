# 页面验收脚本：把环节五、环节四的关键结果写进 verify.log
# 用法：先启动应用，再执行
#   powershell -ExecutionPolicy Bypass -File scripts\02-verify-pages.ps1
# 脚本只用 ASCII 判断依据（class="pager" / field-error / Location），避免中文编码干扰

$ErrorActionPreference = "Continue"
$base = "http://127.0.0.1:8080"
$logPath = Join-Path $PSScriptRoot "..\verify.log"
$results = New-Object System.Collections.Generic.List[string]

function Add-Result {
    param($name, $ok, $detail)
    if ($ok) { $results.Add("$name : PASS : $detail") }
    else     { $results.Add("$name : FAIL : $detail") }
}

function Invoke-NoRedirect {
    param($uri, $session, $method = "GET", $body = $null)
    try {
        $p = @{
            Uri = $uri
            Method = $method
            MaximumRedirection = 0
            WebSession = $session
            UseBasicParsing = $true
        }
        if ($null -ne $body) {
            $p["Body"] = $body
            $p["ContentType"] = "application/x-www-form-urlencoded"
        }
        $r = Invoke-WebRequest @p
        return @{ Code = [int]$r.StatusCode; Body = [string]$r.Content; Location = "" }
    } catch {
        $resp = $_.Exception.Response
        if ($null -ne $resp) {
            $code = -1; $loc = ""
            try { $code = [int]$resp.StatusCode } catch { }
            try { $loc = [string]$resp.Headers["Location"] } catch { }
            return @{ Code = $code; Body = ""; Location = $loc }
        }
        return @{ Code = -1; Body = [string]$_.Exception.Message; Location = "" }
    }
}

# 0. 端口存活检查：读系统 TCP 连接表，不新建 socket
#    （不用 Test-NetConnection：它要临时创建客户端 socket，
#      在本机套接字资源紧张时会假报不通，实测已出现过误判）
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

# 1. 未登录访问 /task，应被 LoginInterceptor 重定向到 /login
$s0 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$r1 = Invoke-NoRedirect "$base/task" $s0
Add-Result "D1 no-login-task" ($r1.Code -eq 302 -and $r1.Location -like "*login*") "code=$($r1.Code) location=$($r1.Location)"

# 2. 登录（admin / 123456）
$s1 = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$r2 = Invoke-NoRedirect "$base/doLogin" $s1 "POST" "username=admin&password=123456"
Add-Result "LOGIN admin" ($r2.Code -eq 302 -and $r2.Location -like "*index*") "code=$($r2.Code) location=$($r2.Location)"

# 3. 登录后访问列表，应出现分页栏与检索表单
$r3 = Invoke-NoRedirect "$base/task?current=1&size=5" $s1
$hasPager = $r3.Body -like '*class="pager"*' -or $r3.Body -like '*name="size"*'
$hasFilter = $r3.Body -like '*name="keyword"*'
Add-Result "D2 task-list" ($r3.Code -eq 200 -and $hasPager -and $hasFilter) "code=$($r3.Code) pager=$hasPager filter=$hasFilter"

# 4. 新增时标题留空，应停留在表单页并输出字段级错误
$r4 = Invoke-NoRedirect "$base/task/save" $s1 "POST" "opt=add&title=%20&content=&priority=2&status=0"
$hasFieldError = $r4.Body -like '*field-error*'
Add-Result "D5 blank-title" ($r4.Code -eq 200 -and $hasFieldError) "code=$($r4.Code) fieldError=$hasFieldError"

# 5. 关键字检索，应返回 200 且页面仍是列表结构
$r5 = Invoke-NoRedirect "$base/task?keyword=zzz_not_exists&current=1&size=5" $s1
Add-Result "D3 keyword-search" ($r5.Code -eq 200) "code=$($r5.Code)"

# 6. 退出后访问 /task，应再次被拦回登录页
$r6 = Invoke-NoRedirect "$base/logout" $s1
$r7 = Invoke-NoRedirect "$base/task" $s1
Add-Result "D9 logout-then-blocked" ($r7.Code -eq 302 -and $r7.Location -like "*login*") "code=$($r7.Code) location=$($r7.Location)"

$results | Out-File -FilePath $logPath -Encoding utf8
Write-Host "verify.log written: $logPath"
$results | ForEach-Object { Write-Host $_ }
