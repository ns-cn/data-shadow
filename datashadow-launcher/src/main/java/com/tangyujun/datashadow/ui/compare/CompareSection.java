package com.tangyujun.datashadow.ui.compare;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import java.util.List;
import java.util.logging.Logger;
import com.tangyujun.datashadow.core.DataItemChangeListener;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.ui.compare.helper.CompareEngine;
import com.tangyujun.datashadow.ui.compare.helper.CompareResultExporter;
import com.tangyujun.datashadow.ui.compare.helper.CompareTableHelper;
import com.tangyujun.datashadow.ui.compare.helper.DialogHelper;
import com.tangyujun.datashadow.ui.compare.model.CompareResult;
import com.tangyujun.datashadow.ui.compare.model.FilterModel;
import com.tangyujun.datashadow.ui.compare.model.ResultExportCallback;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.io.File;
import java.util.logging.Level;

/**
 * 对比功能与结果展示区
 * 包含对比操作和结果展示的完整功能区域
 * 
 * 主要功能:
 * 1. 执行主数据源和影子数据源的数据对比
 * 2. 展示对比结果,支持多种过滤模式
 * 3. 支持数据项名称和别名两种显示模式
 * 4. 支持导出对比结果为CSV、Excel和JSON格式
 * 
 * 界面组成:
 * 1. 标题区 - 显示功能区标题
 * 2. 工具栏 - 包含对比按钮、过滤模式选择、显示模式选择和导出按钮
 * 3. 结果表格 - 以表格形式展示对比结果
 * 
 * 交互功能:
 * 1. 执行对比 - 点击对比按钮触发数据对比操作
 * 2. 过滤显示 - 通过下拉框选择不同的过滤模式
 * 3. 切换显示 - 支持数据项代码和别名两种显示方式
 * 4. 导出结果 - 支持多种格式导出对比结果
 * 
 * 数据处理:
 * 1. 自动监听数据项变化并更新显示
 * 2. 支持动态加载和更新数据
 * 3. 提供数据验证和错误处理机制
 */
public class CompareSection extends VBox implements DataItemChangeListener {

    /** 日志记录器 - 用于记录组件运行时的日志信息 */
    private static final Logger log = Logger.getLogger(CompareSection.class.getName());

    /**
     * 表头显示模式常量定义
     * HEADER_MODE_CODE - 使用数据项的原始代码作为列标题
     * HEADER_MODE_NICK - 优先使用数据项的别名作为列标题,如无别名则使用代码
     */
    private static final String HEADER_MODE_CODE = "数据项名称";
    private static final String HEADER_MODE_NICK = "数据项别名优先";

    /** 对比结果展示表格 - 用于展示数据对比的详细结果 */
    private final TableView<CompareResult> resultTable;
    /** 执行对比按钮 - 触发数据对比操作 */
    private final Button compareButton;
    /** 过滤模式选择下拉框 - 用于选择不同的数据过滤方式 */
    private final ComboBox<FilterModel> filterMode;
    /** 表头显示模式选择下拉框 - 用于切换列标题的显示方式 */
    private final ComboBox<String> headerDisplayMode;
    /** CSV格式导出按钮 - 将对比结果导出为CSV文件 */
    private final Button exportCsvButton;
    /** Excel格式导出按钮 - 将对比结果导出为Excel文件 */
    private final Button exportExcelButton;
    /** JSON格式导出按钮 - 将对比结果导出为JSON文件 */
    private final Button exportJsonButton;

    /** 结果导出工具 - 处理不同格式的结果导出功能 */
    private CompareResultExporter exporter;

    /**
     * 构造函数
     * 初始化界面组件并设置布局
     * 
     * 初始化流程:
     * 1. 创建标题和工具栏
     * - 设置标题样式和布局
     * - 配置工具栏组件和布局
     * 2. 初始化对比结果表格
     * - 创建表格容器
     * - 设置表格样式和布局属性
     * 3. 设置各组件事件处理
     * - 配置按钮点击事件
     * - 设置下拉框选择事件
     * 4. 注册数据项变化监听
     * - 监听数据项变化以更新显示
     * 
     * 布局设置:
     * 1. 使用VBox作为主容器,设置间距和边框
     * 2. 使用HBox组织工具栏组件
     * 3. 合理分配组件大小和空间
     */
    public CompareSection() {
        super(5);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 设置CompareSection自身的布局属性
        setMaxHeight(Double.MAX_VALUE);
        setMinHeight(200); // 设置最小高度

        // 创建标题
        Label title = new Label("对比功能与结果");
        title.setStyle("-fx-font-weight: bold;");
        title.setMinHeight(Region.USE_PREF_SIZE);

        // 工具栏布局
        HBox toolBox = new HBox(10);
        toolBox.setAlignment(Pos.CENTER_LEFT);
        toolBox.setPadding(new Insets(0, 0, 10, 0));
        toolBox.setMinHeight(Region.USE_PREF_SIZE);

        // 创建左侧功能区
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);

        compareButton = new Button("执行对比");
        compareButton.setPrefWidth(100);

        filterMode = new ComboBox<>();
        filterMode.setItems(FXCollections.observableArrayList(FilterModel.values()));
        filterMode.setValue(FilterModel.ALL_DIFF);
        filterMode.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FilterModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });
        filterMode.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FilterModel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.getDisplayName());
            }
        });

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

        // 创建表格容器
        VBox tableContainer = new VBox();
        tableContainer.setStyle("-fx-border-color: #ddd;");
        tableContainer.setMinHeight(100); // 设置最小高度
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        // 结果表格设置
        resultTable = new TableView<>();
        resultTable.setStyle("-fx-border-width: 0;");
        resultTable.setMinHeight(100); // 设置最小高度
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        tableContainer.getChildren().add(resultTable);

        // 将所有组件添加到主容器，并设置合适的间距
        getChildren().addAll(title, toolBox, tableContainer);
        setSpacing(5); // 设置组件之间的间距

        // 设置按钮事件
        compareButton.setOnAction(event -> startCompare());
        filterMode.setOnAction(event -> filterDiffItems());
        headerDisplayMode.setOnAction(event -> {
            List<DataItem> dataItems = DataFactory.getInstance().getDataItems();
            updateColumns(dataItems);
        });

        // 延迟初始化导出工具，直到组件被添加到Scene中
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // 初始化导出工具
                exporter = new CompareResultExporter(resultTable, newScene.getWindow(),
                        new ResultExportCallback() {
                            @Override
                            public void onExportSuccess(String message, File file) {
                                DialogHelper.showSuccessDialog("导出成功", message, file);
                            }

                            @Override
                            public void onExportError(String message) {
                                DialogHelper.showAlert("导出失败", message);
                            }
                        });

                // 设置导出按钮事件处理
                exportCsvButton.setOnAction(event -> exporter.exportToCsv());
                exportExcelButton.setOnAction(event -> exporter.exportToExcel());
                exportJsonButton.setOnAction(event -> exporter.exportToJson());
            }
        });

        // 注册为数据项变化监听器
        DataFactory.getInstance().addDataItemChangeListener(this);
    }

    /**
     * 更新表格列
     * 根据当前数据项列表和显示模式重新创建表格列
     * 
     * @param dataItems 数据项列表,用于创建对应的表格列
     */
    public void updateColumns(List<DataItem> dataItems) {
        CompareTableHelper.updateColumns(resultTable, dataItems, headerDisplayMode.getValue());
    }

    /**
     * 开始数据对比
     * 执行主数据源和影子数据源的数据对比操作
     * 
     * 处理流程:
     * 1. 验证对比前置条件
     * 2. 禁用对比按钮防止重复操作
     * 3. 执行数据对比
     * 4. 根据过滤模式显示结果
     * 5. 处理可能的异常情况
     * 6. 恢复对比按钮状态
     */
    private void startCompare() {
        if (!validateComparePrerequisites()) {
            return;
        }
        compareButton.setDisable(true);
        try {
            ObservableList<CompareResult> results = FXCollections.observableArrayList();
            resultTable.setItems(results);

            DataSource primary = DataFactory.getInstance().getPrimaryDataSource();
            DataSource shadow = DataFactory.getInstance().getShadowDataSource();
            List<DataItem> dataItems = DataFactory.getInstance().getDataItems();

            CompareEngine.compare(primary, shadow, dataItems, results);
            filterDiffItems();

        } catch (DataAccessException e) {
            log.log(Level.SEVERE, "执行对比时发生错误: {0}", e.getMessage());
            DialogHelper.showAlert("对比失败", "执行对比时发生错误：" + e.getMessage());
        } finally {
            compareButton.setDisable(false);
        }

    }

    /**
     * 验证对比前置条件
     * 检查是否满足执行数据对比的必要条件
     * 
     * 验证项目:
     * 1. 主数据源和影子数据源是否已配置
     * 2. 是否已添加数据项
     * 3. 是否已配置主键数据项
     * 
     * @return 如果满足所有条件返回true,否则返回false
     */
    private boolean validateComparePrerequisites() {
        DataFactory factory = DataFactory.getInstance();
        if (!factory.validateComparePrerequisites()) {
            String errorMessage = factory.getValidationErrorMessage();
            DialogHelper.showAlert("无法执行对比", errorMessage);
            return false;
        }
        return true;
    }

    /**
     * 过滤差异项
     * 根据当前选择的过滤模式设置表格数据显示
     * 
     * 过滤模式说明:
     * - 全部数据: 显示所有数据行
     * - 所有差异项: 显示所有存在差异的数据行
     * - 仅主数据源: 仅显示主数据源中存在的数据行
     * - 仅主数据源差异项: 仅显示主数据源中存在差异的数据行
     * - 仅影子数据源: 仅显示影子数据源中存在的数据行
     * - 仅影子数据源差异项: 仅显示影子数据源中存在差异的数据行
     */
    private void filterDiffItems() {
        @SuppressWarnings("unchecked")
        ObservableList<CompareResult> baseItems = resultTable.getItems() instanceof FilteredList<?>
                ? (ObservableList<CompareResult>) (ObservableList<?>) ((FilteredList<?>) resultTable.getItems())
                        .getSource()
                : (ObservableList<CompareResult>) resultTable.getItems();

        FilteredList<CompareResult> filteredData = new FilteredList<>(baseItems);

        switch (filterMode.getValue()) {
            case ALL -> filteredData.setPredicate(result -> true);
            case ALL_DIFF -> filteredData.setPredicate(CompareResult::hasDifferences);
            case PRIMARY -> filteredData.setPredicate(result -> result.getCellResults().values().stream()
                    .anyMatch(cell -> cell.getPrimaryValue() != null));
            case PRIMARY_DIFF -> filteredData.setPredicate(result -> result.getCellResults().values()
                    .stream().anyMatch(cell -> cell.getPrimaryValue() != null && cell.isDifferent()));
            case SHADOW -> filteredData.setPredicate(result -> result.getCellResults().values().stream()
                    .anyMatch(cell -> cell.getShadowValue() != null));
            case SHADOW_DIFF -> filteredData.setPredicate(result -> result.getCellResults().values()
                    .stream().anyMatch(cell -> cell.getShadowValue() != null && cell.isDifferent()));
        }

        resultTable.setItems(filteredData);
    }

    /**
     * 启用对比功能
     * 激活对比按钮,允许用户执行对比操作
     * 通常在数据源配置完成后调用
     */
    public void enableCompare() {
        compareButton.setDisable(false);
    }

    /**
     * 禁用对比功能
     * 禁用对比按钮,防止用户执行对比操作
     * 通常在数据源配置不完整时调用
     */
    public void disableCompare() {
        compareButton.setDisable(true);
    }

    /**
     * 更新对比结果
     * 清空当前表格数据并显示新的对比结果
     * 
     * @param data 新的对比结果数据列表,如果为null则仅清空表格
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
     * @param cell        表格单元格对象
     * @param isDifferent 是否为差异值,true表示存在差异需要特殊标记
     */
    @SuppressWarnings("unused")
    private void setCellStyle(TableCell<Object, String> cell, boolean isDifferent) {
        if (isDifferent) {
            cell.setStyle("-fx-text-fill: red; -fx-alignment: center;");
        } else {
            cell.setStyle("-fx-alignment: center;");
        }
    }

    /**
     * 数据项变化事件处理
     * 当数据项发生任何变化时更新表格列
     */
    @Override
    public void onDataItemChanged() {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    /**
     * 数据项创建事件处理
     * 当新建数据项时更新表格列
     * 
     * @param item 新创建的数据项
     */
    @Override
    public void onDataItemCreated(DataItem item) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    /**
     * 数据项更新事件处理
     * 当数据项被更新时更新表格列
     * 
     * @param index   更新的数据项索引
     * @param oldItem 更新前的数据项
     * @param newItem 更新后的数据项
     */
    @Override
    public void onDataItemUpdated(int index, DataItem oldItem, DataItem newItem) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }

    /**
     * 数据项删除事件处理
     * 当数据项被删除时更新表格列
     * 
     * @param index 被删除的数据项索引
     * @param item  被删除的数据项
     */
    @Override
    public void onDataItemDeleted(int index, DataItem item) {
        log.info("数据项发生变更,重新加载表格列");
        updateColumns(DataFactory.getInstance().getDataItems());
    }
}