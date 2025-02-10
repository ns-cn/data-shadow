package com.tangyujun.datashadow;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangyujun.datashadow.configuration.ConfigurationLoader;
import com.tangyujun.datashadow.module.ModuleLoader;
import com.tangyujun.datashadow.module.listener.DataComparatorListener;
import com.tangyujun.datashadow.module.listener.DataSourceListener;
import com.tangyujun.datashadow.module.listener.ResultExporterListener;
import com.tangyujun.datashadow.ui.MainLayout;

import java.io.IOException;

import com.tangyujun.datashadow.config.ConfigFactory;

import javafx.application.Platform;

/**
 * 数据影子应用程序启动类
 * 负责初始化JavaFX应用程序、加载插件模块、创建主窗口
 * 
 */
public class DataShadowLauncher extends Application {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(DataShadowLauncher.class);

    /** 窗口最小宽度 */
    private static final double MIN_WINDOW_WIDTH = 1000;
    /** 窗口最小高度 */
    private static final double MIN_WINDOW_HEIGHT = 868;
    /** 窗口默认宽度 */
    private static final double DEFAULT_WINDOW_WIDTH = MIN_WINDOW_WIDTH;
    /** 窗口默认高度 */
    private static final double DEFAULT_WINDOW_HEIGHT = MIN_WINDOW_HEIGHT;

    /**
     * JavaFX应用程序启动方法
     * 创建并显示主窗口,设置窗口属性和事件处理
     *
     * @param primaryStage 主舞台对象
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 创建主布局
        MainLayout mainLayout = new MainLayout();

        // 创建场景
        Scene scene = new Scene(mainLayout);

        // 设置窗口标题
        primaryStage.setTitle("Data Shadow - 数据影子对比工具");

        // 设置窗口最小尺寸
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);

        // 设置窗口初始尺寸
        primaryStage.setWidth(DEFAULT_WINDOW_WIDTH);
        primaryStage.setHeight(DEFAULT_WINDOW_HEIGHT);

        // 设置场景
        primaryStage.setScene(scene);

        // 设置程序图标
        Image icon = new Image(getClass().getResourceAsStream("/icon.png"));
        primaryStage.getIcons().add(icon);

        // 添加窗口关闭事件处理
        primaryStage.setOnCloseRequest(event -> {
            // 显示确认对话框
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认退出");
            alert.setHeaderText("确定要退出程序吗？");
            alert.setContentText("请确认是否要关闭数据影子对比工具。");

            // 等待用户选择,如果不是确认则取消关闭
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

    /**
     * 应用程序入口方法
     * 负责加载插件模块并启动JavaFX应用程序
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        try {
            // 加载配置
            ConfigurationLoader.load();

            // 初始化数据源和比较器监听器
            DataSourceListener loader = new DataSourceListener();
            DataComparatorListener comparatorLoader = new DataComparatorListener();
            ResultExporterListener resultExporterLoader = new ResultExporterListener();

            // 创建模块加载器并注册监听器
            ModuleLoader moduleLoader = new ModuleLoader();
            moduleLoader.registerListener(loader);
            moduleLoader.registerListener(comparatorLoader);
            moduleLoader.registerListener(resultExporterLoader);

            // 加载官方模块
            moduleLoader.loadOfficial("com.tangyujun.datashadow");

            // 从配置的插件目录加载自定义插件
            String pluginsPath = ConfigFactory.getInstance().getConfiguration().getPluginDir();
            log.info("Loading plugins from configured directory: {}", pluginsPath);
            moduleLoader.loadCustom(pluginsPath);

            // 启动JavaFX应用程序
            launch(args);
        } catch (IOException e) {
            // 记录插件加载错误
            log.error("Error loading custom plugins", e);

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