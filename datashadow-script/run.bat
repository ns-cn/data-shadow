@echo off
chcp 65001 > nul

:: 设置JavaFX路径
set PATH_TO_FX=javafx_sdk\lib
set PATH=%PATH_TO_FX%;%PATH%

:: 设置JVM参数
set JAVA_OPTS=-Xms512m -Xmx2048m -XX:MaxMetaspaceSize=512m -XX:CompressedClassSpaceSize=256m -XX:ReservedCodeCacheSize=256m -XX:+UseG1GC -XX:G1HeapRegionSize=4m

:: 启动应用
runtime\bin\javaw %JAVA_OPTS% ^
-Dprism.verbose=true ^
-Dprism.order=d3d,sw ^
-Djavafx.verbose=true ^
--module-path %PATH_TO_FX% ^
--add-modules=javafx.controls,javafx.fxml,javafx.graphics ^
--enable-preview ^
-jar datashadow-launcher-1.0.0-SNAPSHOT.jar

exit 