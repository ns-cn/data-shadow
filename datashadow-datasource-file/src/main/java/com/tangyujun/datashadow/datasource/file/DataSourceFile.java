package com.tangyujun.datashadow.datasource.file;

import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.Objects;

/**
 * 文件类型数据源的基类
 * 提供文件路径的基本属性和方法
 */
public abstract class DataSourceFile extends DataSource {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    protected String path;

    /**
     * 获取文件路径
     * 
     * @return 文件路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置文件路径
     * 
     * @param path 文件路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 判断两个对象是否相等
     * 
     * @param o 要比较的对象
     * @return 如果对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DataSourceFile that = (DataSourceFile) o;
        return Objects.equals(path, that.path);
    }

    /**
     * 计算对象的哈希值
     * 
     * @return 对象的哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }

    /**
     * 配置文件数据源
     * 弹出对话框让用户选择文件路径
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // 创建配置对话框
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("配置文件数据源");

        // 创建布局
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        // 添加文件路径输入框和选择按钮
        Label pathLabel = new Label("文件路径:");
        TextField pathField = new TextField();
        pathField.setPrefWidth(300);
        Button chooseButton = new Button("选择文件");
        Button confirmButton = new Button("确认");
        Button cancelButton = new Button("取消");

        // 设置文件选择按钮事件
        chooseButton.setOnAction(_ -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择数据源文件");
            File file = fileChooser.showOpenDialog(dialog);
            if (file != null) {
                pathField.setText(file.getAbsolutePath());
            }
        });

        // 设置确认按钮事件
        confirmButton.setOnAction(_ -> {
            this.path = pathField.getText();
            dialog.close();
            if (callback != null) {
                callback.onConfigureFinished();
            }
        });

        // 设置取消按钮事件
        cancelButton.setOnAction(_ -> dialog.close());

        // 布局组件
        grid.add(pathLabel, 0, 0);
        grid.add(pathField, 1, 0);
        grid.add(chooseButton, 2, 0);
        grid.add(confirmButton, 1, 1);
        grid.add(cancelButton, 2, 1);

        Scene scene = new Scene(grid);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
