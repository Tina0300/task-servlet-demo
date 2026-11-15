# ============================================================
# task-servlet-demo 迁移收尾脚本
# 用途：删除 SSM(JSP) 遗留文件并重新构建 SpringBoot 可执行 jar
# 运行方式（在项目根目录下执行）：
#   powershell -ExecutionPolicy Bypass -File cleanup-and-build.ps1
# 或直接右键"使用 PowerShell 运行"
# ============================================================
$ErrorActionPreference = "Stop"
$root = "D:\code_java\task-servlet-demo\task-servlet-demo"

Write-Host "== 1/3 删除遗留文件 ==" -ForegroundColor Cyan

$dirs = @(
    "$root\src\main\webapp",
    "$root\src\main\java\cn\edu\mju\band\filters"
)
foreach ($d in $dirs) {
    if (Test-Path $d) { Remove-Item -Recurse -Force $d; Write-Host "已删除目录: $d" }
}

$files = @(
    "$root\src\main\java\cn\TaskApplication.java",
    "$root\src\main\java\cn\edu\mju\band\dal\ITaskDal.java",
    "$root\src\main\java\cn\edu\mju\band\dal\impl\TaskDal.java",
    "$root\src\main\java\cn\edu\mju\band\dal\mapper\TaskMapper.java",
    "$root\src\main\java\cn\edu\mju\band\buss\ITaskBuss.java",
    "$root\src\main\java\cn\edu\mju\band\buss\impl\TaskBuss.java",
    "$root\src\main\java\cn\edu\mju\band\buss\IUserBuss.java",
    "$root\src\main\java\cn\edu\mju\band\buss\impl\UserBuss.java",
    "$root\src\main\resources\applicationContext.xml",
    "$root\src\main\resources\spring-mvc.xml",
    "$root\src\main\resources\jdbc.properties"
)
foreach ($f in $files) {
    if (Test-Path $f) { Remove-Item -Force $f; Write-Host "已删除文件: $f" }
}

# 清理可能的空目录
foreach ($d in @("$root\src\main\java\cn\edu\mju\band\dal\impl",
                 "$root\src\main\java\cn\edu\mju\band\dal\mapper",
                 "$root\src\main\java\cn")) {
    if ((Test-Path $d) -and -not (Get-ChildItem $d -Force | Select-Object -First 1)) {
        Remove-Item -Force $d; Write-Host "已删除空目录: $d"
    }
}

Write-Host "== 2/3 源码残留检查（应无输出） ==" -ForegroundColor Cyan
Get-ChildItem -Path "$root\src" -Recurse -Include *.java,*.html,*.xml,*.yml,*.properties |
    Select-String -Pattern "javax\.servlet|WEB-INF|\.jsp|jstl|c:forEach" |
    Select-Object Path, LineNumber, Line

Write-Host "== 3/3 构建可执行 jar ==" -ForegroundColor Cyan
Push-Location $root
try {
    mvn clean package
    if (Test-Path "$root\target\task-servlet-demo.jar") {
        Write-Host "构建成功: $root\target\task-servlet-demo.jar" -ForegroundColor Green
        Write-Host "启动命令: java -jar target\task-servlet-demo.jar"
    } else {
        Write-Host "构建产物未找到，请检查上方 Maven 输出" -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}
