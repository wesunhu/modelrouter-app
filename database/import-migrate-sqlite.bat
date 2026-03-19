@echo off
chcp 65001 >nul
cd /d "%~dp0"
cd ..

echo ========================================
echo  Import register_url migration (SQLite)
echo ========================================
echo.

python database\import_register_url_sqlite.py
if errorlevel 1 (
    echo.
    pause
    exit /b 1
)

echo.
echo Done.
pause
