@echo off
chcp 65001
setlocal enabledelayedexpansion
:: 开始构建Data Shadow应用程序
echo Start building Data Shadow Application...
echo Current directory: %CD%

:: 检查必要的目录是否存在
echo Checking required directories...
if not exist datashadow-script (
    echo Error: datashadow-script directory not found at: %CD%\datashadow-script
    echo Please make sure you are running this script from the project root directory
    exit /b 1
)
echo datashadow-script directory found.

:: 检查JAVA_HOME环境变量是否设置
echo Checking JAVA_HOME environment variable...
if "%JAVA_HOME%" == "" (
    echo Error: JAVA_HOME environment variable is not set.
    echo Please set JAVA_HOME to point to your JDK 21 or higher installation directory.
    echo Example: set JAVA_HOME=C:\Program Files\Java\jdk-21
    exit /b 1
)
echo JAVA_HOME is set to: %JAVA_HOME%

:: 检查JAVAFX_HOME环境变量是否设置
echo Checking JAVAFX_HOME environment variable...
if "%JAVAFX_HOME%" == "" (
    echo Error: JAVAFX_HOME environment variable is not set.
    echo Please set JAVAFX_HOME to point to your JavaFX SDK installation directory.
    echo Example: set JAVAFX_HOME=C:\Path\To\javafx-sdk-21.0.5
    exit /b 1
)
echo JAVAFX_HOME is set to: %JAVAFX_HOME%

:: 检查Java版本是否满足要求(需要Java 21或更高版本)
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

:: 创建并清理dist目录(如果已存在)
echo Setting up dist directory...
if exist dist (
    echo Cleaning up previous dist directory...
    rd /s /q "dist"
    if !errorlevel! neq 0 (
        echo Error: Failed to remove old dist directory
        exit /b 1
    )
)

:: 创建新的dist目录
echo Creating new dist directory...
mkdir "dist\datashadow"
if !errorlevel! neq 0 (
    echo Error: Failed to create dist directory
    exit /b 1
)
echo Dist directory created successfully.

:: 使用jlink创建自定义运行时环境，仅包含必要的模块
echo Creating runtime environment...
"%JAVA_HOME%\bin\jlink.exe" --module-path "%JAVA_HOME%\jmods" --add-modules java.base,java.desktop,java.logging,java.xml,java.management,java.naming,java.sql,java.prefs,java.scripting,jdk.unsupported --no-header-files --no-man-pages --output "dist\datashadow\runtime"
if !errorlevel! neq 0 (
    echo Error: Failed to create runtime environment
    exit /b 1
)
echo Runtime environment created successfully.

:: 执行Maven构建，清理并打包项目
echo Executing Maven build...
call mvn clean package -DskipTests
if !errorlevel! neq 0 (
    echo Error: Maven build failed
    exit /b 1
)
echo Maven build completed successfully.

:: 复制必要文件到dist目录
echo Copying files to dist directory...
:: 复制JavaFX SDK文件
echo Copying JavaFX SDK files...
xcopy /E /I /Y "%JAVAFX_HOME%" "dist\datashadow\javafx_sdk"
if !errorlevel! neq 0 (
    echo Error: Failed to copy JavaFX SDK files
    exit /b 1
)
:: 特殊的，将javafx中无用的删掉
del "dist\datashadow\javafx_sdk\bin\jfxwebkit.dll"
rd /s /q "dist\datashadow\javafx_sdk\legal"
del "dist\datashadow\javafx_sdk\src.zip"

:: 复制启动器jar包
echo Copying launcher jar...
copy /Y "datashadow-launcher\target\datashadow-launcher-1.0.0-SNAPSHOT.jar" "dist\datashadow\"
if !errorlevel! neq 0 (
    echo Error: Failed to copy launcher jar
    exit /b 1
)

:: 复制运行脚本
echo Copying run.bat and 启动.vbs...
copy /Y "datashadow-script\run.bat" "dist\datashadow\"
if !errorlevel! neq 0 (
    echo Error: Failed to copy run.bat
    exit /b 1
)
copy /Y "datashadow-script\启动.vbs" "dist\datashadow\"
if !errorlevel! neq 0 (
    echo Error: Failed to copy 启动.vbs
    exit /b 1
)

:: 创建最终的zip发布包
echo Creating zip package...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "Write-Host 'Starting zip creation...'; try { Compress-Archive -Path 'dist\datashadow' -DestinationPath 'dist\datashadow.zip' -Force; if ($?) { Write-Host 'Zip creation successful'; exit 0 } else { Write-Host 'Zip creation failed'; exit 1 }} catch { Write-Host 'Exception during zip creation:'; Write-Error $_.Exception.Message; exit 1 }"
if !errorlevel! neq 0 (
    echo Error: Failed to create zip package
    exit /b 1
)

:: 构建完成提示
echo Build completed! Application has been packaged to dist\datashadow.zip
endlocal