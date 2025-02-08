package com.tangyujun.datashadow.ui.menu.dialog;

import com.tangyujun.datashadow.ai.Models;
import com.tangyujun.datashadow.configuration.ConfigurationLoader;
import com.tangyujun.datashadow.config.ConfigFactory;
import com.tangyujun.datashadow.ai.AIService;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import javafx.application.Platform;

import java.util.concurrent.TimeUnit;

import com.tangyujun.datashadow.config.Configuration;

/**
 * 系统设置对话框
 * 提供基础配置和AI配置功能
 * 
 * 主要功能:
 * 1. 插件目录配置 - 包括选择、重置、打开目录等操作
 * 2. AI模型配置 - 选择AI模型和配置API Key
 * 3. 配置保存功能
 */
public class SystemSettingDialog extends Dialog<Boolean> {
    /** 日志记录器 */
    private static final Logger log = LoggerFactory.getLogger(SystemSettingDialog.class);

    /** 对话框所属窗口 */
    private final Window owner;

    /** 插件目录输入框 */
    private final TextField pluginDirField;

    /** AI模型选择下拉框 */
    private final ComboBox<Models> aiModelComboBox;

    /** API Key输入框 */
    private final PasswordField apiKeyField;

    /** API Key验证按钮 */
    private final Button validateButton;

    /** API Key输入区域容器 */
    private final HBox apiKeyBox;

    /** 默认插件目录路径 */
    private static final String DEFAULT_PLUGIN_DIR = System.getProperty("user.home")
            + File.separator + ".datashadow"
            + File.separator + "plugins";

    /**
     * 构造函数
     * 初始化对话框界面和控件
     * 
     * @param owner 父窗口
     */
    public SystemSettingDialog(Window owner) {
        this.owner = owner;

        setTitle("系统设置");
        setHeaderText(null);

        // 初始化字段
        pluginDirField = new TextField(ConfigFactory.getInstance().getConfiguration().getPluginDir());
        pluginDirField.setEditable(false);

        aiModelComboBox = new ComboBox<>();
        // 设置单元格工厂，显示displayName
        aiModelComboBox.setCellFactory(listView -> new ListCell<Models>() {
            @Override
            protected void updateItem(Models item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });
        // 设置按钮单元格工厂，显示displayName
        aiModelComboBox.setButtonCell(new ListCell<Models>() {
            @Override
            protected void updateItem(Models item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDisplayName());
                }
            }
        });

        apiKeyField = new PasswordField();
        validateButton = new Button("验证");
        apiKeyBox = new HBox(10);

        // 初始化值
        initializeValues();

        // 创建主容器
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(25));
        mainContainer.setMinWidth(600);

        // 添加基础配置和AI配置表单
        mainContainer.getChildren().addAll(
                createBasicConfigForm(),
                createAIConfigForm());

        // 设置对话框内容
        getDialogPane().setContent(mainContainer);

        // 添加按钮
        ButtonType saveButtonType = new ButtonType("保存设置", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

        // 设置结果转换器
        setResultConverter(dialogButton -> dialogButton == saveButtonType);
    }

    /**
     * 创建基础配置表单
     * 包含插件目录配置相关控件
     * 
     * @return 基础配置表单面板
     */
    private TitledPane createBasicConfigForm() {
        GridPane content = new GridPane();
        content.setPadding(new Insets(10));
        content.setHgap(10);
        content.setVgap(10);

        // 插件目录配置
        Label pathLabel = new Label("插件目录路径：");
        content.add(pathLabel, 0, 0);

        pluginDirField.setPrefWidth(400);
        content.add(pluginDirField, 1, 0);

        // 按钮放在下一行
        HBox buttonBox = new HBox(10);
        Button browseButton = new Button("选择目录");
        Button resetButton = new Button("重置为默认");
        Button openButton = new Button("打开目录");

        browseButton.setOnAction(e -> browseDirectory());
        resetButton.setOnAction(e -> resetToDefault());
        openButton.setOnAction(e -> openPluginFolder());

        buttonBox.getChildren().addAll(browseButton, resetButton, openButton);
        // 按钮占据第二行，跨两列
        content.add(buttonBox, 0, 1, 2, 1);

        TitledPane form = new TitledPane("基础配置", content);
        form.setCollapsible(false);
        return form;
    }

    /**
     * 创建AI配置表单
     * 包含AI模型选择和API Key配置相关控件
     * 
     * @return AI配置表单面板
     */
    private TitledPane createAIConfigForm() {
        GridPane content = new GridPane();
        content.setPadding(new Insets(10));
        content.setHgap(10);
        content.setVgap(10);

        // AI模型选择
        Label modelLabel = new Label("AI模型：");
        content.add(modelLabel, 0, 0);

        aiModelComboBox.setPrefWidth(400);
        aiModelComboBox.getItems().clear();
        // 直接添加所有Models枚举值
        aiModelComboBox.getItems().addAll(Models.values());
        content.add(aiModelComboBox, 1, 0);

        // API Key配置
        Label keyLabel = new Label("API Key：");
        content.add(keyLabel, 0, 1);

        apiKeyField.setPrefWidth(400);
        content.add(apiKeyField, 1, 1);

        // 验证和测试按钮放在API Key下方
        HBox buttonBox = new HBox(10);
        validateButton.setOnAction(e -> validateApiKey());
        Button testButton = new Button("测试对话");
        testButton.setOnAction(e -> showTestDialog());
        buttonBox.getChildren().addAll(validateButton, testButton);
        // 按钮占据第三行，跨两列
        content.add(buttonBox, 0, 2, 2, 1);

        TitledPane form = new TitledPane("AI配置", content);
        form.setCollapsible(false);
        return form;
    }

    /**
     * 初始化配置值
     * 从ConfigFactory加载已保存的配置
     */
    private void initializeValues() {
        var config = ConfigFactory.getInstance().getConfiguration();

        // 插件目录
        String pluginDir = config.getPluginDir();
        if (pluginDir == null || pluginDir.trim().isEmpty()) {
            pluginDirField.setText(ConfigFactory.getInstance().getPluginDir());
        } else {
            pluginDirField.setText(pluginDir);
        }

        // 设置当前选中的AI模型
        String savedModel = config.getAiModel();
        if (savedModel != null && !savedModel.trim().isEmpty()) {
            for (Models model : Models.values()) {
                if (model.getModelName().equals(savedModel)) {
                    aiModelComboBox.setValue(model);
                    break;
                }
            }
        }
        // 如果没有保存的模型或找不到对应的模型，设置默认值
        if (aiModelComboBox.getValue() == null && !aiModelComboBox.getItems().isEmpty()) {
            aiModelComboBox.setValue(aiModelComboBox.getItems().get(0));
        }

        // API Key
        String apiKey = config.getAiApiKey();
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            apiKeyField.setText(apiKey);
        }

        // 如果有API Key，启用验证和测试按钮
        boolean hasApiKey = apiKey != null && !apiKey.trim().isEmpty();
        validateButton.setDisable(!hasApiKey);
    }

    /**
     * 打开目录选择器
     * 用于选择新的插件目录
     */
    private void browseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择插件目录");

        String currentDir = pluginDirField.getText();
        File initialDir = new File(currentDir);
        if (initialDir.exists() && initialDir.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDir);
        }

        File selectedDir = directoryChooser.showDialog(owner);
        if (selectedDir != null) {
            pluginDirField.setText(selectedDir.getAbsolutePath());
        }
    }

    /**
     * 在系统资源管理器中打开插件目录
     * 如果目录不存在会尝试创建
     */
    private void openPluginFolder() {
        String dirPath = pluginDirField.getText();
        File dir = new File(dirPath);

        if (!dir.exists() && !dir.mkdirs()) {
            showError("无法创建目录", "创建插件目录失败：" + dirPath);
            return;
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
     * 会弹出确认对话框
     */
    private void resetToDefault() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认重置");
        confirm.setHeaderText("确认重置为默认插件目录？");
        confirm.setContentText("默认目录：" + DEFAULT_PLUGIN_DIR);

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                pluginDirField.setText(DEFAULT_PLUGIN_DIR);
                File defaultDir = new File(DEFAULT_PLUGIN_DIR);
                if (!defaultDir.exists() && !defaultDir.mkdirs()) {
                    showError("创建失败", "无法创建默认插件目录");
                }
            }
        });
    }

    /**
     * 验证API Key
     * 通过调用SiliconFlow的chat completions接口验证API Key的有效性
     */
    private void validateApiKey() {
        String apiKey = apiKeyField.getText();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showError("验证失败", "请输入API Key");
            return;
        }

        Models selectedModel = aiModelComboBox.getValue();
        if (selectedModel == null) {
            showError("验证失败", "请选择AI模型");
            return;
        }

        validateButton.setDisable(true);
        validateButton.setText("验证中...");

        new Thread(() -> {
            try {
                boolean isValid = AIService.validateApiKey(selectedModel, apiKey);

                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    validateButton.setText("验证");

                    if (isValid) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("验证成功");
                        alert.setHeaderText(null);
                        alert.setContentText("API Key验证通过");
                        alert.showAndWait();
                    } else {
                        showError("验证失败", "API Key无效");
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    validateButton.setText("验证");
                    showError("验证失败", "连接错误: " + e.getMessage());
                });
                log.error("Failed to validate API Key", e);
            }
        }).start();
    }

    /**
     * 显示测试对话窗口
     */
    private void showTestDialog() {
        String apiKey = apiKeyField.getText();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            showError("测试失败", "请输入API Key");
            return;
        }

        Models selectedModel = aiModelComboBox.getValue();
        if (selectedModel == null) {
            showError("测试失败", "请选择AI模型");
            return;
        }

        TestChatDialog dialog = new TestChatDialog(getDialogPane().getScene().getWindow(), selectedModel, apiKey);
        dialog.show();
    }

    /**
     * 显示对话框并保存设置
     * 如果用户点击保存按钮，会将设置保存到配置文件
     */
    public void showAndSave() {
        showAndWait().ifPresent(result -> {
            if (result) {
                try {
                    // 保存所有配置
                    Configuration config = ConfigFactory.getInstance().getConfiguration();
                    config.setPluginDir(pluginDirField.getText());
                    // 保存模型的modelName
                    Models selectedModel = aiModelComboBox.getValue();

                    if (selectedModel != null) {
                        config.setAiModel(selectedModel.getModelName());
                    }
                    config.setAiApiKey(apiKeyField.getText());
                    ConfigFactory.getInstance().updateConfiguration(config, true);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("保存成功");
                    alert.setHeaderText(null);
                    alert.setContentText("设置已保存，重启程序后生效");
                    alert.showAndWait();
                } catch (Exception e) {
                    log.error("Failed to save configuration", e);
                    showError("保存失败", "保存配置失败：" + e.getMessage());
                }
            }
        });
    }

    /**
     * 显示错误对话框
     * 
     * @param header  错误标题
     * @param content 错误内容
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}