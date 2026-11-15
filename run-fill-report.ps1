# Install python-docx if needed, then fill the report.
# Usage: powershell -ExecutionPolicy Bypass -File run-fill-report.ps1
$ErrorActionPreference = 'Stop'

$py = 'C:\Users\17\AppData\Local\Programs\Python\Python313\python.exe'

Write-Host '== 1/2 Ensure python-docx is installed =='
& $py -m pip install --disable-pip-version-check python-docx

Write-Host ''
Write-Host '== 2/2 Run fill_report.py =='
& $py 'D:\code_java\task-servlet-demo\task-servlet-demo\fill_report.py'
