package com.tangyujun.datashadow.ui.components;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 测试自定义分组下拉选框
 */
public class Test extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // 第一个下拉框 - HTTP请求方法
        GroupComboBox<String> httpMethodCombo = new GroupComboBox<>("请选择请求方法");

        // HTTP请求方法数据
        Map<String, Map<String, String>> httpMethodData = new LinkedHashMap<>();

        Map<String, String> basicMethods = new LinkedHashMap<>();
        basicMethods.put("GET", "GET");
        basicMethods.put("POST", "POST");
        basicMethods.put("PUT", "PUT");
        basicMethods.put("DELETE", "DELETE");
        httpMethodData.put("基础请求方法", basicMethods);

        Map<String, String> advancedMethods = new LinkedHashMap<>();
        advancedMethods.put("HEAD", "HEAD");
        advancedMethods.put("OPTIONS", "OPTIONS");
        advancedMethods.put("PATCH", "PATCH");
        advancedMethods.put("TRACE", "TRACE");
        httpMethodData.put("高级请求方法", advancedMethods);

        httpMethodCombo.setDataMap(httpMethodData);
        httpMethodCombo.setOnSelectListener(
                event -> System.out.printf("HTTP Method Selected - Group: %s, Name: %s, Value: %s%n",
                        event.group(), event.name(), event.value()));

        // 第二个下拉框 - 数据源类型
        GroupComboBox<String> dataSourceCombo = new GroupComboBox<>("请选择数据源类型");

        // 数据源类型数据
        Map<String, Map<String, String>> dataSourceData = new LinkedHashMap<>();

        Map<String, String> fileTypes = new LinkedHashMap<>();
        fileTypes.put("CSV", "CSV");
        fileTypes.put("Excel", "Excel");
        fileTypes.put("JSON", "JSON");
        fileTypes.put("XML", "XML");
        dataSourceData.put("文件类型", fileTypes);

        Map<String, String> dbTypes = new LinkedHashMap<>();
        dbTypes.put("MySQL", "MySQL");
        dbTypes.put("Oracle", "Oracle");
        dbTypes.put("Memory内存数据库", "Memory");
        dataSourceData.put("数据库类型", dbTypes);

        Map<String, String> otherTypes = new LinkedHashMap<>();
        otherTypes.put("HTTP接口", "HTTP");
        dataSourceData.put("其他类型", otherTypes);

        dataSourceCombo.setDataMap(dataSourceData);
        dataSourceCombo.setOnSelectListener(
                event -> System.out.printf("Data Source Selected - Group: %s, Name: %s, Value: %s%n",
                        event.group(), event.name(), event.value()));

        dataSourceCombo.setSelectedItem("其他类型", "HTTP接口");

        // 添加到布局
        root.getChildren().addAll(httpMethodCombo, dataSourceCombo);

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setTitle("自定义分组下拉框测试");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
