package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.util.List;

/**
 * 对比结果展示区
 */
public class ResultSection extends VBox {

    private final TableView<Object> resultTable;

    public ResultSection() {
        super(5);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 创建标题
        Label title = new Label("对比结果");
        title.setStyle("-fx-font-weight: bold;");

        // 创建结果表格
        resultTable = new TableView<>();
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        // 添加示例列
        List<TableColumn<Object, String>> columns = List.of(
                createColumn("用户ID", "userId", 100),
                createColumn("用户名称", "userName", 150),
                createColumn("年龄", "age", 80),
                createColumn("性别", "gender", 80),
                createColumn("手机号", "phone", 150));
        resultTable.getColumns().addAll(columns);

        getChildren().addAll(title, resultTable);
    }

    private TableColumn<Object, String> createColumn(String title, String property, double width) {
        TableColumn<Object, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        // TODO: 设置单元格值工厂
        return column;
    }
}