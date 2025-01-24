package com.tangyujun.datashadow.ui.datasource;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.core.DataSourceChangeListener;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallbackAdapter;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.ui.components.GroupComboBox;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

/**
 * 数据源维护区域
 */
public class DataSourceSection extends VBox implements DataSourceChangeListener {
    private final DataFactory dataFactory = DataFactory.getInstance();
    // 标记是否由onDataSourceChanged触发的值变更
    private boolean isUpdatingFromCallback = false;

    // 保存主数据源和影子数据源的控件引用
    private GroupComboBox<DataSourceGenerator> primaryTypeSelect;
    private GroupComboBox<DataSourceGenerator> shadowTypeSelect;
    private Button primaryConfigButton;
    private Button shadowConfigButton;
    private Button primaryMappingButton;
    private Button shadowMappingButton;
    private Label primaryStatus;
    private Label shadowStatus;

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

        // 注册数据源变更监听器
        dataFactory.addDataSourceChangeListener(this);
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
        GroupComboBox<DataSourceGenerator> typeSelect = new GroupComboBox<>();
        // 从DataFactory获取所有已注册的数据源
        typeSelect.setDataMap(dataFactory.getDataSources());
        typeSelect.setPrefWidth(200);

        Button configButton = new Button("配置数据源");
        configButton.setPrefWidth(120);
        configButton.setDisable(true);

        Button mappingButton = new Button("字段映射");
        mappingButton.setPrefWidth(120);
        mappingButton.setDisable(true);

        // 保存控件引用
        if (isPrimary) {
            primaryTypeSelect = typeSelect;
            primaryConfigButton = configButton;
            primaryMappingButton = mappingButton;
            primaryStatus = status;
        } else {
            shadowTypeSelect = typeSelect;
            shadowConfigButton = configButton;
            shadowMappingButton = mappingButton;
            shadowStatus = status;
        }

        // 数据源选择事件处理
        typeSelect.setOnSelectListener(event -> {
            // 如果是由onDataSourceChanged触发的，则不执行后续逻辑
            if (isUpdatingFromCallback) {
                return;
            }
            DataSourceGenerator generator = event.value();
            if (generator != null) {
                DataSource dataSource = generator.generate();
                // 设置到DataFactory
                if (isPrimary) {
                    dataFactory.setPrimaryDataSource(event.group(), event.name(), dataSource);
                } else {
                    dataFactory.setShadowDataSource(event.group(), event.name(), dataSource);
                }
                // 更新状态显示
                status.setText(dataSource.getDescription());
                configButton.setDisable(false);
                mappingButton.setDisable(false);
            }
        });

        // 配置按钮事件处理
        configButton.setOnAction(event -> {
            DataSourceGenerator generator = typeSelect.getValue();
            if (generator != null) {
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

        // 字段映射按钮事件处理
        mappingButton.setOnAction(event -> {
            DataSource dataSource = isPrimary ? dataFactory.getPrimaryDataSource()
                    : dataFactory.getShadowDataSource();
            if (dataSource != null) {
                DataSourceMappingDialog dialog = new DataSourceMappingDialog(getScene().getWindow(), dataSource);
                dialog.showAndWait();
            }
        });

        controls.getChildren().addAll(typeSelect, configButton, mappingButton);
        box.getChildren().addAll(titleLabel, controls, status);

        return box;
    }

    @Override
    public void onDataSourceChanged(boolean isPrimary, String group, String sourceName, DataSource dataSource) {
        javafx.application.Platform.runLater(() -> {
            GroupComboBox<DataSourceGenerator> typeSelect = isPrimary ? primaryTypeSelect : shadowTypeSelect;
            Button configButton = isPrimary ? primaryConfigButton : shadowConfigButton;
            Button mappingButton = isPrimary ? primaryMappingButton : shadowMappingButton;
            Label status = isPrimary ? primaryStatus : shadowStatus;

            isUpdatingFromCallback = true;
            // 更新数据源类型选择
            if (sourceName != null) {
                typeSelect.setSelectedItem(group, sourceName);
                // 手动更新状态
                if (dataSource != null) {
                    status.setText(dataSource.getDescription());
                    configButton.setDisable(false);
                    mappingButton.setDisable(false);
                }
            } else {
                typeSelect.setSelectedItem(null, null);
                status.setText("未配置数据源");
                configButton.setDisable(true);
                mappingButton.setDisable(true);
            }
            isUpdatingFromCallback = false;
        });
    }
}