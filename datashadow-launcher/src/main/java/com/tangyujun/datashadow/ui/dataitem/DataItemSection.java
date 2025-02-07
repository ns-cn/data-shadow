package com.tangyujun.datashadow.ui.dataitem;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.ArrayList;

import com.tangyujun.datashadow.dataitem.DataItem;

import javafx.collections.ObservableList;

import java.util.Optional;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.core.DataItemChangeListener;
import com.tangyujun.datashadow.datacomparator.DataComparator;

import javafx.scene.Cursor;
import javafx.scene.layout.Region;
import javafx.scene.input.KeyCode;

/**
 * 数据项维护区域
 * 用于展示和管理数据项列表,包含数据项的增删改查和排序功能
 */
public class DataItemSection extends VBox implements DataItemChangeListener {

    /**
     * 数据项表格控件
     */
    private final TableView<DataItem> table;

    /**
     * 操作按钮容器
     */
    private final VBox buttonBox;

    /**
     * 数据项列表,与DataFactory共享数据
     */
    private final ObservableList<DataItem> items;

    private double dragStartY;
    private double initialHeight;
    /**
     * 最小高度，包含:
     * - 标题栏高度 (约30px)
     * - 表格至少显示5行 (约150px)
     * - 内边距和边框 (30px)
     * - 分隔条高度 (5px)
     */
    private static final double MIN_HEIGHT = 250;

    /**
     * 最大高度，避免占据太多空间
     * 应该至少能显示10行数据
     */
    private static final double MAX_HEIGHT = 600;

    /**
     * 构造函数
     * 初始化数据项维护区域的界面布局和控件
     */
    public DataItemSection() {
        super(5);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 设置初始高度和限制
        setPrefHeight(350);
        setMinHeight(MIN_HEIGHT);
        setMaxHeight(MAX_HEIGHT);

        // 获取 DataFactory 中的数据项列表
        this.items = DataFactory.getInstance().getDataItems();

        // 创建标题
        Label title = new Label("数据项维护");
        title.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5 0;");

        // 创建表格
        table = createTable();
        // 设置表格填充属性
        VBox.setVgrow(table, Priority.ALWAYS);
        HBox.setHgrow(table, Priority.ALWAYS);
        table.setMaxWidth(Double.MAX_VALUE); // 允许表格扩展到最大宽度

        // 创建按钮区域
        buttonBox = createButtonBox();

        // 创建水平布局容器
        HBox content = new HBox(10);
        content.setFillHeight(true);
        VBox.setVgrow(content, Priority.ALWAYS); // 让内容区域在垂直方向填充
        content.getChildren().addAll(table, buttonBox);

        getChildren().addAll(title, content);

        // 设置表格数据源
        table.setItems(items);

        // 添加可拖动的分隔条
        Region dragHandle = createDragHandle();
        getChildren().add(dragHandle);

        // 注册监听器
        DataFactory.getInstance().addDataItemChangeListener(this);
    }

    /**
     * 创建数据项表格
     * 
     * @return 配置好的TableView实例
     */
    private TableView<DataItem> createTable() {
        TableView<DataItem> tableView = new TableView<>();
        tableView.setStyle("-fx-border-color: #ddd;");

        // 设置表格自动调整大小
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setMinWidth(0);

        // 设置表格填充属性
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setMaxWidth(Double.MAX_VALUE);
        tableView.setMaxHeight(Double.MAX_VALUE);

        // 设置表格选择模式为多选
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 添加列
        TableColumn<DataItem, Boolean> uniqueColumn = new TableColumn<>("是否唯一");
        uniqueColumn.setPrefWidth(80);
        uniqueColumn.setCellValueFactory(new PropertyValueFactory<>("unique"));
        uniqueColumn.setCellFactory(column -> new TableCell<DataItem, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✓" : "");
                    setStyle(item ? "-fx-text-fill: green;" : "");
                    setAlignment(Pos.CENTER);
                }
            }
        });
        uniqueColumn.setSortable(false);

        TableColumn<DataItem, String> nameColumn = new TableColumn<>("名称");
        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setSortable(false);

        TableColumn<DataItem, String> nickColumn = new TableColumn<>("别名");
        nickColumn.setPrefWidth(150);
        nickColumn.setCellValueFactory(new PropertyValueFactory<>("nick"));
        nickColumn.setSortable(false);

        TableColumn<DataItem, String> comparatorColumn = new TableColumn<>("比较器");
        comparatorColumn.setPrefWidth(100);
        comparatorColumn.setCellValueFactory(cellData -> {
            DataItem item = cellData.getValue();
            DataComparator comparator = item.getComparator();
            if (comparator == null) {
                return new SimpleStringProperty("未设置");
            }
            String description = comparator.getDescription();
            return new SimpleStringProperty(description != null ? description : "已设置");
        });
        comparatorColumn.setSortable(false);

        TableColumn<DataItem, String> remarkColumn = new TableColumn<>("备注");
        remarkColumn.setPrefWidth(200);
        remarkColumn.setCellValueFactory(new PropertyValueFactory<>("remark"));
        remarkColumn.setSortable(false);

        // 设置所有列居中对齐
        List.of(uniqueColumn, nameColumn, nickColumn, comparatorColumn, remarkColumn)
                .forEach(column -> column.setStyle("-fx-alignment: CENTER;"));

        List<TableColumn<DataItem, ?>> columns = List.of(uniqueColumn, nameColumn, nickColumn, comparatorColumn,
                remarkColumn);
        tableView.getColumns().addAll(columns);

        // 添加双击事件处理
        tableView.setRowFactory(tv -> {
            TableRow<DataItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEdit();
                }
            });
            return row;
        });

        // 添加键盘事件处理
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDelete();
            }
        });

        return tableView;
    }

    /**
     * 创建操作按钮容器
     * 
     * @return 包含所有操作按钮的VBox容器
     */
    private VBox createButtonBox() {
        VBox box = new VBox(5);
        box.setPrefWidth(120);
        box.setMinWidth(120);

        // 创建按钮并添加样式
        Button[] buttons = {
                createButton("新增数据项"),
                createButton("编辑选中项"),
                createButton("删除选中项"),
                createButton("上移"),
                createButton("下移")
        };

        // 添加按钮事件处理
        buttons[0].setOnAction(event -> handleAdd());
        buttons[1].setOnAction(event -> handleEdit());
        buttons[2].setOnAction(event -> handleDelete());
        buttons[3].setOnAction(event -> handleMoveUp());
        buttons[4].setOnAction(event -> handleMoveDown());

        // 设置按钮禁用状态的监听
        table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newSelection) -> {
            boolean hasSelection = newSelection != null;
            boolean singleSelection = table.getSelectionModel().getSelectedItems().size() == 1;
            int selectedIndex = table.getSelectionModel().getSelectedIndex();

            buttons[1].setDisable(!singleSelection);
            buttons[2].setDisable(!hasSelection);
            buttons[3].setDisable(!singleSelection || selectedIndex <= 0);
            buttons[4].setDisable(!singleSelection || selectedIndex >= items.size() - 1);
        });

        box.getChildren().addAll(buttons);
        return box;
    }

    /**
     * 创建标准样式的按钮
     * 
     * @param text 按钮文本
     * @return 配置好的Button实例
     */
    private Button createButton(String text) {
        Button button = new Button(text);
        button.setPrefWidth(120);
        button.setStyle("-fx-margin-bottom: 5;");

        // 默认禁用需要选择才能操作的按钮
        if (!"新增数据项".equals(text)) {
            button.setDisable(true);
        }

        return button;
    }

    /**
     * 处理新增数据项
     * 打开数据项对话框,添加新的数据项
     */
    private void handleAdd() {
        DataItemDialog dialog = new DataItemDialog(null);
        Optional<DataItem> result = dialog.showAndWait();
        result.ifPresent(item -> {
            DataFactory.getInstance().addDataItem(item);
        });
    }

    /**
     * 处理编辑数据项
     * 打开数据项对话框,编辑选中的数据项
     */
    private void handleEdit() {
        DataItem selectedItem = table.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            DataItemDialog dialog = new DataItemDialog(selectedItem);
            Optional<DataItem> result = dialog.showAndWait();
            result.ifPresent(item -> {
                int index = items.indexOf(selectedItem);
                DataFactory.getInstance().updateDataItem(index, item);
            });
        }
    }

    /**
     * 处理删除数据项
     * 删除选中的一个或多个数据项,删除前会显示确认对话框
     */
    private void handleDelete() {
        // 获取选中的数据项
        ObservableList<DataItem> selectedItems = table.getSelectionModel().getSelectedItems();

        // 检查是否有选中的数据项
        if (selectedItems == null || selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("警告");
            alert.setHeaderText(null);
            alert.setContentText("请先选择要删除的数据项");
            alert.showAndWait();
            return;
        }

        // 显示确认对话框
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除选中的数据项吗？");
        confirm.setContentText("此操作不可恢复，请确认。");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 创建要删除的数据项列表的副本
            List<DataItem> itemsToDelete = new ArrayList<>(selectedItems);

            // 从数据工厂中删除数据项
            DataFactory dataFactory = DataFactory.getInstance();
            itemsToDelete.forEach(dataFactory::removeDataItem);

            // 清除选择
            table.getSelectionModel().clearSelection();
        }
    }

    /**
     * 处理上移数据项
     * 将选中的数据项在列表中向上移动一位
     */
    private void handleMoveUp() {
        int selectedIndex = table.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            // 保存当前选中的数据项
            DataItem selectedItem = table.getSelectionModel().getSelectedItem();
            DataItem item = items.remove(selectedIndex);
            items.add(selectedIndex - 1, item);
            // 重新选中并请求焦点
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(selectedItem);
            table.requestFocus();
            DataFactory.getInstance().notifyDataItemChangeListeners();
        }
    }

    /**
     * 处理下移数据项
     * 将选中的数据项在列表中向下移动一位
     */
    private void handleMoveDown() {
        int selectedIndex = table.getSelectionModel().getSelectedIndex();
        if (selectedIndex < items.size() - 1) {
            // 保存当前选中的数据项
            DataItem selectedItem = table.getSelectionModel().getSelectedItem();
            DataItem item = items.remove(selectedIndex);
            items.add(selectedIndex + 1, item);
            // 重新选中并请求焦点
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(selectedItem);
            table.requestFocus();
            DataFactory.getInstance().notifyDataItemChangeListeners();
        }
    }

    /**
     * 创建可拖动的分隔条
     * 
     * @return 分隔条控件
     */
    private Region createDragHandle() {
        Region dragHandle = new Region();
        dragHandle.setPrefHeight(5);
        dragHandle.setStyle("-fx-background-color: transparent;" +
                "-fx-border-width: 1 0 0 0;" +
                "-fx-border-color: #ddd;");
        dragHandle.setCursor(Cursor.V_RESIZE);

        // 鼠标按下时记录初始位置和高度
        dragHandle.setOnMousePressed(event -> {
            dragStartY = event.getSceneY();
            initialHeight = getPrefHeight();
            event.consume();
        });

        // 鼠标拖动时调整高度
        dragHandle.setOnMouseDragged(event -> {
            double dragDelta = event.getSceneY() - dragStartY;
            double newHeight = initialHeight + dragDelta;

            // 获取父容器高度（如果有）
            double parentHeight = getParent() != null ? getParent().getLayoutBounds().getHeight() : 1000;
            // 计算最大可用高度（预留数据源区域的空间）
            double maxAvailableHeight = parentHeight * 0.7; // 最多占用70%的空间
            double actualMaxHeight = Math.min(MAX_HEIGHT, maxAvailableHeight);

            // 限制高度在最小值和最大可用高度之间
            newHeight = Math.max(MIN_HEIGHT, Math.min(actualMaxHeight, newHeight));

            // 设置新高度
            setPrefHeight(newHeight);

            // 请求布局更新
            requestLayout();

            event.consume();
        });

        // 鼠标进入时显示提示
        dragHandle.setOnMouseEntered(event -> {
            dragHandle.setStyle("-fx-background-color: #f0f0f0;" +
                    "-fx-border-width: 1 0 0 0;" +
                    "-fx-border-color: #ddd;");
        });

        // 鼠标离开时恢复样式
        dragHandle.setOnMouseExited(event -> {
            dragHandle.setStyle("-fx-background-color: transparent;" +
                    "-fx-border-width: 1 0 0 0;" +
                    "-fx-border-color: #ddd;");
        });

        return dragHandle;
    }

    @Override
    public void onDataItemChanged() {
        table.refresh();
    }

    @Override
    public void onDataItemCreated(DataItem item) {
        table.refresh();
    }

    @Override
    public void onDataItemUpdated(int index, DataItem oldItem, DataItem newItem) {
        table.refresh();
    }

    @Override
    public void onDataItemDeleted(int index, DataItem item) {
        table.refresh();
    }
}