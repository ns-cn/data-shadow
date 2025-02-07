package com.tangyujun.datashadow.ui.menu.dialog;

import com.tangyujun.datashadow.configuration.ConfigurationLoader;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.tangyujun.datashadow.config.ConfigFactory;

/**
 * 插件配置对话框
 * 用于配置和管理插件目录,提供以下功能:
 * 1. 查看和修改当前插件目录路径
 * 2. 通过文件选择器浏览并选择新的插件目录
 * 3. 在系统资源管理器中打开插件目录
 * 4. 重置为默认插件目录
 * 5. 保存插件目录配置
 */
public class PluginManagerDialog extends Dialog<String> {
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(PluginManagerDialog.class);

    /** 插件目录输入框 */
    private final TextField pluginDirField;

    /** 对话框所属窗口 */
    private final Window owner;

    /**
     * 默认插件目录路径
     * 位于用户目录/.datashadow/plugins下
     * 例如Windows系统下为: C:/Users/username/.datashadow/plugins
     */
    private static final String DEFAULT_PLUGIN_DIR = System.getProperty("user.home")
            + File.separator + ".datashadow"
            + File.separator + "plugins";

    /**
     * 构造函数
     * 初始化插件管理对话框,包括:
     * 1. 初始化插件目录配置
     * 2. 创建对话框UI组件
     * 3. 设置按钮事件处理
     * 4. 配置对话框结果转换器
     *
     * @param owner 对话框所属的窗口
     */
    public PluginManagerDialog(Window owner) {
        this.owner = owner;

        // 确保当前插件目录不为空
        String currentPluginDir = ConfigFactory.getInstance().getConfiguration().getPluginDir();
        if (currentPluginDir == null || currentPluginDir.trim().isEmpty()) {
            currentPluginDir = DEFAULT_PLUGIN_DIR;
            ConfigFactory.getInstance().getConfiguration().setPluginDir(currentPluginDir);
        }

        // 设置对话框标题和头部文本
        setTitle("插件配置");
        setHeaderText("配置插件加载目录");

        // 创建对话框内容
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        // 创建目录输入框
        pluginDirField = new TextField();
        pluginDirField.setPrefWidth(400);
        pluginDirField.setText(currentPluginDir);

        // 创建按钮容器
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        // 创建浏览按钮
        Button browseButton = new Button("浏览...");
        browseButton.setOnAction(e -> browseDirectory());

        // 创建打开文件夹按钮
        Button openFolderButton = new Button("打开文件夹");
        openFolderButton.setOnAction(e -> openPluginFolder());

        // 创建重置按钮
        Button resetButton = new Button("重置为默认");
        resetButton.setOnAction(e -> resetToDefault());

        buttonBox.getChildren().addAll(browseButton, openFolderButton, resetButton);

        // 添加控件到网格
        grid.add(new Label("插件目录:"), 0, 0);
        grid.add(pluginDirField, 1, 0);
        grid.add(buttonBox, 1, 1);

        // 添加提示信息
        Label tipLabel = new Label("提示：更改插件目录后需要重启应用程序才能生效");
        tipLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");
        grid.add(tipLabel, 1, 2);

        // 设置对话框内容
        getDialogPane().setContent(grid);

        // 添加确定和取消按钮
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // 设置结果转换器
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                return pluginDirField.getText();
            }
            return null;
        });
    }

    /**
     * 打开目录选择器
     * 允许用户通过图形界面选择插件目录
     * 如果选择了新目录,会更新输入框中的路径
     */
    private void browseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择插件目录");

        // 设置初始目录
        String currentDir = pluginDirField.getText();
        File initialDir = new File(currentDir);
        if (initialDir.exists() && initialDir.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDir);
        }

        // 显示目录选择器
        File selectedDir = directoryChooser.showDialog(owner);
        if (selectedDir != null) {
            pluginDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    /**
     * 在系统资源管理器中打开插件目录
     * 如果目录不存在会尝试创建
     * 如果无法打开目录会显示错误提示
     */
    private void openPluginFolder() {
        String dirPath = pluginDirField.getText();
        File dir = new File(dirPath);

        // 如果目录不存在，尝试创建
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                showError("无法创建目录", "创建插件目录失败：" + dirPath);
                return;
            }
        }

        try {
            Desktop.getDesktop().open(dir);
        } catch (IOException e) {
            log.error("Failed to open plugin directory", e);
            showError("打开失败", "无法打开插件目录：" + e.getMessage());
        }
    }

    /**
     * 重置为默认插件目录
     * 会先显示确认对话框
     * 确认后会:
     * 1. 更新输入框中的路径为默认路径
     * 2. 确保默认目录存在,不存在则创建
     */
    private void resetToDefault() {
        // 确认对话框
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认重置");
        confirm.setHeaderText("确认重置为默认插件目录？");
        confirm.setContentText("默认目录：" + DEFAULT_PLUGIN_DIR);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                pluginDirField.setText(DEFAULT_PLUGIN_DIR);

                // 确保默认目录存在
                File defaultDir = new File(DEFAULT_PLUGIN_DIR);
                if (!defaultDir.exists()) {
                    if (!defaultDir.mkdirs()) {
                        showError("创建失败", "无法创建默认插件目录");
                    }
                }
            }
        });
    }

    /**
     * 显示错误对话框
     * 用于向用户展示操作过程中的错误信息
     *
     * @param header  错误标题
     * @param content 错误详细信息
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 显示对话框并保存配置
     * 保存成功后会提示需要重启应用
     * 保存过程包括:
     * 1. 验证新目录路径有效性
     * 2. 检查是否发生变化
     * 3. 更新配置并保存
     * 4. 确保新目录存在
     * 5. 显示成功提示
     */
    public void showAndSave() {
        showAndWait().ifPresent(newPluginDir -> {
            // 验证新目录路径不为空
            if (newPluginDir == null || newPluginDir.trim().isEmpty()) {
                showError("无效的目录", "插件目录路径不能为空");
                return;
            }

            if (!newPluginDir.equals(ConfigFactory.getInstance().getConfiguration().getPluginDir())) {
                ConfigFactory.getInstance().getConfiguration().setPluginDir(newPluginDir);
                try {
                    ConfigurationLoader.save();

                    // 尝试创建目录
                    File newDir = new File(newPluginDir);
                    if (!newDir.exists() && !newDir.mkdirs()) {
                        showError("创建失败", "无法创建插件目录：" + newPluginDir);
                        return;
                    }

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("保存成功");
                    alert.setHeaderText(null);
                    alert.setContentText("插件目录配置已保存，重启应用程序后生效");
                    alert.showAndWait();
                } catch (Exception e) {
                    log.error("Failed to save plugin directory configuration", e);
                    showError("保存失败", "保存插件目录配置失败：" + e.getMessage());
                }
            }
        });
    }
}