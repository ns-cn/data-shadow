package com.tangyujun.datashadow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.tangyujun.datashadow.core.DataSourceLoader;
import com.tangyujun.datashadow.ui.MainLayout;

/**
 * 数据影子应用程序启动类
 */
public class DataShadowLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 加载数据源
        DataSourceLoader loader = new DataSourceLoader();
        loader.load();
        // 创建主布局
        MainLayout mainLayout = new MainLayout();

        // 创建场景
        Scene scene = new Scene(mainLayout, 1024, 768);

        // 设置窗口标题
        primaryStage.setTitle("Data Shadow");

        // 设置场景
        primaryStage.setScene(scene);

        // 显示窗口
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}