@echo off
chcp 65001 >nul
cd /d "%~dp0"

set VERSION=0.1.0-preview.1
set PKG_NAME=ModelRouter-%VERSION%-Windows

echo ========================================
echo  ModelRouter Preview - Package
echo ========================================
echo.

REM Step 1: Build frontend
echo [1/4] Building frontend...
cd frontend
call npm install
if errorlevel 1 (cd .. & echo Frontend build failed & pause & exit /b 1)
call npm run build
if errorlevel 1 (cd .. & echo Frontend build failed & pause & exit /b 1)
cd ..
copy /Y LEGAL.md LEGAL.en.md LEGAL.ja.md frontend\dist\ 2>nul

REM Step 2a: Build backend
echo Building backend...
cd backend
where mvn >nul 2>&1
if errorlevel 1 (
    call .\mvnw.cmd package -DskipTests -DskipFrontendCopy=false
) else (
    call mvn package -DskipTests -DskipFrontendCopy=false
)
if errorlevel 1 (cd .. & cd .. & echo Backend build failed & pause & exit /b 1)
cd ..

copy /Y backend\target\modelrouter-backend-0.1.0-preview.1.jar modelrouter.jar >nul

REM Step 2: Build Launcher EXE (optional)
if exist launcher\package.json (
  echo Building Launcher EXE...
  cd launcher
  call npm install 2>nul
  call npm run build 2>nul
  cd ..
)

REM Step 3: Assemble release
echo.
echo [2/3] Assembling release...
set RELEASE_DIR=dist\release\%PKG_NAME%
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%\data"
echo. > "%RELEASE_DIR%\data\.keep"

copy /Y modelrouter.jar "%RELEASE_DIR%\"
copy /Y start.bat "%RELEASE_DIR%\"
if exist launcher\dist\ModelRouterLauncher.exe copy /Y launcher\dist\ModelRouterLauncher.exe "%RELEASE_DIR%\"
xcopy /E /I /Y frontend\dist "%RELEASE_DIR%\frontend\dist" >nul

REM Write README
(
echo ModelRouter %VERSION% - Windows Preview
echo.
echo Usage
echo -----
echo 1. Install JDK 17+ and Node.js, add to PATH
echo 2. Double-click ModelRouterLauncher.exe or start.bat
echo 3. Launcher EXE: GUI with logs. start.bat: CLI, ports 20119/20118
echo 4. Data: data\modelrouter.db
echo.
echo UI:  http://localhost:20119
echo API: http://localhost:20118
echo.

) > "%RELEASE_DIR%\README.txt"

REM Step 4: Create ZIP
echo [3/3] Creating ZIP...
set ZIP_PATH=dist\%PKG_NAME%.zip
if exist "%ZIP_PATH%" del "%ZIP_PATH%"

powershell -NoProfile -Command "Compress-Archive -Path '%RELEASE_DIR%\*' -DestinationPath '%ZIP_PATH%' -Force"

if exist "%ZIP_PATH%" (
    echo.
    echo ========================================
    echo  Package Complete
    echo ========================================
    echo.
    echo  ZIP: %cd%\%ZIP_PATH%
    echo  Dir: %cd%\%RELEASE_DIR%\
    echo.
) else (
    echo [WARN] ZIP not created, but dir ready: %RELEASE_DIR%\
)

pause
