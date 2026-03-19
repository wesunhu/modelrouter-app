@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo  ModelRouter Windows Build
echo  SQLite + Frontend
echo ========================================
echo.

REM Step 1: Build frontend
echo [1/3] Building frontend...
cd frontend
call npm install
if errorlevel 1 (
    echo ERROR: npm install failed
    cd ..
    pause
    exit /b 1
)
call npm run build
if errorlevel 1 (
    echo ERROR: Frontend build failed
    cd ..
    pause
    exit /b 1
)
cd ..

REM Step 2: Build backend (prefer mvn if in PATH, else use mvnw wrapper)
echo [2/3] Building backend...
cd backend
where mvn >nul 2>&1
if errorlevel 1 (
    call .\mvnw.cmd package -DskipTests -DskipFrontendCopy=false
) else (
    call mvn package -DskipTests -DskipFrontendCopy=false
)
if errorlevel 1 (
    echo ERROR: Backend build failed
    echo.
    echo If you see "zip END header not found" or download errors, install Maven:
    echo   choco install maven
    echo   OR download from https://maven.apache.org/download.cgi
    echo Then run build-windows.bat again - it will use mvn directly.
    echo.
    cd ..
    pause
    exit /b 1
)
cd ..

REM Step 3: Copy jar
echo [3/3] Copying jar...
set "JAR=backend\target\modelrouter-backend-0.1.0-preview.1.jar"
if exist "%JAR%" (
    copy /Y "%JAR%" modelrouter.jar >nul
    echo Done: modelrouter.jar
) else (
    echo Jar not found. Check backend\target
)

echo.
echo ========================================
echo  Build Complete
echo ========================================
echo.
echo Run: java -jar modelrouter.jar
echo Or:  python launcher\launcher.py
echo.
pause
