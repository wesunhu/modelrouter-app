@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo ========================================
echo  ModelRouter 启动器 - 构建 EXE
echo ========================================
echo.

REM 检查 Python
python --version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Python，请先安装 Python 3.8+
    pause
    exit /b 1
)

REM 安装依赖
echo [1/2] 安装 PyInstaller...
pip install pyinstaller -q

REM 构建
echo [2/2] 构建 ModelRouterLauncher.exe...
pyinstaller --onefile --windowed --name ModelRouterLauncher launcher.py

if exist dist\ModelRouterLauncher.exe (
    echo.
    echo [完成] EXE 已生成: %cd%\dist\ModelRouterLauncher.exe
    echo.
    echo 建议：将 ModelRouterLauncher.exe 复制到项目根目录（与 modelrouter.jar 同级）运行
    echo.
) else (
    echo [失败] 构建未成功
)

pause
