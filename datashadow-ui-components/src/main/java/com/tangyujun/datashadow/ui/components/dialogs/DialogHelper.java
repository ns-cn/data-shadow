package com.tangyujun.datashadow.ui.components.dialogs;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 对话框辅助类
 * 提供通用的对话框显示功能,包括警告框和成功提示框
 * 主要功能:
 * 1. 显示警告对话框
 * 2. 显示成功对话框,支持打开导出的文件
 */
public class DialogHelper {
    /** 日志记录器 */
    private static final Logger log = Logger.getLogger(DialogHelper.class.getName());

    /**
     * 显示警告对话框
     * 用于显示警告信息或错误提示
     *
     * @param title   对话框标题
     * @param content 对话框内容
     */
    public static void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 显示成功对话框
     * 用于显示操作成功的提示信息,并提供打开导出文件的选项
     *
     * @param title   对话框标题
     * @param content 对话框内容
     * @param file    导出的文件对象
     */
    public static void showSuccessDialog(String title, String content, File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        // 创建对话框按钮
        ButtonType openButton = new ButtonType("打开文件");
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openButton, closeButton);

        // 处理按钮点击事件
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == openButton) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    log.log(Level.SEVERE, "打开文件失败: {0}", e.getMessage());
                    showAlert("打开失败", "无法打开文件：" + e.getMessage());
                }
            }
        });
    }
}