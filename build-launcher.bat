@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo  ModelRouter Java launcher build
echo ========================================
echo.

cd launcher-java
where mvn >nul 2>&1
if errorlevel 1 goto use_mvnw
call mvn -q package
goto maven_done
:use_mvnw
call ..\backend\mvnw.cmd -q package
:maven_done
if errorlevel 1 goto mvn_fail

set "OUT=target\modelrouter-launcher-1.0.1.jar"
if not exist "%OUT%" goto jar_missing
copy /Y "%OUT%" ..\modelrouter-launcher.jar >nul
cd ..
echo Done: modelrouter-launcher.jar
echo.
echo Run: java -jar modelrouter-launcher.jar
echo.
pause
exit /b 0

:jar_missing
cd ..
echo ERROR: Output jar not found: launcher-java\%OUT%
pause
exit /b 1

:mvn_fail
cd ..
echo ERROR: Maven build failed. If you see a .m2 lock error, close other Maven and retry.
pause
exit /b 1
