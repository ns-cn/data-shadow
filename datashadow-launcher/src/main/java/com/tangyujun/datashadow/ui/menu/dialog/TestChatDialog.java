package com.tangyujun.datashadow.ui.menu.dialog;

import java.util.ArrayList;
import java.util.List;

import com.tangyujun.datashadow.ai.Models;
import com.tangyujun.datashadow.ai.AIService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;

/**
 * AI对话测试窗口
 * 使用聊天软件式的布局，提供更好的对话体验
 * 
 * 主要功能:
 * 1. 提供类似聊天软件的对话界面
 * 2. 支持与AI模型进行对话
 * 3. 保存对话历史记录
 * 4. 支持快捷键发送消息
 */
public class TestChatDialog extends Dialog<Void> {
    /** 日志记录器 */
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TestChatDialog.class);

    /** 消息输入框 */
    private final TextArea inputArea;

    /** 聊天消息显示区域 */
    private final VBox chatBox;

    /** 发送消息按钮 */
    private final Button sendButton;

    /** 聊天记录滚动面板 */
    private final ScrollPane scrollPane;

    /** 消息历史记录列表，用于存储所有对话内容 */
    private final List<JSONObject> messageHistory = new ArrayList<>();

    /**
     * 构造函数
     * 初始化对话窗口的UI组件和事件处理
     *
     * @param owner  父窗口
     * @param model  AI模型
     * @param apiKey API密钥
     */
    public TestChatDialog(Window owner, Models model, String apiKey) {
        setTitle("AI对话测试");
        setHeaderText(null);
        initOwner(owner);

        // 创建主布局
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: #F5F5F5;");

        // 聊天记录区域
        scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background: #F5F5F5; -fx-background-color: #F5F5F5;");
        chatBox = new VBox(15);
        chatBox.setPadding(new Insets(10));
        chatBox.setStyle("-fx-background-color: #F5F5F5;");
        scrollPane.setContent(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(400);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // 输入区域
        VBox inputContainer = new VBox(5);
        inputContainer.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");

        inputArea = new TextArea();
        inputArea.setWrapText(true);
        inputArea.setPrefRowCount(3);
        inputArea.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        // 底部工具栏
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        Label hint = new Label("按Enter发送，Shift+Enter换行");
        hint.setStyle("-fx-text-fill: #666666;");
        HBox.setHgrow(hint, Priority.ALWAYS);
        hint.setAlignment(Pos.CENTER_LEFT);

        sendButton = new Button("发送");
        sendButton.setStyle("""
                -fx-background-color: #007AFF;
                -fx-text-fill: white;
                -fx-padding: 5 15;
                -fx-background-radius: 3;
                """);

        toolbar.getChildren().addAll(hint, sendButton);
        inputContainer.getChildren().addAll(inputArea, toolbar);

        content.getChildren().addAll(scrollPane, inputContainer);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setPrefWidth(700);
        getDialogPane().setPrefHeight(600);

        setResizable(true);

        // 添加系统消息作为第一条消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "请用自然语言回答，请一定不要使用markdown格式");
        messageHistory.add(systemMessage);

        // 按键处理逻辑
        inputArea.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> {
                    if (event.isShiftDown()) {
                        // Shift+Enter实现换行
                        break;
                    } else {
                        // 普通Enter发送消息
                        event.consume(); // 阻止默认的换行
                        sendMessage(model, apiKey);
                    }
                }
                default -> {
                }
            }
        });

        sendButton.setOnAction(e -> sendMessage(model, apiKey));
    }

    /**
     * 添加消息到聊天窗口
     * 创建消息气泡并添加到聊天记录区域
     *
     * @param text   消息内容
     * @param isUser 是否为用户消息
     */
    private void addMessage(String text, boolean isUser) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(500);
        label.setPadding(new Insets(10, 15, 10, 15));

        // 设置消息气泡样式
        String bubbleStyle = isUser ? """
                -fx-background-color: #007AFF;
                -fx-text-fill: white;
                -fx-background-radius: 15 15 0 15;
                """ : """
                -fx-background-color: white;
                -fx-text-fill: black;
                -fx-background-radius: 15 15 15 0;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 1, 1, 0, 1);
                """;
        label.setStyle(bubbleStyle);

        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5, 15, 5, 15));
        messageBox.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.getChildren().add(label);

        chatBox.getChildren().add(messageBox);

        // 自动滚动到底部
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /**
     * 发送消息到AI服务器并处理响应
     * 
     * 处理流程:
     * 1. 获取输入内容并验证
     * 2. 显示用户消息
     * 3. 添加消息到历史记录
     * 4. 清空输入框并禁用发送按钮
     * 5. 异步发送请求到AI服务器
     * 6. 处理服务器响应
     * 7. 显示AI回复
     *
     * @param model  AI模型
     * @param apiKey API密钥
     */
    private void sendMessage(Models model, String apiKey) {
        String input = inputArea.getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        // 显示用户消息
        addMessage(input, true);

        // 添加用户消息到历史记录
        messageHistory.add(AIService.createUserMessage(input));

        // 清空输入框并禁用发送按钮
        inputArea.clear();
        sendButton.setDisable(true);

        // 发送消息
        AIService.sendMessage(
                model,
                apiKey,
                messageHistory,
                content -> {
                    // 成功回调
                    messageHistory.add(AIService.createAssistantMessage(content));
                    Platform.runLater(() -> {
                        sendButton.setDisable(false);
                        addMessage(content, false);
                    });
                },
                error -> {
                    // 错误回调
                    Platform.runLater(() -> {
                        sendButton.setDisable(false);
                        addMessage(error, false);
                    });
                });
    }
}