@echo off
setlocal EnableDelayedExpansion
@REM Maven Wrapper - runs Maven without global install
@REM Requires JAVA_HOME (supports paths with spaces e.g. C:\Program Files\Java\...)

if not defined JAVA_HOME (
    echo Error: JAVA_HOME not set
    exit /b 1
)
REM Strip surrounding quotes from JAVA_HOME (fixes "C:\Program Files\..." parsing)
set "JAVA_HOME=%JAVA_HOME:"=%"

set "MAVEN_PROJECTBASEDIR=%~dp0"
REM Remove trailing backslash to avoid escaping the quote in -D param
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"
set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"

set "DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar"

if exist "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties" (
    for /f "usebackq tokens=1,2 delims==" %%a in ("%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties") do (
        if "%%a"=="wrapperUrl" set "DOWNLOAD_URL=%%b"
    )
)

if not exist "%WRAPPER_JAR%" (
    echo Downloading Maven Wrapper...
    set "DL_OK=0"
    set "WRAPPER_DIR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper"
    if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"
    pushd "%WRAPPER_DIR%"
    certutil -urlcache -split -f "%DOWNLOAD_URL%" maven-wrapper.jar >nul 2>&1
    if exist maven-wrapper.jar set "DL_OK=1"
    popd
    if "!DL_OK!"=="0" (
        echo Download failed. Save manually from:
        echo %DOWNLOAD_URL%
        echo to .mvn\wrapper\maven-wrapper.jar
        exit /b 1
    )
)

set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
REM Remove trailing backslash from MAVEN_PROJECTBASEDIR (trailing \ escapes the quote in -D=value)
set "MAVEN_DIR=%MAVEN_PROJECTBASEDIR%"
if "%MAVEN_DIR:~-1%"=="\" set "MAVEN_DIR=%MAVEN_DIR:~0,-1%"
REM Write launcher to temp file to avoid path/quote parsing issues
set "LAUNCHER=%TEMP%\mvnw-launcher-%RANDOM%.bat"
(
    echo @echo off
    echo "%JAVA_EXE%" -cp "%WRAPPER_JAR%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_DIR%" org.apache.maven.wrapper.MavenWrapperMain %*
) > "%LAUNCHER%"
call "%LAUNCHER%"
set "EXIT_CODE=!ERRORLEVEL!"
del /q "%LAUNCHER%" 2>nul
exit /b !EXIT_CODE!
