package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.HashMap;

import com.tangyujun.datashadow.core.DataItemChangeListener;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.exception.DataAccessException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;

/**
 * 对比功能与结果展示区
 * 包含对比操作和结果展示的完整功能区域
 */
public class CompareSection extends VBox implements DataItemChangeListener {

    private static final Logger log = Logger.getLogger(CompareSection.class.getName());

    private final TableView<CompareResult> resultTable;
    private final Button compareButton;
    private final CheckBox showDiffOnly;
    private final CheckBox preferNickname;

    /**
     * 构造函数
     */
    public CompareSection() {
        super(10);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 创建标题
        Label title = new Label("对比功能与结果");
        title.setStyle("-fx-font-weight: bold;");

        // 创建功能按钮区域
        HBox toolBox = new HBox(10);
        toolBox.setAlignment(Pos.CENTER_LEFT);
        toolBox.setPadding(new Insets(0, 0, 10, 0));

        compareButton = new Button("执行对比");
        compareButton.setPrefWidth(100);

        showDiffOnly = new CheckBox("仅显示差异项");
        preferNickname = new CheckBox("优先显示数据项别名");

        toolBox.getChildren().addAll(
                compareButton,
                showDiffOnly,
                preferNickname);

        // 创建结果表格
        resultTable = new TableView<>();
        VBox.setVgrow(resultTable, Priority.ALWAYS);
        resultTable.setStyle("-fx-border-color: #ddd;");

        // 添加所有组件到主容器
        getChildren().addAll(title, toolBox, resultTable);

        // 设置按钮事件
        compareButton.setOnAction(event -> startCompare());
        showDiffOnly.setOnAction(event -> filterDiffItems());
        preferNickname.setOnAction(event -> updateColumnHeaders());

        // 注册为数据项变化监听器
        DataFactory.getInstance().addDataItemChangeListener(this);
    }

    /**
     * 更新表格列
     * 根据数据项列表动态创建表格列
     * 
     * @param dataItems 数据项列表
     */
    public void updateColumns(List<DataItem> dataItems) {
        resultTable.getColumns().clear();

        if (dataItems != null) {
            for (DataItem item : dataItems) {
                TableColumn<CompareResult, String> column = new TableColumn<>(getColumnHeader(item));
                column.setId(item.getCode());

                // 设置列宽
                column.setPrefWidth(150);

                // 修改这里：正确设置 cellValueFactory
                column.setCellValueFactory(cellData -> {
                    String value = cellData.getValue().get(item.getCode());
                    return new SimpleStringProperty(value != null ? value : "");
                });

                // 设置单元格工厂
                column.setCellFactory(col -> new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else if (item.contains("❌")) {
                            setText(item);
                            setStyle("-fx-text-fill: red;");
                        } else {
                            setText(item);
                            setStyle("");
                        }
                    }
                });

                resultTable.getColumns().add(column);
            }
        }
    }

    /**
     * 获取列标题
     * 根据是否优先显示别名的设置返回对应的显示文本
     * 
     * @param item 数据项
     * @return 列标题文本
     */
    private String getColumnHeader(DataItem item) {
        if (preferNickname.isSelected() && item.getNick() != null && !item.getNick().isEmpty()) {
            return item.getNick();
        }
        return item.getCode();
    }

    /**
     * 更新列标题
     * 在切换优先显示别名选项时调用
     */
    private void updateColumnHeaders() {
        List<DataItem> dataItems = DataFactory.getInstance().getDataItems();
        for (TableColumn<CompareResult, ?> column : resultTable.getColumns()) {
            String columnId = column.getId();
            dataItems.stream()
                    .filter(item -> item.getCode().equals(columnId))
                    .findFirst()
                    .ifPresent(item -> column.setText(getColumnHeader(item)));
        }
    }

    /**
     * 开始数据对比
     */
    @SuppressWarnings("LoggerStringConcat")
    private void startCompare() {
        DataSource primary = DataFactory.getInstance().getPrimaryDataSource();
        DataSource shadow = DataFactory.getInstance().getShadowDataSource();

        if (primary == null || shadow == null) {
            showAlert("请先配置数据源", "主数据源和影子数据源都必须配置后才能执行对比");
            return;
        }

        // 获取数据项列表
        List<DataItem> dataItems = DataFactory.getInstance().getDataItems();
        if (dataItems.isEmpty()) {
            showAlert("无法执行对比", "请先添加数据项");
            return;
        }

        // 检查是否配置了主键
        List<DataItem> uniqueItems = dataItems.stream()
                .filter(DataItem::isUnique)
                .toList();
        if (uniqueItems.isEmpty()) {
            showAlert("无法执行对比", "请先配置主键数据项");
            return;
        }

        // 禁用对比按钮，避免重复操作
        compareButton.setDisable(true);

        try {
            // 创建新的 ObservableList 来存储结果
            ObservableList<CompareResult> results = FXCollections.observableArrayList();

            // 直接设置新的列表，而不是清空现有列表
            resultTable.setItems(results);

            // 获取两个数据源的数据和映射关系
            List<Map<String, Object>> primaryData = primary.getValues();
            Map<String, String> primaryMapping = primary.getMappings();

            List<Map<String, Object>> shadowData = shadow.getValues();
            Map<String, String> shadowMapping = shadow.getMappings();

            // 创建结果数据结构
            // ObservableList<CompareResult> results = FXCollections.observableArrayList();

            // 根据唯一标识字段进行数据匹配和对比
            Map<Object, Map<String, Object>> shadowMap = new HashMap<>();

            // 将影子数据转换为Map以便快速查找，同时应用字段映射
            for (Map<String, Object> shadowRow : shadowData) {
                Map<String, Object> mappedRow = new HashMap<>();
                for (DataItem item : dataItems) {
                    String mappedField = shadowMapping.get(item.getCode());
                    if (mappedField != null) {
                        mappedRow.put(item.getCode(), shadowRow.get(mappedField));
                    }
                }
                String key = buildUniqueKey(mappedRow, uniqueItems);
                shadowMap.put(key, mappedRow);
            }

            // 遍历主数据源进行对比，同时应用字段映射
            for (Map<String, Object> primaryRow : primaryData) {
                Map<String, Object> mappedPrimaryRow = new HashMap<>();
                for (DataItem item : dataItems) {
                    String mappedField = primaryMapping.get(item.getCode());
                    if (mappedField != null) {
                        mappedPrimaryRow.put(item.getCode(), primaryRow.get(mappedField));
                    }
                }

                String key = buildUniqueKey(mappedPrimaryRow, uniqueItems);
                Map<String, Object> shadowRow = shadowMap.remove(key);

                CompareResult result = new CompareResult();

                // 遍历所有数据项进行对比
                for (DataItem item : dataItems) {
                    Object primaryValue = mappedPrimaryRow.get(item.getCode());
                    Object shadowValue = shadowRow != null ? shadowRow.get(item.getCode()) : null;

                    if (shadowRow == null) {
                        result.put(item.getCode(), formatDifference(primaryValue, ""));
                    } else if (!Objects.equals(primaryValue, shadowValue)) {
                        result.put(item.getCode(), formatDifference(primaryValue, shadowValue));
                    } else {
                        result.put(item.getCode(), primaryValue != null ? primaryValue.toString() : "");
                    }
                }

                results.add(result);
            }

            // 处理仅在影子数据源中存在的记录
            for (Map<String, Object> shadowRow : shadowMap.values()) {
                CompareResult result = new CompareResult();
                for (DataItem item : dataItems) {
                    Object shadowValue = shadowRow.get(item.getCode());
                    result.put(item.getCode(), formatDifference("", shadowValue));
                }
                results.add(result);
            }

            // 更新表格数据和过滤器
            if (showDiffOnly.isSelected()) {
                FilteredList<CompareResult> filteredData = new FilteredList<>(results);
                filteredData.setPredicate(result -> result.values().stream().anyMatch(value -> value.contains("❌")));
                resultTable.setItems(filteredData);
            } else {
                resultTable.setItems(results);
            }

        } catch (DataAccessException e) {
            log.severe("执行对比时发生错误: " + e.getMessage());
            showAlert("对比失败", "执行对比时发生错误：" + e.getMessage());
        } finally {
            // 恢复对比按钮
            compareButton.setDisable(false);
        }
    }

    /**
     * 构建唯一键
     * 根据唯一标识字段构建用于匹配的键
     */
    private String buildUniqueKey(Map<String, Object> row, List<DataItem> uniqueItems) {
        return uniqueItems.stream()
                .map(item -> String.valueOf(row.get(item.getCode())))
                .collect(Collectors.joining("_"));
    }

    /**
     * 格式化差异值显示
     */
    private String formatDifference(Object primary, Object shadow) {
        return primary + " ❌ " + shadow;
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 过滤差异项
     */
    private void filterDiffItems() {
        @SuppressWarnings("unchecked")
        ObservableList<CompareResult> baseItems = resultTable.getItems() instanceof FilteredList<?>
                ? (ObservableList<CompareResult>) (ObservableList<?>) ((FilteredList<?>) resultTable.getItems())
                        .getSource()
                : (ObservableList<CompareResult>) resultTable.getItems();

        if (showDiffOnly.isSelected()) {
            FilteredList<CompareResult> filteredData = new FilteredList<>(baseItems);
            filteredData.setPredicate(result -> result.values().stream().anyMatch(value -> value.contains("❌")));
            resultTable.setItems(filteredData);
        } else {
            resultTable.setItems(baseItems);
        }
    }

    /**
     * 比对结果数据类
     */
    private static class CompareResult extends HashMap<String, String> {
        // 使用 HashMap 存储每个数据项的对比结果
        // key 为数据项的 code，value 为显示的文本
    }

    /**
     * 启用对比功能
     */
    public void enableCompare() {
        compareButton.setDisable(false);
    }

    /**
     * 禁用对比功能
     */
    public void disableCompare() {
        compareButton.setDisable(true);
    }

    /**
     * 更新对比结果
     * 
     * @param data 对比结果数据
     */
    public void updateResults(List<CompareResult> data) {
        resultTable.getItems().clear();
        if (data != null) {
            resultTable.getItems().addAll(data);
        }
    }

    /**
     * 设置差异值的单元格样式
     * 
     * @param cell        表格单元格
     * @param isDifferent 是否为差异值
     */
    @SuppressWarnings("unused")
    private void setCellStyle(TableCell<Object, String> cell, boolean isDifferent) {
        if (isDifferent) {
            cell.setStyle("-fx-text-fill: red;");
        } else {
            cell.setStyle("");
        }
    }

    @Override
    public void onDataItemChanged() {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    @Override
    public void onDataItemCreated(DataItem item) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    @Override
    public void onDataItemUpdated(int index, DataItem oldItem, DataItem newItem) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    @Override
    public void onDataItemDeleted(int index, DataItem item) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }
}