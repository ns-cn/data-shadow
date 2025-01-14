package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

/**
 * 数据源维护区域
 */
public class DataSourceSection extends VBox {

    public DataSourceSection() {
        super(5);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 创建标题
        Label title = new Label("数据源维护");
        title.setStyle("-fx-font-weight: bold;");

        // 创建数据源容器
        HBox sourcesBox = new HBox(20);
        sourcesBox.getChildren().addAll(
                createDataSourceBox("主数据源"),
                createDataSourceBox("影子数据源"));

        getChildren().addAll(title, sourcesBox);
    }

    private VBox createDataSourceBox(String title) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #ddd;");
        box.setPrefWidth(400);

        Label titleLabel = new Label(title);

        HBox controls = new HBox(5);
        ComboBox<String> typeSelect = new ComboBox<>();
        typeSelect.getItems().addAll("MySQL", "Oracle", "Excel", "CSV");
        typeSelect.setPrefWidth(200);

        Button configButton = new Button("配置数据源");
        configButton.setPrefWidth(120);

        Button mappingButton = new Button("字段映射");
        mappingButton.setPrefWidth(120);

        controls.getChildren().addAll(typeSelect, configButton, mappingButton);

        Label status = new Label("未配置数据源");
        status.setStyle("-fx-text-fill: #666;");

        box.getChildren().addAll(titleLabel, controls, status);

        return box;
    }
}