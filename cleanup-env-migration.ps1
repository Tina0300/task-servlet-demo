# ============================================================
# Env 1 wrap-up + Env 2: delete legacy configs and Tomcat leftovers, then rebuild
# Usage: powershell -ExecutionPolicy Bypass -File cleanup-env-migration.ps1
# ============================================================
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$files = @(
    'src\main\resources\mybatis-config.xml',
    'src\main\resources\db.properties',
    'target\classes\mybatis-config.xml',
    'target\classes\db.properties'
)
$dirs = @('conf', '.smarttomcat', 'work', 'webapps')

Write-Host '== 1/3 Delete legacy config files =='
foreach ($f in $files) {
    if (Test-Path $f) {
        Remove-Item $f -Force
        Write-Host "Deleted file: $f"
    } else {
        Write-Host "Not found, skip: $f"
    }
}

Write-Host ''
Write-Host '== 2/3 Delete external Tomcat leftover directories =='
foreach ($d in $dirs) {
    if (Test-Path $d) {
        Remove-Item $d -Recurse -Force
        Write-Host "Deleted dir: $d"
    } else {
        Write-Host "Not found, skip: $d"
    }
}

Write-Host ''
Write-Host '== Residual check (no RESIDUAL output means clean) =='
$leftover = @()
$leftover += Get-ChildItem -Path 'src' -Recurse -Filter 'mybatis-config.xml' -ErrorAction SilentlyContinue
$leftover += Get-ChildItem -Path 'src' -Recurse -Filter 'db.properties' -ErrorAction SilentlyContinue
if ($leftover.Count -eq 0) {
    Write-Host 'Check passed: no legacy config residual'
} else {
    $leftover | ForEach-Object { Write-Host ("RESIDUAL: " + $_.FullName) }
}

Write-Host ''
Write-Host '== 3/3 Rebuild: mvn clean package =='
mvn clean package
