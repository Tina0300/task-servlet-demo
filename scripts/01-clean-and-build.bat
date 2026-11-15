@echo off
chcp 65001 >nul
setlocal
cd /d "%~dp0\.."

echo ==============================================
echo  TaskMaster SpringBoot modernization - build
echo ==============================================

echo.
echo [1/5] delete legacy mapper XML ...
if exist "src\main\resources\mapper\TaskMapper.xml" del /q "src\main\resources\mapper\TaskMapper.xml"
if exist "src\main\resources\mapper\UserMapper.xml" del /q "src\main\resources\mapper\UserMapper.xml"
if exist "src\main\resources\mapper" rmdir /q "src\main\resources\mapper" 2>nul

echo [2/5] delete legacy AuthFilter ...
if exist "src\main\java\cn\edu\mju\band\filter\AuthFilter.java" del /q "src\main\java\cn\edu\mju\band\filter\AuthFilter.java"
if exist "src\main\java\cn\edu\mju\band\filter" rmdir /q "src\main\java\cn\edu\mju\band\filter" 2>nul

echo [3/5] check maven ...
where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] mvn not found in PATH.
    echo         Run this script in IDEA Terminal, or add Maven bin to PATH.
    goto fail
)

echo [4/5] mvn clean package ...
call mvn -B clean package
if errorlevel 1 goto fail

echo [5/5] result ...
if exist "target\task-servlet-demo.jar" (
    echo [OK] target\task-servlet-demo.jar is ready.
    echo      start with: java -jar target\task-servlet-demo.jar
) else (
    echo [WARN] jar not found, check the build log above.
)

echo.
echo Done.
goto end

:fail
echo.
echo [FAILED] see the log above.

:end
endlocal
pause
