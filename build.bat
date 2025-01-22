@echo off
setlocal enabledelayedexpansion
echo Start building Data Shadow Application...
echo Current directory: %CD%

:: 检查必要的目录和文件是否存在
echo Checking required directories and files...
if not exist datashadow-runtime (
    echo Error: datashadow-runtime directory not found at: %CD%\datashadow-runtime
    echo Please make sure you are running this script from the project root directory
    exit /b 1
)
echo datashadow-runtime directory found.

set JAVAFX_ZIP=datashadow-runtime\openjfx-21.0.5_windows-x64_bin-sdk.zip
echo Checking JavaFX SDK zip file: !JAVAFX_ZIP!
if not exist "!JAVAFX_ZIP!" (
    echo Error: JavaFX SDK zip file not found at: %CD%\!JAVAFX_ZIP!
    echo Please place openjfx-21.0.5_windows-x64_bin-sdk.zip in datashadow-runtime directory
    exit /b 1
)
echo JavaFX SDK zip file found.

:: 检查JAVA_HOME是否设置
echo Checking JAVA_HOME environment variable...
if "%JAVA_HOME%" == "" (
    echo Error: JAVA_HOME environment variable is not set.
    echo Please set JAVA_HOME to point to your JDK 21 or higher installation directory.
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-21
    exit /b 1
)
echo JAVA_HOME is set to: %JAVA_HOME%

:: 检查Java版本
echo Checking Java version...
for /f "tokens=* usebackq" %%i in (`"%JAVA_HOME%\bin\java.exe" -version 2^>^&1`) do (
    echo %%i
    set "JAVA_VERSION_LINE=%%i"
    if "!JAVA_VERSION_LINE:~0,11!"=="java version" (
        set "JAVA_VERSION=!JAVA_VERSION_LINE:~14,2!"
        if !JAVA_VERSION! LSS 21 (
            echo Error: Java version must be 21 or higher. Current version: !JAVA_VERSION!
            echo Please update your JAVA_HOME to point to JDK 21 or higher.
            exit /b 1
        )
    )
)
echo Java version check passed.

:: 检查并解压JavaFX SDK
set JAVAFX_SDK_DIR=datashadow-runtime\javafx-sdk-21.0.5
echo Checking JavaFX SDK directory: !JAVAFX_SDK_DIR!
if not exist "!JAVAFX_SDK_DIR!" (
    echo Extracting JavaFX SDK...
    echo Command: powershell.exe Expand-Archive...
    powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Write-Host 'Starting JavaFX SDK extraction...'; try { Expand-Archive -LiteralPath '!JAVAFX_ZIP!' -DestinationPath 'datashadow-runtime' -Force; if ($?) { Write-Host 'Extraction successful'; exit 0 } else { Write-Host 'Extraction failed'; exit 1 }} catch { Write-Host 'Exception during extraction:'; Write-Error $_.Exception.Message; exit 1 }"
    if !errorlevel! neq 0 (
        echo Error: Failed to extract JavaFX SDK
        echo PowerShell extraction failed with error level !errorlevel!
        exit /b 1
    )
)
echo JavaFX SDK directory check passed.

:: 创建dist目录
echo Setting up dist directory...
if exist dist (
    echo Cleaning up previous dist directory...
    rd /s /q "dist"
    if !errorlevel! neq 0 (
        echo Error: Failed to remove old dist directory
        echo Command failed with error level !errorlevel!
        exit /b 1
    )
)

echo Creating new dist directory...
mkdir "dist\datashadow"
if !errorlevel! neq 0 (
    echo Error: Failed to create dist directory
    echo Command failed with error level !errorlevel!
    exit /b 1
)
echo Dist directory created successfully.

:: 如果runtime目录存在，先删除它
echo Checking runtime directory...
if exist "datashadow-runtime\runtime" (
    echo Removing old runtime directory...
    rd /s /q "datashadow-runtime\runtime"
    if !errorlevel! neq 0 (
        echo Error: Failed to remove old runtime directory
        echo Command failed with error level !errorlevel!
        exit /b 1
    )
)

:: 使用jlink创建运行时
echo Creating runtime environment...
echo Command: jlink with modules...
"%JAVA_HOME%\bin\jlink.exe" --module-path "%JAVA_HOME%\jmods" --add-modules java.base,java.desktop,java.logging,java.xml,java.management,java.naming,java.sql,java.prefs,java.scripting,jdk.unsupported --no-header-files --no-man-pages --output "datashadow-runtime\runtime"
if !errorlevel! neq 0 (
    echo Error: Failed to create runtime environment
    echo jlink command failed with error level !errorlevel!
    exit /b 1
)
echo Runtime environment created successfully.

:: 执行Maven构建
echo Executing Maven build...
call mvn clean package
if !errorlevel! neq 0 (
    echo Error: Maven build failed
    echo Maven command failed with error level !errorlevel!
    exit /b 1
)
echo Maven build completed successfully.

:: 复制文件到dist目录
echo Copying files to dist directory...
echo Copying runtime files...
xcopy /E /I /Y "datashadow-runtime\runtime" "dist\datashadow\runtime"
if !errorlevel! neq 0 (
    echo Error: Failed to copy runtime files
    echo xcopy command failed with error level !errorlevel!
    exit /b 1
)

echo Copying JavaFX SDK files...
xcopy /E /I /Y "!JAVAFX_SDK_DIR!" "dist\datashadow\javafx-sdk-21.0.5"
if !errorlevel! neq 0 (
    echo Error: Failed to copy JavaFX SDK files
    echo xcopy command failed with error level !errorlevel!
    exit /b 1
)

echo Copying launcher jar...
copy /Y "datashadow-launcher\target\datashadow-launcher-1.0.0-SNAPSHOT.jar" "dist\datashadow\"
if !errorlevel! neq 0 (
    echo Error: Failed to copy launcher jar
    echo copy command failed with error level !errorlevel!
    exit /b 1
)

echo Copying run.bat...
copy /Y "datashadow-runtime\run.bat" "dist\datashadow\"
if !errorlevel! neq 0 (
    echo Error: Failed to copy run.bat
    echo copy command failed with error level !errorlevel!
    exit /b 1
)

:: 创建zip包
echo Creating zip package...
echo Command: PowerShell Compress-Archive...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Write-Host 'Starting zip creation...'; try { Compress-Archive -Path 'dist\datashadow' -DestinationPath 'dist\datashadow.zip' -Force; if ($?) { Write-Host 'Zip creation successful'; exit 0 } else { Write-Host 'Zip creation failed'; exit 1 }} catch { Write-Host 'Exception during zip creation:'; Write-Error $_.Exception.Message; exit 1 }"
if !errorlevel! neq 0 (
    echo Error: Failed to create zip package
    echo PowerShell compression failed with error level !errorlevel!
    exit /b 1
)

echo Build completed! Application has been packaged to dist\datashadow.zip
endlocal