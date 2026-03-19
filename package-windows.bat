@echo off
chcp 65001 >nul
cd /d "%~dp0"

set VERSION=0.1.0-preview.1
set PKG_NAME=ModelRouter-%VERSION%-Windows

echo ========================================
echo  ModelRouter 预览版 - 打包安装包
echo ========================================
echo.

REM Step 1: 构建前端
echo [1/4] 构建前端...
cd frontend
call npm install
if errorlevel 1 (cd .. & echo 前端构建失败 & pause & exit /b 1)
call npm run build
if errorlevel 1 (cd .. & echo 前端构建失败 & pause & exit /b 1)
cd ..

REM Step 2a: 构建后端
echo 构建后端...
cd backend
where mvn >nul 2>&1
if errorlevel 1 (
    call .\mvnw.cmd package -DskipTests -DskipFrontendCopy=false
) else (
    call mvn package -DskipTests -DskipFrontendCopy=false
)
if errorlevel 1 (cd .. & cd .. & echo 后端构建失败 & pause & exit /b 1)
cd ..

copy /Y backend\target\modelrouter-backend-0.1.0-preview.1.jar modelrouter.jar >nul

REM Step 2: 构建启动器 EXE
echo.
echo [2/4] 构建 ModelRouterLauncher.exe...
cd launcher
pip install pyinstaller -q 2>nul
pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py
if not exist dist\ModelRouterLauncher.exe (
    echo [失败] EXE 构建未成功
    cd ..
    pause
    exit /b 1
)
cd ..

REM Step 3: 组装发布目录
echo.
echo [3/4] 组装发布包...
set RELEASE_DIR=dist\release\%PKG_NAME%
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%"
mkdir "%RELEASE_DIR%\data"
echo. > "%RELEASE_DIR%\data\.keep"

copy /Y modelrouter.jar "%RELEASE_DIR%\"
copy /Y launcher\dist\ModelRouterLauncher.exe "%RELEASE_DIR%\"

REM 写入使用说明
(
echo ModelRouter %VERSION% - Windows 预览版
echo.
echo 使用说明
echo ---------
echo 1. 确保已安装 JDK 17+ 并设置 JAVA_HOME 或 PATH
echo 2. 双击 ModelRouterLauncher.exe 启动
echo 3. 首次启动约 5 秒后会自动打开管理界面
echo 4. 数据保存在 data\modelrouter.db
echo.
echo 管理界面: http://localhost:20118
echo.

) > "%RELEASE_DIR%\使用说明.txt"

REM Step 4: 打包 ZIP
echo [4/4] 生成 ZIP...
set ZIP_PATH=dist\%PKG_NAME%.zip
if exist "%ZIP_PATH%" del "%ZIP_PATH%"

powershell -NoProfile -Command "Compress-Archive -Path '%RELEASE_DIR%\*' -DestinationPath '%ZIP_PATH%' -Force"

if exist "%ZIP_PATH%" (
    echo.
    echo ========================================
    echo  打包完成
    echo ========================================
    echo.
    echo  安装包: %cd%\%ZIP_PATH%
    echo  目录版: %cd%\%RELEASE_DIR%\
    echo.
) else (
    echo [警告] ZIP 未生成，但目录已就绪: %RELEASE_DIR%\
)

pause
