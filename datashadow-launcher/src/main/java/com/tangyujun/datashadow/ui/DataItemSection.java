package com.tangyujun.datashadow.ui;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.ui.dialog.DataItemDialog;
import javafx.collections.ObservableList;
import java.util.Optional;
import com.tangyujun.datashadow.core.DataFactory;

/**
 * 数据项维护区域
 * 用于展示和管理数据项列表,包含数据项的增删改查和排序功能
 */
public class DataItemSection extends VBox {

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

    /**
     * 构造函数
     * 初始化数据项维护区域的界面布局和控件
     */
    public DataItemSection() {
        super(5);
        setPadding(new Insets(10));
        setStyle("-fx-border-color: #ddd; -fx-border-radius: 5;");

        // 获取 DataFactory 中的数据项列表
        this.items = DataFactory.getInstance().getDataItems();

        // 创建标题
        Label title = new Label("数据项维护");
        title.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 5 0;");

        // 创建表格
        table = createTable();
        HBox.setHgrow(table, Priority.ALWAYS);

        // 创建按钮区域
        buttonBox = createButtonBox();

        // 创建水平布局容器
        HBox content = new HBox(10);
        content.setFillHeight(true);
        content.getChildren().addAll(table, buttonBox);

        getChildren().addAll(title, content);

        // 设置表格数据源
        table.setItems(items);
    }

    /**
     * 创建数据项表格
     * 
     * @return 配置好的TableView实例
     */
    private TableView<DataItem> createTable() {
        TableView<DataItem> tableView = new TableView<>();
        tableView.setStyle("-fx-border-color: #ddd;");

        // 设置表格选择模式为多选
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 添加列
        TableColumn<DataItem, Boolean> uniqueColumn = new TableColumn<>("是否唯一");
        uniqueColumn.setPrefWidth(80);
        uniqueColumn.setCellValueFactory(new PropertyValueFactory<>("unique"));
        uniqueColumn.setCellFactory(_ -> new TableCell<DataItem, Boolean>() {
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

        TableColumn<DataItem, String> nameColumn = new TableColumn<>("名称");
        nameColumn.setPrefWidth(150);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("code"));

        TableColumn<DataItem, String> nickColumn = new TableColumn<>("别名");
        nickColumn.setPrefWidth(150);
        nickColumn.setCellValueFactory(new PropertyValueFactory<>("nick"));

        TableColumn<DataItem, String> comparatorColumn = new TableColumn<>("自定义比较器");
        comparatorColumn.setPrefWidth(100);
        comparatorColumn.setCellValueFactory(cellData -> {
            String comparator = cellData.getValue().getComparator();
            return new SimpleStringProperty(comparator != null && !comparator.isEmpty() ? "已设置" : "未设置");
        });

        TableColumn<DataItem, String> remarkColumn = new TableColumn<>("备注");
        remarkColumn.setPrefWidth(200);
        remarkColumn.setCellValueFactory(new PropertyValueFactory<>("remark"));

        // 设置所有列居中对齐
        List.of(uniqueColumn, nameColumn, nickColumn, comparatorColumn, remarkColumn)
                .forEach(column -> column.setStyle("-fx-alignment: CENTER;"));

        List<TableColumn<DataItem, ?>> columns = List.of(uniqueColumn, nameColumn, nickColumn, comparatorColumn,
                remarkColumn);
        tableView.getColumns().addAll(columns);

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
        buttons[0].setOnAction(e -> handleAdd());
        buttons[1].setOnAction(e -> handleEdit());
        buttons[2].setOnAction(e -> handleDelete());
        buttons[3].setOnAction(e -> handleMoveUp());
        buttons[4].setOnAction(e -> handleMoveDown());

        // 设置按钮禁用状态的监听
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
        result.ifPresent(item -> items.add(item));
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
                items.set(index, item);
            });
        }
    }

    /**
     * 处理删除数据项
     * 删除选中的一个或多个数据项,删除前会显示确认对话框
     */
    private void handleDelete() {
        ObservableList<DataItem> selectedItems = table.getSelectionModel().getSelectedItems();
        if (!selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("确认删除");
            alert.setHeaderText(null);
            alert.setContentText("确定要删除选中的" + selectedItems.size() + "个数据项吗？");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                items.removeAll(selectedItems);
            }
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
        }
    }
}