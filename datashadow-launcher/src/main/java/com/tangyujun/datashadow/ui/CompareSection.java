package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * 对比功能区
 * 用于展示和管理数据对比相关的功能按钮和选项
 * 包含执行对比、显示差异项控制、导入导出对比方案等功能
 */
public class CompareSection extends VBox {

    /**
     * 构造函数
     * 初始化对比功能区的界面布局和控件
     */
    public CompareSection() {
        // 设置垂直间距为5
        super(5);
        // 设置内边距为10
        setPadding(new Insets(10));
        // 设置边框样式
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 创建标题标签
        Label title = new Label("对比功能");
        title.setStyle("-fx-font-weight: bold;");

        // 创建水平工具栏,设置间距为10
        HBox tools = new HBox(10);
        // 设置工具栏左对齐
        tools.setAlignment(Pos.CENTER_LEFT);

        // 创建执行对比按钮
        Button compareButton = new Button("执行对比");
        compareButton.setStyle("-fx-padding: 5 20;");
        // TODO: 添加执行对比的事件处理

        // 创建仅显示差异项的复选框
        CheckBox showDiffOnly = new CheckBox("仅显示差异项");
        // TODO: 添加显示控制的事件处理

        // 创建导入对比方案按钮
        Button importButton = new Button("导入对比方案");
        importButton.setPrefWidth(120);
        // TODO: 添加导入方案的事件处理

        // 创建导出对比方案按钮
        Button exportButton = new Button("导出对比方案");
        exportButton.setPrefWidth(120);
        // TODO: 添加导出方案的事件处理

        // 将所有控件添加到工具栏
        tools.getChildren().addAll(
                compareButton,
                showDiffOnly,
                importButton,
                exportButton);

        // 将标题和工具栏添加到主容器
        getChildren().addAll(title, tools);
    }
}