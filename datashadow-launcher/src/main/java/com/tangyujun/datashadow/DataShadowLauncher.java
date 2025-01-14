package com.tangyujun.datashadow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 数据影子应用程序启动类
 */
public class DataShadowLauncher extends Application {

    /**
     * 程序入口
     * 
     * @param args 启动参数
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * JavaFX应用程序启动方法
     * 
     * @param primaryStage 主舞台
     */
    @Override
    public void start(Stage primaryStage) {
        // 创建根面板
        BorderPane root = new BorderPane();

        // 创建场景
        Scene scene = new Scene(root, 800, 600);

        // 设置窗口标题
        primaryStage.setTitle("Data Shadow");

        // 设置场景
        primaryStage.setScene(scene);

        // 显示窗口
        primaryStage.show();
    }
}