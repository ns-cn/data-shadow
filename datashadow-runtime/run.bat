@echo off
set PATH_TO_FX=javafx-sdk-21.0.5\lib
set PATH=%PATH_TO_FX%;%PATH%
runtime\bin\java -Dprism.verbose=true -Dprism.order=d3d,sw -Djavafx.verbose=true --module-path %PATH_TO_FX% --add-modules=javafx.controls,javafx.fxml,javafx.graphics --enable-preview -jar datashadow-launcher-1.0.0-SNAPSHOT.jar 