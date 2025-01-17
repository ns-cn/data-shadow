package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.List;
import java.util.logging.Logger;

import com.tangyujun.datashadow.core.DataItemChangeListener;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.core.DataFactory;

/**
 * 对比功能与结果展示区
 * 包含对比操作和结果展示的完整功能区域
 */
public class CompareSection extends VBox implements DataItemChangeListener {

    private static final Logger log = Logger.getLogger(CompareSection.class.getName());

    private final TableView<Object> resultTable;
    private final Button compareButton;
    private final CheckBox showDiffOnly;
    private final CheckBox preferNickname;
    private final Button importButton;
    private final Button exportButton;

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
        compareButton.setDisable(true); // 初始禁用，等待数据源配置完成

        showDiffOnly = new CheckBox("仅显示差异项");
        preferNickname = new CheckBox("优先显示数据项别名");

        importButton = new Button("导入对比方案");
        exportButton = new Button("导出对比方案");

        toolBox.getChildren().addAll(
                compareButton,
                showDiffOnly,
                preferNickname,
                importButton,
                exportButton);

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
        importButton.setOnAction(event -> importCompareScheme());
        exportButton.setOnAction(event -> exportCompareScheme());

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
                TableColumn<Object, String> column = new TableColumn<>(
                        getColumnHeader(item));
                column.setId(item.getCode());

                // 设置列宽
                column.setPrefWidth(150);

                // 设置单元格值工厂
                column.setCellValueFactory(cellData -> {
                    // TODO: 根据数据项类型和比较器实现单元格值的展示
                    return null;
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
        for (TableColumn<Object, ?> column : resultTable.getColumns()) {
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
    private void startCompare() {
        // TODO: 实现数据对比逻辑
        // 1. 获取两个数据源的数据
        // 2. 执行对比
        // 3. 更新结果表格
    }

    /**
     * 过滤差异项
     */
    private void filterDiffItems() {
        // TODO: 根据复选框状态过滤表格显示
    }

    /**
     * 导入对比方案
     */
    private void importCompareScheme() {
        // TODO: 实现导入对比方案功能
    }

    /**
     * 导出对比方案
     */
    private void exportCompareScheme() {
        // TODO: 实现导出对比方案功能
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
    public void updateResults(List<Object> data) {
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
}