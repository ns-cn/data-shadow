package com.tangyujun.datashadow.exporter;

import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.stage.Window;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 导出对话框辅助类
 * 用于显示导出成功和失败的对话框
 * 
 * @author tangyujun
 */
public class ExportDialogHelper {
    private static final Logger log = Logger.getLogger(ExportDialogHelper.class.getName());

    /**
     * 显示导出成功对话框
     * 
     * @param title   对话框标题
     * @param message 成功消息
     * @param file    导出的文件
     * @param window  父窗口
     */
    public static void showSuccessDialog(String title, String message, File file, Window window) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);

        VBox content = new VBox(5);
        content.getChildren().add(new javafx.scene.control.Label(message));

        if (file != null) {
            HBox linkBox = new HBox(10);
            linkBox.setAlignment(Pos.CENTER_LEFT);

            Hyperlink openFileLink = new Hyperlink("打开文件");
            openFileLink.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException ex) {
                    log.log(Level.WARNING, "无法打开文件", ex);
                }
            });

            Hyperlink openDirLink = new Hyperlink("打开所在目录");
            openDirLink.setOnAction(e -> {
                try {
                    Desktop.getDesktop().open(file.getParentFile());
                } catch (IOException ex) {
                    log.log(Level.WARNING, "无法打开文件目录", ex);
                }
            });

            linkBox.getChildren().addAll(openFileLink, openDirLink);
            content.getChildren().add(linkBox);
        }

        alert.getDialogPane().setContent(content);
        if (window != null) {
            alert.initOwner(window);
        }
        alert.showAndWait();
    }

    /**
     * 显示导出失败对话框
     * 
     * @param title   对话框标题
     * @param message 错误消息
     * @param error   异常信息
     * @param window  父窗口
     */
    public static void showErrorDialog(String title, String message, Throwable error, Window window) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);

        VBox content = new VBox(5);
        content.getChildren().add(new javafx.scene.control.Label(message));

        if (error != null) {
            String errorDetails = error.getMessage();
            if (errorDetails != null && !errorDetails.isEmpty()) {
                content.getChildren().add(new javafx.scene.control.Label("错误详情：" + errorDetails));
            }
        }

        alert.getDialogPane().setContent(content);
        if (window != null) {
            alert.initOwner(window);
        }
        alert.showAndWait();
    }
}