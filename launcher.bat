@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist "modelrouter-launcher.jar" goto no_jar
java -jar modelrouter-launcher.jar %*
exit /b %ERRORLEVEL%

:no_jar
echo [ERROR] modelrouter-launcher.jar not found. Run build-launcher.bat first.
pause
exit /b 1
