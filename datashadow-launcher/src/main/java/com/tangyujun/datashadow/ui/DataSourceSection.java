package com.tangyujun.datashadow.ui;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallbackAdapter;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

/**
 * 数据源维护区域
 */
public class DataSourceSection extends VBox {
    private final DataFactory dataFactory = DataFactory.getInstance();

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
                createDataSourceBox("主数据源", true),
                createDataSourceBox("影子数据源", false));

        getChildren().addAll(title, sourcesBox);
    }

    private VBox createDataSourceBox(String title, boolean isPrimary) {
        VBox box = new VBox(5);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-border-color: #ddd;");
        box.setPrefWidth(400);

        Label titleLabel = new Label(title);
        Label status = new Label("未配置数据源");
        status.setStyle("-fx-text-fill: #666;");

        HBox controls = new HBox(5);
        ComboBox<String> typeSelect = new ComboBox<>();
        // 从DataFactory获取所有已注册的数据源
        typeSelect.getItems().addAll(dataFactory.getDataSources().keySet());
        typeSelect.setPrefWidth(200);

        Button configButton = new Button("配置数据源");
        configButton.setPrefWidth(120);
        configButton.setDisable(true);

        Button mappingButton = new Button("字段映射");
        mappingButton.setPrefWidth(120);
        mappingButton.setDisable(true);

        // 数据源选择事件处理
        typeSelect.setOnAction(e -> {
            String selectedType = typeSelect.getValue();
            if (selectedType != null) {
                DataSourceGenerator generator = dataFactory.getDataSources().get(selectedType);
                if (generator != null) {
                    DataSource dataSource = generator.generate();
                    // 设置到DataFactory
                    if (isPrimary) {
                        dataFactory.setPrimaryDataSource(dataSource);
                    } else {
                        dataFactory.setShadowDataSource(dataSource);
                    }

                    // 更新状态显示
                    status.setText(dataSource.getDescription());
                    configButton.setDisable(false);
                    mappingButton.setDisable(false);
                }
            }
        });

        // 配置按钮事件处理
        configButton.setOnAction(e -> {
            String selectedType = typeSelect.getValue();
            if (selectedType != null) {
                DataSource dataSource = isPrimary ? dataFactory.getPrimaryDataSource()
                        : dataFactory.getShadowDataSource();
                DataSourceConfigurationCallback callback = new DataSourceConfigurationCallbackAdapter() {
                    @Override
                    public void onConfigureFinished() {
                        // 更新状态显示
                        status.setText(dataSource.getDescription());
                        mappingButton.setDisable(false);
                    }
                };
                dataSource.configure(getScene().getWindow(), callback);
            }
        });

        controls.getChildren().addAll(typeSelect, configButton, mappingButton);
        box.getChildren().addAll(titleLabel, controls, status);

        return box;
    }
}