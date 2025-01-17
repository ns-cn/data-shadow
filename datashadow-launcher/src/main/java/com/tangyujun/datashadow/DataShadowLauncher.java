package com.tangyujun.datashadow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangyujun.datashadow.core.DataSourceLoader;
import com.tangyujun.datashadow.ui.MainLayout;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import javafx.application.Platform;

/**
 * 数据影子应用程序启动类
 */
public class DataShadowLauncher extends Application {

    private static final Logger log = LoggerFactory.getLogger(DataShadowLauncher.class);

    @Override
    public void start(Stage primaryStage) {
        // 创建主布局
        MainLayout mainLayout = new MainLayout();

        // 创建场景
        Scene scene = new Scene(mainLayout, 1024, 768);

        // 设置窗口标题
        primaryStage.setTitle("Data Shadow - 数据影子对比工具");

        // 设置场景
        primaryStage.setScene(scene);

        // 添加窗口关闭事件处理
        primaryStage.setOnCloseRequest(event -> {
            // 显示确认对话框
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("确定要退出程序吗？");
            alert.setContentText("请确认是否要关闭数据影子对比工具。");

            // 等待用户选择
            alert.showAndWait().ifPresent(response -> {
                if (response != ButtonType.OK) {
                    // 如果用户点击取消，则阻止窗口关闭
                    event.consume();
                }
            });
        });

        // 显示窗口
        primaryStage.show();
    }

    public static void main(String[] args) {
        try {
            // 加载数据源
            DataSourceLoader loader = new DataSourceLoader();
            loader.loadOfficial();
            // 临时读取resources/plugins目录
            URL pluginsUrl = DataShadowLauncher.class.getClassLoader().getResource("plugins");
            if (pluginsUrl == null) {
                log.error("Plugins directory not found in resources");
            } else {
                String pluginsPath = pluginsUrl.getFile();
                log.info("Loading plugins directory: {}", pluginsPath);
                // 确保路径正确解码（处理空格和特殊字符）
                pluginsPath = URLDecoder.decode(pluginsPath, StandardCharsets.UTF_8);
                loader.loadCustom(pluginsPath);
            }
            // 启动应用程序
            launch(args);
        } catch (IOException e) {
            log.error("Error loading custom data sources", e);
            // 显示错误对话框
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载错误");
                alert.setHeaderText("加载自定义数据源时发生错误");
                alert.setContentText("详细信息：" + e.getMessage());
                alert.showAndWait();
            });
            // 继续启动应用程序
            launch(args);
        }
    }
}