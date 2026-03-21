@echo off
chcp 65001 >nul
cd /d "%~dp0"

set FRONTEND_PORT=%1
set BACKEND_PORT=%2
if "%FRONTEND_PORT%"=="" set FRONTEND_PORT=20119
if "%BACKEND_PORT%"=="" set BACKEND_PORT=20118

echo ========================================
echo  ModelRouter
echo  Frontend: http://localhost:%FRONTEND_PORT%
echo  Backend:  http://localhost:%BACKEND_PORT%
echo ========================================
echo.

REM Find jar
set JAR=
if exist modelrouter.jar (set JAR=modelrouter.jar) else if exist modelrouter-backend.jar (set JAR=modelrouter-backend.jar) else if exist backend\target\modelrouter-backend-0.1.0-preview.1.jar (set JAR=backend\target\modelrouter-backend-0.1.0-preview.1.jar)

if "%JAR%"=="" (
  echo [ERROR] modelrouter.jar not found. Run build-windows.bat first.
  pause
  exit /b 1
)

REM Find frontend dist
set DIST=
if exist "frontend\dist\index.html" set DIST=frontend\dist
if "%DIST%"=="" if exist "backend\target\classes\static\index.html" set DIST=backend\target\classes\static

if "%DIST%"=="" (
  echo [ERROR] Frontend dist not found. Build frontend first.
  pause
  exit /b 1
)

REM Ensure data dir
if not exist data mkdir data

REM Start backend
echo [1/2] Starting backend on port %BACKEND_PORT%...
start "ModelRouter-Backend" cmd /k "java -jar %JAR% --spring.profiles.active=sqlite --server.port=%BACKEND_PORT% --modelrouter.serve-spa=false --spring.web.resources.add-mappings=false"

REM Wait for backend
timeout /t 3 /nobreak >nul

REM Start frontend
echo [2/2] Starting frontend on port %FRONTEND_PORT%...
where npx >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Node.js npx required. Please install Node.js
  pause
  exit /b 1
)
start "ModelRouter-Frontend" cmd /k "npx -y serve -s %DIST% -l %FRONTEND_PORT%"

echo.
echo ----------------------------------------
echo  UI:    http://localhost:%FRONTEND_PORT%
echo  API:   http://localhost:%BACKEND_PORT%
echo ----------------------------------------
echo Close backend/frontend windows to stop.
echo.
pause
