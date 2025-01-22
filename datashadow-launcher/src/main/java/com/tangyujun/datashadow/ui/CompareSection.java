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
import javafx.stage.FileChooser;
import java.io.File;

/**
 * 对比功能与结果展示区
 * 包含对比操作和结果展示的完整功能区域
 * 主要功能:
 * 1. 执行主数据源和影子数据源的数据对比
 * 2. 展示对比结果,支持仅显示差异项和显示全部数据
 * 3. 支持数据项名称和别名两种显示模式
 * 4. 支持导出对比结果为CSV、Excel和JSON格式
 */
public class CompareSection extends VBox implements DataItemChangeListener {

    /** 日志记录器 */
    private static final Logger log = Logger.getLogger(CompareSection.class.getName());

    /** 过滤模式常量 - 仅显示存在差异的数据项 */
    private static final String FILTER_MODE_DIFF_ONLY = "仅差异项";
    /** 过滤模式常量 - 显示所有数据项 */
    private static final String FILTER_MODE_ALL = "全部数据";

    /** 表头显示模式常量 - 使用数据项代码作为列标题 */
    private static final String HEADER_MODE_CODE = "数据项名称";
    /** 表头显示模式常量 - 优先使用数据项别名作为列标题 */
    private static final String HEADER_MODE_NICK = "数据项别名优先";

    /** 对比结果展示表格 */
    private final TableView<CompareResult> resultTable;
    /** 执行对比按钮 */
    private final Button compareButton;
    /** 过滤模式选择下拉框 */
    private final ComboBox<String> filterMode;
    /** 表头显示模式选择下拉框 */
    private final ComboBox<String> headerDisplayMode;
    /** CSV导出按钮 */
    private final Button exportCsvButton;
    /** Excel导出按钮 */
    private final Button exportExcelButton;
    /** JSON导出按钮 */
    private final Button exportJsonButton;

    /**
     * 构造函数
     * 初始化界面组件并设置布局
     * 包括:
     * 1. 创建标题和工具栏
     * 2. 初始化对比结果表格
     * 3. 设置各组件事件处理
     * 4. 注册数据项变化监听
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

        // 创建左侧功能区
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        compareButton = new Button("执行对比");
        compareButton.setPrefWidth(100);

        filterMode = new ComboBox<>();
        filterMode.setItems(FXCollections.observableArrayList(FILTER_MODE_DIFF_ONLY, FILTER_MODE_ALL));
        filterMode.setValue(FILTER_MODE_DIFF_ONLY);

        headerDisplayMode = new ComboBox<>();
        headerDisplayMode.setItems(FXCollections.observableArrayList(HEADER_MODE_CODE, HEADER_MODE_NICK));
        headerDisplayMode.setValue(HEADER_MODE_CODE);

        leftBox.getChildren().addAll(compareButton, filterMode, headerDisplayMode);

        // 创建右侧导出按钮区
        HBox rightBox = new HBox(5);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        exportCsvButton = new Button("导出CSV");
        exportExcelButton = new Button("导出Excel");
        exportJsonButton = new Button("导出JSON");

        rightBox.getChildren().addAll(exportCsvButton, exportExcelButton, exportJsonButton);

        // 使用HBox.setHgrow使左侧区域占据剩余空间
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        toolBox.getChildren().addAll(leftBox, rightBox);

        // 创建结果表格
        resultTable = new TableView<>();
        VBox.setVgrow(resultTable, Priority.ALWAYS);
        resultTable.setStyle("-fx-border-color: #ddd;");

        // 添加所有组件到主容器
        getChildren().addAll(title, toolBox, resultTable);

        // 设置按钮事件
        compareButton.setOnAction(event -> startCompare());
        filterMode.setOnAction(event -> filterDiffItems());
        headerDisplayMode.setOnAction(event -> updateColumnHeaders());
        exportCsvButton.setOnAction(event -> exportToCsv());
        exportExcelButton.setOnAction(event -> exportToExcel());
        exportJsonButton.setOnAction(event -> exportToJson());

        // 注册为数据项变化监听器
        DataFactory.getInstance().addDataItemChangeListener(this);
    }

    /**
     * 更新表格列
     * 根据数据项列表动态创建表格列,包括:
     * 1. 清空现有列
     * 2. 为每个数据项创建对应的列
     * 3. 设置列的值工厂和单元格工厂
     * 4. 配置列的显示样式
     * 
     * @param dataItems 数据项列表,用于创建表格列
     */
    public void updateColumns(List<DataItem> dataItems) {
        resultTable.getColumns().clear();

        if (dataItems != null) {
            for (DataItem dataItem : dataItems) {
                TableColumn<CompareResult, String> column = new TableColumn<>(getColumnHeader(dataItem));
                column.setId(dataItem.getCode());

                // 设置列宽
                column.setPrefWidth(150);

                // 修改这里：正确设置 cellValueFactory
                column.setCellValueFactory(cellData -> {
                    CellResult cellResult = cellData.getValue().getCellResult(dataItem.getCode());
                    if (cellResult == null) {
                        return new SimpleStringProperty("");
                    }

                    String primaryStr = cellResult.getPrimaryValue() != null ? cellResult.getPrimaryValue().toString()
                            : "";
                    String shadowStr = cellResult.getShadowValue() != null ? cellResult.getShadowValue().toString()
                            : "";

                    if (cellResult.isDifferent()) {
                        return new SimpleStringProperty(primaryStr + " ❌ " + shadowStr);
                    } else {
                        return new SimpleStringProperty(primaryStr);
                    }
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
                            setStyle("-fx-text-fill: red; -fx-alignment: center;");
                        } else {
                            setText(item);
                            setStyle("-fx-alignment: center;");
                        }
                    }
                });

                resultTable.getColumns().add(column);
            }
        }
    }

    /**
     * 获取列标题
     * 根据当前表头显示模式返回适当的列标题:
     * - 如果选择别名优先且数据项有别名,则返回别名
     * - 否则返回数据项代码
     * 
     * @param item 数据项对象
     * @return 根据显示模式确定的列标题文本
     */
    private String getColumnHeader(DataItem item) {
        if (headerDisplayMode.getValue().equals(HEADER_MODE_NICK) && item.getNick() != null
                && !item.getNick().isEmpty()) {
            return item.getNick();
        }
        return item.getCode();
    }

    /**
     * 更新列标题
     * 在切换表头显示模式时更新所有列的标题:
     * 1. 获取当前所有数据项
     * 2. 遍历表格列
     * 3. 根据新的显示模式更新每列标题
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
     * 执行主数据源和影子数据源的数据对比操作:
     * 1. 验证数据源和数据项配置
     * 2. 获取两个数据源的数据
     * 3. 根据主键匹配记录并比对
     * 4. 生成对比结果并更新表格显示
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
                    CellResult cellResult = new CellResult();
                    cellResult.setPrimaryValue(primaryValue);
                    cellResult.setShadowValue(shadowValue);
                    // 检查比较器是否为空
                    if (item.getComparator() != null) {
                        cellResult.setDifferent(!item.getComparator().equals(primaryValue, shadowValue));
                    } else {
                        // 如果比较器为空,则使用Objects.equals进行比较
                        cellResult.setDifferent(!Objects.equals(primaryValue, shadowValue));
                    }
                    result.putCellResult(item.getCode(), cellResult);
                }
                results.add(result);
            }

            // 处理仅在影子数据源中存在的记录
            for (Map<String, Object> shadowRow : shadowMap.values()) {
                CompareResult result = new CompareResult();
                for (DataItem item : dataItems) {
                    Object shadowValue = shadowRow.get(item.getCode());

                    CellResult cellResult = new CellResult();
                    cellResult.setPrimaryValue(null);
                    cellResult.setShadowValue(shadowValue);
                    cellResult.setDifferent(true);

                    result.putCellResult(item.getCode(), cellResult);
                }
                results.add(result);
            }
            // 更新表格数据和过滤器
            if (filterMode.getValue().equals(FILTER_MODE_DIFF_ONLY)) {
                FilteredList<CompareResult> filteredData = new FilteredList<>(results);
                filteredData.setPredicate(result -> result.hasDifferences());
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
     * 将多个主键字段的值拼接成唯一标识字符串
     * 
     * @param row         数据行
     * @param uniqueItems 主键数据项列表
     * @return 由主键值拼接而成的唯一标识字符串
     */
    private String buildUniqueKey(Map<String, Object> row, List<DataItem> uniqueItems) {
        return uniqueItems.stream()
                .map(item -> String.valueOf(row.get(item.getCode())))
                .collect(Collectors.joining("_"));
    }

    /**
     * 显示警告对话框
     * 用于显示操作过程中的警告信息
     * 
     * @param title   警告标题
     * @param content 警告内容
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 过滤差异项
     * 根据当前过滤模式设置表格数据显示:
     * - 仅差异项模式: 只显示存在差异的数据行
     * - 全部数据模式: 显示所有数据行
     */
    private void filterDiffItems() {
        @SuppressWarnings("unchecked")
        ObservableList<CompareResult> baseItems = resultTable.getItems() instanceof FilteredList<?>
                ? (ObservableList<CompareResult>) (ObservableList<?>) ((FilteredList<?>) resultTable.getItems())
                        .getSource()
                : (ObservableList<CompareResult>) resultTable.getItems();

        if (filterMode.getValue().equals(FILTER_MODE_DIFF_ONLY)) {
            FilteredList<CompareResult> filteredData = new FilteredList<>(baseItems);
            filteredData.setPredicate(CompareResult::hasDifferences);
            resultTable.setItems(filteredData);
        } else {
            resultTable.setItems(baseItems);
        }
    }

    /**
     * 比对结果数据类
     * 用于存储一行数据的所有字段比对结果
     * 包含每个字段的主数据源值、影子数据源值及其差异状态
     */
    public static class CompareResult {
        /**
         * 存储每个字段的比对结果
         * key为字段编码,value为该字段的比对结果
         */
        private final Map<String, CellResult> cellResults = new HashMap<>();

        /**
         * 添加一个字段的比对结果
         * 
         * @param code   字段编码
         * @param result 字段比对结果
         */
        public void putCellResult(String code, CellResult result) {
            cellResults.put(code, result);
        }

        /**
         * 获取指定字段的比对结果
         * 
         * @param code 字段编码
         * @return 字段比对结果,如果字段不存在则返回null
         */
        public CellResult getCellResult(String code) {
            return cellResults.get(code);
        }

        /**
         * 判断该行数据是否存在差异
         * 
         * @return 如果任一字段存在差异则返回true,否则返回false
         */
        public boolean hasDifferences() {
            return cellResults.values().stream().anyMatch(CellResult::isDifferent);
        }
    }

    /**
     * 单元格比对结果类
     * 用于存储单个单元格的主数据源值和影子数据源值,以及是否存在差异
     * 包含:
     * 1. 主数据源的值
     * 2. 影子数据源的值
     * 3. 是否存在差异的标志
     */
    public static class CellResult {
        /**
         * 主数据源的值
         */
        private Object primaryValue;

        /**
         * 影子数据源的值
         */
        private Object shadowValue;

        /**
         * 是否存在差异
         */
        private boolean isDifferent;

        /**
         * 获取主数据源的值
         * 
         * @return 主数据源的值
         */
        public Object getPrimaryValue() {
            return primaryValue;
        }

        /**
         * 设置主数据源的值
         * 
         * @param primaryValue 主数据源的值
         */
        public void setPrimaryValue(Object primaryValue) {
            this.primaryValue = primaryValue;
        }

        /**
         * 获取影子数据源的值
         * 
         * @return 影子数据源的值
         */
        public Object getShadowValue() {
            return shadowValue;
        }

        /**
         * 设置影子数据源的值
         * 
         * @param shadowValue 影子数据源的值
         */
        public void setShadowValue(Object shadowValue) {
            this.shadowValue = shadowValue;
        }

        /**
         * 判断是否存在差异
         * 
         * @return 如果存在差异返回true,否则返回false
         */
        public boolean isDifferent() {
            return isDifferent;
        }

        /**
         * 设置是否存在差异
         * 
         * @param different 是否存在差异
         */
        public void setDifferent(boolean different) {
            isDifferent = different;
        }
    }

    /**
     * 启用对比功能
     * 激活对比按钮,允许用户执行对比操作
     */
    public void enableCompare() {
        compareButton.setDisable(false);
    }

    /**
     * 禁用对比功能
     * 禁用对比按钮,防止用户执行对比操作
     */
    public void disableCompare() {
        compareButton.setDisable(true);
    }

    /**
     * 更新对比结果
     * 清空当前表格数据并显示新的对比结果
     * 
     * @param data 新的对比结果数据列表
     */
    public void updateResults(List<CompareResult> data) {
        resultTable.getItems().clear();
        if (data != null) {
            resultTable.getItems().addAll(data);
        }
    }

    /**
     * 设置差异值的单元格样式
     * 为差异值设置特殊的显示样式
     * 
     * @param cell        表格单元格
     * @param isDifferent 是否为差异值
     */
    @SuppressWarnings("unused")
    private void setCellStyle(TableCell<Object, String> cell, boolean isDifferent) {
        if (isDifferent) {
            cell.setStyle("-fx-text-fill: red; -fx-alignment: center;");
        } else {
            cell.setStyle("-fx-alignment: center;");
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

    /**
     * 导出为CSV文件
     * 将当前对比结果导出为CSV格式文件
     */
    @SuppressWarnings("LoggerStringConcat")
    private void exportToCsv() {
        File file = showSaveFileDialog("CSV Files", "*.csv");
        if (file != null) {
            // TODO: 实现CSV导出逻辑
            log.info("导出CSV文件到: " + file.getAbsolutePath());
        }
    }

    /**
     * 导出为Excel文件
     * 将当前对比结果导出为Excel格式文件
     */
    @SuppressWarnings("LoggerStringConcat")
    private void exportToExcel() {
        File file = showSaveFileDialog("Excel Files", "*.xlsx");
        if (file != null) {
            // TODO: 实现Excel导出逻辑
            log.info("导出Excel文件到: " + file.getAbsolutePath());
        }
    }

    /**
     * 导出为JSON文件
     * 将当前对比结果导出为JSON格式文件
     */
    @SuppressWarnings("LoggerStringConcat")
    private void exportToJson() {
        File file = showSaveFileDialog("JSON Files", "*.json");
        if (file != null) {
            // TODO: 实现JSON导出逻辑
            log.info("导出JSON文件到: " + file.getAbsolutePath());
        }
    }

    /**
     * 显示文件保存对话框
     * 打开系统文件选择器供用户选择保存位置
     * 
     * @param description 文件类型描述
     * @param extension   文件扩展名
     * @return 选择的文件，如果取消则返回null
     */
    private File showSaveFileDialog(String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extension));
        return fileChooser.showSaveDialog(getScene().getWindow());
    }
}