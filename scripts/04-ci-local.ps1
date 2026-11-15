# 环节一：本地复刻 CI 的三个门禁，推送前自证，避免「提交即红」
# 用法（在任意目录均可执行，脚本会自己定位项目根）：
#   powershell -ExecutionPolicy Bypass -File scripts\04-ci-local.ps1
# 说明：控制台输出刻意使用英文，避免 PowerShell 5.1 对 UTF-8 无 BOM 脚本的编码误读。

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root

function Invoke-Gate {
    param([string]$Label, [string[]]$MvnArgs)
    Write-Host ""
    Write-Host "== $Label ==" -ForegroundColor Cyan
    & mvn @MvnArgs
    if ($LASTEXITCODE -ne 0) {
        Write-Host "FAIL: $Label (exit=$LASTEXITCODE) - pipeline stopped here" -ForegroundColor Red
        exit $LASTEXITCODE
    }
    Write-Host "PASS: $Label" -ForegroundColor Green
}

Write-Host "project root: $root"
Write-Host "java/maven:"
java -version 2>&1 | ForEach-Object { Write-Host "  $_" }
mvn -v 2>&1 | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" }

Invoke-Gate "[1/3] quality gate  : mvn clean test"            @("-B", "-ntp", "clean", "test")
Invoke-Gate "[2/3] coverage gate : jacoco:report + check"      @("-B", "-ntp", "jacoco:report", "jacoco:check")
Invoke-Gate "[3/3] package       : mvn package -DskipTests"    @("-B", "-ntp", "package", "-DskipTests")

$jar = Join-Path $root "target\task-servlet-demo.jar"
if (Test-Path $jar) {
    $mb = [math]::Round((Get-Item $jar).Length / 1MB, 2)
    Write-Host ""
    Write-Host "ALL GREEN. artifact: $jar ($mb MB)" -ForegroundColor Green
    exit 0
} else {
    Write-Host "artifact NOT found: target\task-servlet-demo.jar - check build/finalName" -ForegroundColor Yellow
    exit 1
}
