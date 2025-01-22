package com.tangyujun.datashadow.ui.compare;

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
import java.util.ArrayList;

import com.tangyujun.datashadow.core.DataItemChangeListener;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.opencsv.CSVWriter;
import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.exception.DataAccessException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Desktop;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;

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
     * 获取单元格显示内容
     * 根据单元格结果生成显示内容
     * 
     * @param cellResult 单元格结果对象
     * @return 格式化后的显示内容
     */
    private String getCellDisplayValue(CellResult cellResult) {
        if (cellResult == null) {
            return "";
        }

        String primaryStr = cellResult.getPrimaryValue() != null ? cellResult.getPrimaryValue().toString() : "";
        String shadowStr = cellResult.getShadowValue() != null ? cellResult.getShadowValue().toString() : "";

        if (cellResult.isDifferent()) {
            return primaryStr + " ❌ " + shadowStr;
        } else {
            return primaryStr;
        }
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

                // 设置值工厂
                column.setCellValueFactory(cellData -> {
                    CellResult cellResult = cellData.getValue().getCellResult(dataItem.getCode());
                    return new SimpleStringProperty(getCellDisplayValue(cellResult));
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
     * 显示成功对话框并提供打开文件选项
     * 
     * @param title   标题
     * @param content 内容
     * @param file    导出的文件
     */
    private void showSuccessDialog(String title, String content, File file) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);

        ButtonType openButton = new ButtonType("打开文件");
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(openButton, closeButton);

        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == openButton) {
                try {
                    Desktop.getDesktop().open(file);
                } catch (IOException e) {
                    log.severe("打开文件失败: " + e.getMessage());
                    showAlert("打开失败", "无法打开文件：" + e.getMessage());
                }
            }
        });
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
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存CSV文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                // 获取当前表格数据
                ObservableList<CompareResult> data = resultTable.getItems();
                if (data == null || data.isEmpty()) {
                    showAlert("导出失败", "没有可导出的数据");
                    return;
                }

                // 写入表头 - 使用当前表格显示的列标题
                String[] headers = resultTable.getColumns().stream()
                        .map(TableColumn::getText)
                        .toArray(String[]::new);
                writer.writeNext(headers);

                // 写入数据行 - 使用当前表格显示的内容
                for (CompareResult row : data) {
                    String[] rowData = resultTable.getColumns().stream()
                            .map(column -> {
                                CellResult cellResult = row.getCellResult(column.getId());
                                return getCellDisplayValue(cellResult);
                            })
                            .toArray(String[]::new);
                    writer.writeNext(rowData);
                }

                log.info("成功导出CSV文件到: " + file.getAbsolutePath());
                showSuccessDialog("导出成功", "CSV文件已成功导出到：" + file.getAbsolutePath(), file);
            } catch (IOException e) {
                log.severe("导出CSV文件失败: " + e.getMessage());
                showAlert("导出失败", "导出CSV文件时发生错误：" + e.getMessage());
            }
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
            try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                // 获取当前表格数据
                ObservableList<CompareResult> data = resultTable.getItems();
                if (data == null || data.isEmpty()) {
                    showAlert("导出失败", "没有可导出的数据");
                    return;
                }

                XSSFSheet sheet = workbook.createSheet("对比结果");

                // 创建标题行样式
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // 创建差异单元格样式
                CellStyle diffStyle = workbook.createCellStyle();
                diffStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
                diffStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // 写入表头
                XSSFRow headerRow = sheet.createRow(0);
                List<TableColumn<CompareResult, ?>> columns = resultTable.getColumns();
                for (int i = 0; i < columns.size(); i++) {
                    XSSFCell cell = headerRow.createCell(i);
                    cell.setCellValue(columns.get(i).getText());
                    cell.setCellStyle(headerStyle);
                }

                // 写入数据行
                int rowNum = 1;
                for (CompareResult row : data) {
                    XSSFRow excelRow = sheet.createRow(rowNum++);
                    for (int i = 0; i < columns.size(); i++) {
                        XSSFCell cell = excelRow.createCell(i);
                        TableColumn<CompareResult, ?> column = columns.get(i);
                        CellResult cellResult = row.getCellResult(column.getId());

                        // 设置单元格值
                        cell.setCellValue(getCellDisplayValue(cellResult));

                        // 如果是差异项，应用差异样式
                        if (cellResult != null && cellResult.isDifferent()) {
                            cell.setCellStyle(diffStyle);
                        }
                    }
                }

                // 自动调整列宽
                for (int i = 0; i < columns.size(); i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入文件
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    workbook.write(fileOut);
                }

                log.info("成功导出Excel文件到: " + file.getAbsolutePath());
                showSuccessDialog("导出成功", "Excel文件已成功导出到：" + file.getAbsolutePath(), file);
            } catch (IOException e) {
                log.severe("导出Excel文件失败: " + e.getMessage());
                showAlert("导出失败", "导出Excel文件时发生错误：" + e.getMessage());
            }
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
            try {
                // 获取当前表格数据
                ObservableList<CompareResult> data = resultTable.getItems();
                if (data == null || data.isEmpty()) {
                    showAlert("导出失败", "没有可导出的数据");
                    return;
                }
                // 创建JSON数组存储所有行数据
                List<JSONObject> jsonRows = new ArrayList<>();
                // 遍历每一行数据
                for (CompareResult row : data) {
                    JSONObject jsonRow = new JSONObject();
                    // 遍历每一列
                    for (TableColumn<CompareResult, ?> column : resultTable.getColumns()) {
                        CellResult cellResult = row.getCellResult(column.getId());
                        if (cellResult != null) {
                            JSONObject cellData = new JSONObject();
                            cellData.put("primaryValue", cellResult.getPrimaryValue());
                            cellData.put("shadowValue", cellResult.getShadowValue());
                            cellData.put("isDifferent", cellResult.isDifferent());
                            jsonRow.put(column.getText(), cellData);
                        }
                    }
                    jsonRows.add(jsonRow);
                }
                // 将JSON数据写入文件
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(JSON.toJSONString(jsonRows));
                }
                log.info("成功导出JSON文件到: " + file.getAbsolutePath());
            } catch (IOException e) {
                log.severe("导出JSON文件失败: " + e.getMessage());
                showAlert("导出失败", "导出JSON文件时发生错误：" + e.getMessage());
            }
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