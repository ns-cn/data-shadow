package com.tangyujun.datashadow.ui.menu;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import com.tangyujun.datashadow.scheme.ComparisonSchemeManager;
import com.tangyujun.datashadow.ui.menu.dialog.SystemSettingDialog;
import com.tangyujun.datashadow.ui.menu.dialog.QADialog;
import com.tangyujun.datashadow.core.Commit;

import javafx.application.Platform;

/**
 * 菜单栏组件
 * 包含文件菜单和帮助菜单:
 * - 文件菜单:
 * - 退出: 退出应用程序
 * - 帮助菜单:
 * - Q&A: 显示常见问题解答
 * - 关于: 显示应用程序版本信息和开发者信息
 */
public class MenuBarSection extends VBox {

    /**
     * JavaFX菜单栏控件
     */
    private final MenuBar menuBar;

    /**
     * 构造函数
     * 初始化菜单栏及其菜单项,设置事件处理
     */
    public MenuBarSection() {
        menuBar = new MenuBar();

        // 创建文件菜单
        Menu fileMenu = new Menu("文件");

        // 创建退出菜单项
        MenuItem exitItem = new MenuItem("退出");
        exitItem.setOnAction(event -> showExitConfirmDialog());

        // 将菜单项添加到文件菜单
        fileMenu.getItems().add(exitItem);

        // 创建对比方案菜单
        Menu comparisonSchemeMenu = new Menu("对比方案");

        MenuItem importMenuItem = new MenuItem("导入方案");
        importMenuItem.setOnAction(event -> {
            ComparisonSchemeManager.importScheme(menuBar.getScene().getWindow());
        });

        MenuItem exportMenuItem = new MenuItem("导出方案");
        exportMenuItem.setOnAction(event -> {
            ComparisonSchemeManager.exportScheme(menuBar.getScene().getWindow());
        });

        comparisonSchemeMenu.getItems().addAll(importMenuItem, exportMenuItem);

        // 创建设置菜单
        Menu settingsMenu = new Menu("设置");

        // 创建系统设置菜单项
        MenuItem systemSettingItem = new MenuItem("系统设置");
        systemSettingItem.setOnAction(event -> showSystemSettingDialog());

        settingsMenu.getItems().add(systemSettingItem);

        // 创建帮助菜单
        Menu helpMenu = new Menu("帮助");

        // 创建Q&A菜单项
        MenuItem qaItem = new MenuItem("Q&A");
        qaItem.setOnAction(event -> showQADialog());

        // 创建关于菜单项
        MenuItem aboutItem = new MenuItem("关于");
        aboutItem.setOnAction(event -> showAboutDialog());

        // 将菜单项添加到帮助菜单,使用分隔符分隔Q&A和关于
        helpMenu.getItems().addAll(qaItem, new SeparatorMenuItem(), aboutItem);

        // 将菜单添加到菜单栏
        menuBar.getMenus().addAll(fileMenu, comparisonSchemeMenu, settingsMenu, helpMenu);

        // 将菜单栏添加到VBox容器
        getChildren().add(menuBar);
    }

    /**
     * 显示退出确认对话框
     * 弹出确认对话框询问用户是否确定退出程序
     * 如果用户点击确定,则调用Platform.exit()退出程序
     */
    private void showExitConfirmDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认退出");
        alert.setHeaderText("确认退出程序");
        alert.setContentText("确定要退出程序吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    /**
     * 显示关于对话框
     * 显示应用程序的版本信息、开发者信息等
     */
    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("DataShadow");
        alert.setContentText("""
                版本: %s
                开发者: tangyujun

                DataShadow是一个基于JavaFX的数据比对工具,支持多种数据源的结构化数据读取、比对和结果展示。
                主要用于数据迁移、数据校验、数据一致性检查等场景。""".formatted(Commit.VERSION));
        alert.showAndWait();
    }

    /**
     * 显示Q&A对话框
     * 显示常见问题解答内容
     */
    private void showQADialog() {
        QADialog dialog = new QADialog(menuBar.getScene().getWindow());
        dialog.show();
    }

    /**
     * 显示系统设置对话框
     */
    private void showSystemSettingDialog() {
        SystemSettingDialog dialog = new SystemSettingDialog(menuBar.getScene().getWindow());
        dialog.showAndSave();
    }
}