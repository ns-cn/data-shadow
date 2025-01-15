package com.tangyujun.datashadow.ui.dialog;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据源映射对话框
 * 用于配置数据项与数据源字段之间的映射关系
 * 
 * 主要功能:
 * 1. 显示数据项列表和对应的数据源字段下拉选择
 * 2. 支持自动映射和重建映射功能
 * 3. 提供确认和取消操作
 * 4. 返回配置好的映射关系
 */
public class DataSourceMappingDialog extends Stage {

    /**
     * 存储数据项与数据源字段的映射关系
     */
    private final Map<DataItem, String> mappings = new HashMap<>();

    /**
     * 当前操作的数据源对象
     */
    private final DataSource dataSource;

    /**
     * 数据源中的所有字段列表
     */
    private final List<String> sourceColumns;

    /**
     * 标记用户是否点击了确认按钮
     */
    private boolean confirmed = false;

    /**
     * 构造函数
     * 
     * @param owner      父窗口
     * @param dataSource 需要配置映射的数据源
     */
    public DataSourceMappingDialog(Window owner, DataSource dataSource) {
        this.dataSource = dataSource;
        this.sourceColumns = dataSource.getColumns();

        // 初始化映射关系,回显已有的映射
        DataFactory.getInstance().getDataItems().forEach(dataItem -> {
            // 添加空值检查
            Map<String, String> sourceMappings = dataSource.getMappings();
            if (sourceMappings != null) {
                String mappedField = sourceMappings.get(dataItem.getCode());
                if (mappedField != null) {
                    mappings.put(dataItem, mappedField);
                }
            }
        });

        // 设置对话框属性
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("数据源字段映射");
        setMinWidth(600);
        setMinHeight(400);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // 标题
        Label titleLabel = new Label("数据源字段映射");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 操作按钮区域
        HBox buttonBox = createButtonBox();

        // 映射表格
        TableView<DataItem> mappingTable = createMappingTable();
        VBox.setVgrow(mappingTable, Priority.ALWAYS);

        // 底部按钮
        HBox bottomButtons = createBottomButtons();

        root.getChildren().addAll(titleLabel, buttonBox, mappingTable, bottomButtons);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    /**
     * 创建顶部操作按钮区域
     * 包含自动映射、重建映射按钮和提示说明
     * 
     * @return 按钮容器
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #eee;");

        Button autoMapButton = new Button("自动映射");
        Button rebuildButton = new Button("重建映射");

        Label tipLabel = new Label("注：自动映射将根据字段名称相似度追加映射关系，重建映射将根据数据源字段创建新的数据项");
        tipLabel.setWrapText(true);
        tipLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");

        autoMapButton.setOnAction(_ -> handleAutoMap());
        rebuildButton.setOnAction(_ -> handleRebuild());

        buttonBox.getChildren().addAll(autoMapButton, rebuildButton, tipLabel);
        return buttonBox;
    }

    /**
     * 创建映射关系表格
     * 显示数据项名称和对应的数据源字段选择下拉框
     * 
     * @return 映射表格控件
     */
    @SuppressWarnings("unchecked")
    private TableView<DataItem> createMappingTable() {
        TableView<DataItem> table = new TableView<>();

        // 数据项名称列
        TableColumn<DataItem, String> nameColumn = new TableColumn<>("数据项名称");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayName()));
        nameColumn.setPrefWidth(200);

        // 数据源字段列
        TableColumn<DataItem, String> mappingColumn = new TableColumn<>("数据源字段");
        mappingColumn.setPrefWidth(300);
        mappingColumn.setCellFactory(_ -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                // 添加一个空选项
                List<String> options = new ArrayList<>();
                options.add(""); // 空选项
                options.addAll(sourceColumns);
                comboBox.setItems(FXCollections.observableArrayList(options));
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.valueProperty().addListener((_, _, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        if (newVal != null && !newVal.isEmpty()) {
                            mappings.put(getTableRow().getItem(), newVal);
                        } else {
                            mappings.remove(getTableRow().getItem());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DataItem dataItem = getTableRow().getItem();
                    if (dataItem != null) {
                        comboBox.setValue(mappings.get(dataItem));
                        setGraphic(comboBox);
                    }
                }
            }
        });

        table.getColumns().addAll(nameColumn, mappingColumn);
        table.setItems(DataFactory.getInstance().getDataItems());

        return table;
    }

    /**
     * 创建底部按钮区域
     * 包含确定和取消按钮
     * 
     * @return 按钮容器
     */
    private HBox createBottomButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        Button confirmButton = new Button("确定");
        confirmButton.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");

        cancelButton.setOnAction(_ -> close());
        confirmButton.setOnAction(_ -> {
            // 将映射结果回写到数据源
            dataSource.clearMappings(); // 先清除所有映射
            mappings.forEach((dataItem, sourceField) -> {
                if (sourceField != null && !sourceField.isEmpty()) {
                    dataSource.addMapping(dataItem.getCode(), sourceField);
                }
            });
            confirmed = true;
            close();
        });

        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        return buttonBox;
    }

    /**
     * 处理自动映射按钮点击事件
     * 根据字段名称相似度自动建立映射关系
     */
    private void handleAutoMap() {
        // 遍历所有数据项
        for (DataItem dataItem : DataFactory.getInstance().getDataItems()) {
            // 如果该数据项已经有映射,则跳过
            if (mappings.containsKey(dataItem)) {
                continue;
            }

            // 获取数据项的代码和昵称
            String itemCode = dataItem.getCode().toLowerCase();
            String itemNick = dataItem.getNick() != null ? dataItem.getNick().toLowerCase() : "";

            // 遍历数据源字段,寻找最匹配的字段
            String bestMatch = null;
            for (String sourceField : sourceColumns) {
                String fieldLower = sourceField.toLowerCase();
                // 完全匹配代码
                if (fieldLower.equals(itemCode)) {
                    bestMatch = sourceField;
                    break;
                }
                // 完全匹配昵称
                if (!itemNick.isEmpty() && fieldLower.equals(itemNick)) {
                    bestMatch = sourceField;
                    break;
                }
                // 包含代码
                if (fieldLower.contains(itemCode) || itemCode.contains(fieldLower)) {
                    bestMatch = sourceField;
                }
                // 包含昵称
                if (!itemNick.isEmpty() && (fieldLower.contains(itemNick) || itemNick.contains(fieldLower))) {
                    bestMatch = sourceField;
                }
            }

            // 如果找到匹配的字段,建立映射关系
            if (bestMatch != null) {
                mappings.put(dataItem, bestMatch);
            }
        }

        // 刷新表格显示
        @SuppressWarnings("unchecked")
        TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
        if (table != null) {
            table.refresh();
        }

        // 显示提示信息
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText("自动映射完成");
        alert.showAndWait();
    }

    /**
     * 处理重建映射按钮点击事件
     * 根据数据源字段创建新的映射关系
     */
    private void handleRebuild() {
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认重建映射");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("重建映射将根据数据源字段创建新的数据项。是否继续？");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 获取现有数据项的code列表
            List<String> existingCodes = DataFactory.getInstance().getDataItems().stream()
                    .map(DataItem::getCode)
                    .collect(Collectors.toList());
            // 根据数据源字段创建新的数据项
            for (String column : sourceColumns) {
                // 如果字段已经存在对应的数据项，则跳过
                if (existingCodes.contains(column)) {
                    continue;
                }
                DataItem newItem = new DataItem();
                newItem.setCode(column);
                // 添加到数据工厂
                DataFactory.getInstance().getDataItems().add(newItem);
                // 建立映射关系
                mappings.put(newItem, column);
            }
            // 刷新表格显示
            @SuppressWarnings("unchecked")
            TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
            if (table != null) {
                table.refresh();
            }
            // 显示完成提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("重建映射完成");
            alert.showAndWait();
        }
    }

    /**
     * 获取映射结果
     * 如果用户点击了确认按钮,返回配置好的映射关系
     * 否则返回null表示用户取消了操作
     * 
     * @return 数据项与数据源字段的映射关系,取消操作时返回null
     */
    public Map<DataItem, String> getMappings() {
        return confirmed ? mappings : null;
    }
}