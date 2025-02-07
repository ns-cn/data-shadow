package com.tangyujun.datashadow.ui.components;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

/**
 * 自定义分组下拉选框组件
 * 提供分组展示的下拉选择功能,支持分组和选项的层级展示
 * 支持鼠标悬停效果、选择事件监听等功能
 * 
 * @param <T> 选项值的类型参数，表示下拉选项的值类型
 */
public class GroupComboBox<T> extends StackPane {
    /**
     * 显示当前选中值的文本框
     */
    private final TextField displayField;

    /**
     * 清除按钮
     */
    private final Button clearButton;

    /**
     * 下拉列表弹出窗口
     */
    private final Popup popup;

    /**
     * 下拉列表内容容器
     */
    private final HBox dropdownContent;

    /**
     * 分组列表容器
     */
    private final VBox groupList;

    /**
     * 选项列表容器
     */
    private final VBox itemList;

    /**
     * 选中值的属性对象
     */
    private final ObjectProperty<T> selectedValueProperty;

    /**
     * 当前选中的分组名称
     */
    private String selectedGroup;

    /**
     * 当前选中的选项名称
     */
    private String selectedName;

    /**
     * 选择事件监听器
     */
    private Consumer<GroupSelectEvent<T>> onSelectListener;

    /**
     * 数据源Map,key为分组名称,value为该分组下的选项Map(选项名称->选项值)
     */
    private Map<String, Map<String, T>> dataMap;

    /**
     * 提示文本
     */
    private String promptText;

    /**
     * 分组列表宽度
     */
    private double groupListWidth = 100;

    /**
     * 选项列表宽度
     */
    private double itemListWidth = 100;

    /**
     * 是否允许清除选择
     */
    private boolean allowClear = true;

    /**
     * 默认构造函数,使用默认提示文本"请选择"
     */
    public GroupComboBox() {
        this("请选择");
    }

    /**
     * 构造函数
     * 
     * @param promptText 提示文本
     */
    public GroupComboBox(String promptText) {
        this.promptText = promptText;
        this.selectedValueProperty = new SimpleObjectProperty<>();

        // 创建显示区域
        HBox displayArea = new HBox();
        displayArea.setAlignment(Pos.CENTER_LEFT);
        displayArea.setStyle("""
                -fx-background-color: linear-gradient(to bottom, #ffffff 0%, #f2f2f2 100%);
                -fx-border-color: #cccccc;
                -fx-border-width: 1;
                -fx-border-radius: 3;
                -fx-background-radius: 3;
                -fx-cursor: hand;
                """);
        displayArea.setPrefHeight(25);

        // 创建文本显示框
        displayField = new TextField();
        displayField.setEditable(false);
        displayField.setPrefWidth(200);
        displayField.setPromptText(promptText);
        displayField.setStyle("""
                -fx-background-color: transparent;
                -fx-background-insets: 0;
                -fx-background-radius: 0;
                -fx-border-width: 0;
                -fx-padding: 0 5;
                -fx-cursor: hand;
                -fx-prompt-text-fill: #999999;
                """);
        HBox.setHgrow(displayField, Priority.ALWAYS);

        // 创建清除按钮
        clearButton = new Button("×");
        clearButton.setStyle("""
                -fx-background-color: transparent;
                -fx-border-width: 0;
                -fx-padding: 0;
                -fx-font-size: 12px;
                -fx-text-fill: #999999;
                -fx-min-width: 20px;
                -fx-pref-width: 20px;
                -fx-max-width: 20px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """);
        clearButton.setAlignment(Pos.CENTER);

        // 添加点击事件
        clearButton.setOnAction(e -> {
            clearSelection();
            e.consume();
        });

        // 添加鼠标悬停效果
        clearButton.setOnMouseEntered(e -> clearButton.setStyle("""
                -fx-background-color: transparent;
                -fx-border-width: 0;
                -fx-padding: 0;
                -fx-font-size: 12px;
                -fx-text-fill: #ff4444;
                -fx-min-width: 20px;
                -fx-pref-width: 20px;
                -fx-max-width: 20px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """));

        clearButton.setOnMouseExited(e -> clearButton.setStyle("""
                -fx-background-color: transparent;
                -fx-border-width: 0;
                -fx-padding: 0;
                -fx-font-size: 12px;
                -fx-text-fill: #999999;
                -fx-min-width: 20px;
                -fx-pref-width: 20px;
                -fx-max-width: 20px;
                -fx-font-weight: bold;
                -fx-cursor: hand;
                """));

        // 监听文本变化来控制清除按钮的可见性
        displayField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean shouldShow = allowClear && newValue != null && !newValue.isEmpty();
            clearButton.setManaged(shouldShow);
            clearButton.setVisible(shouldShow);
        });
        // 初始状态设置为不可见
        clearButton.setVisible(false);
        clearButton.setManaged(false);

        displayArea.getChildren().addAll(displayField, clearButton);

        // 创建下拉内容
        dropdownContent = new HBox(0);
        dropdownContent.setStyle("""
                -fx-background-color: white;
                -fx-border-color: #c4c4c4;
                -fx-border-width: 1;
                -fx-background-radius: 3;
                -fx-border-radius: 3;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 4, 0, 0, 1);
                """);

        // 创建分组列表和选项列表
        groupList = new VBox(0);
        groupList.setStyle("""
                -fx-padding: 2;
                -fx-background-color: white;
                """);
        groupList.setPrefWidth(groupListWidth);

        // 创建分组列表的滚动面板
        ScrollPane groupScroll = new ScrollPane(groupList);
        groupScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        groupScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        groupScroll.setFitToWidth(true);
        groupScroll.setPrefViewportHeight(250);
        groupScroll.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-padding: 0;
                -fx-background-insets: 0;
                """);

        itemList = new VBox(0);
        itemList.setStyle("""
                -fx-padding: 2;
                -fx-background-color: white;
                -fx-border-width: 0 0 0 1;
                -fx-border-color: #e6e6e6;
                """);
        itemList.setVisible(false);
        itemList.setPrefWidth(itemListWidth);

        // 创建选项列表的滚动面板
        ScrollPane itemScroll = new ScrollPane(itemList);
        itemScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        itemScroll.setFitToWidth(true);
        itemScroll.setPrefViewportHeight(250);
        itemScroll.setStyle("""
                -fx-background-color: transparent;
                -fx-background: transparent;
                -fx-padding: 0;
                -fx-background-insets: 0;
                """);

        dropdownContent.getChildren().addAll(groupScroll, itemScroll);

        // 创建弹出窗口
        popup = new Popup();
        popup.getContent().add(dropdownContent);
        popup.setAutoHide(true);

        // 设置点击事件
        displayArea.setOnMouseClicked(e -> showDropdown());
        displayField.setOnMouseClicked(e -> showDropdown());

        // 修改鼠标悬浮效果
        displayArea.setOnMouseEntered(e -> {
            if (!popup.isShowing()) {
                displayArea.setStyle("""
                        -fx-background-color: linear-gradient(to bottom, #ffffff 0%, #e6e6e6 100%);
                        -fx-border-color: #999999;
                        -fx-border-width: 1;
                        -fx-border-radius: 3;
                        -fx-background-radius: 3;
                        -fx-cursor: hand;
                        """);
            }
        });

        displayArea.setOnMouseExited(e -> {
            if (!popup.isShowing()) {
                displayArea.setStyle("""
                        -fx-background-color: linear-gradient(to bottom, #ffffff 0%, #f2f2f2 100%);
                        -fx-border-color: #cccccc;
                        -fx-border-width: 1;
                        -fx-border-radius: 3;
                        -fx-background-radius: 3;
                        -fx-cursor: hand;
                        """);
            }
        });

        getChildren().add(displayArea);
    }

    /**
     * 设置数据源
     * 
     * @param dataMap 数据源,格式为 Map&lt;分组名称, Map&lt;选项名称, 选项值&gt;&gt;
     */
    public void setDataMap(Map<String, Map<String, T>> dataMap) {
        // 使用TreeMap对分组和选项进行排序
        TreeMap<String, Map<String, T>> sortedDataMap = new TreeMap<>();
        dataMap.forEach((group, itemMap) -> {
            TreeMap<String, T> sortedItemMap = new TreeMap<>(itemMap);
            sortedDataMap.put(group, sortedItemMap);
        });
        this.dataMap = sortedDataMap;
        updateDropdownContent();
    }

    /**
     * 设置选择监听器
     * 
     * @param listener 监听器,当选择发生变化时触发,参数为选择事件对象,包含分组、名称和值
     */
    public void setOnSelectListener(Consumer<GroupSelectEvent<T>> listener) {
        this.onSelectListener = listener;
    }

    /**
     * 获取选中值的属性对象
     * 
     * @return 选中值的属性对象
     */
    public ObjectProperty<T> selectedValueProperty() {
        return selectedValueProperty;
    }

    /**
     * 获取当前选中的分组名称
     * 
     * @return 分组名称
     */
    public String getSelectedGroup() {
        return selectedGroup;
    }

    /**
     * 获取当前选中的选项名称
     * 
     * @return 选项名称
     */
    public String getSelectedName() {
        return selectedName;
    }

    /**
     * 显示或隐藏下拉列表
     */
    private void showDropdown() {
        if (popup.isShowing()) {
            popup.hide();
        } else {
            popup.show(this, localToScreen(getBoundsInLocal()).getMinX(),
                    localToScreen(getBoundsInLocal()).getMaxY());
        }
    }

    /**
     * 更新下拉列表内容
     * 根据数据源重新生成分组和选项列表
     */
    private void updateDropdownContent() {
        groupList.getChildren().clear();
        final Label[] currentGroupLabel = new Label[1];

        dataMap.forEach((group, itemMap) -> {
            Label groupLabel = new Label(group);
            groupLabel.setMaxWidth(Double.MAX_VALUE);
            groupLabel.setPrefHeight(24); // 调整为标准高度
            groupLabel.setPadding(new Insets(4, 8, 4, 8));
            groupLabel.setAlignment(Pos.CENTER_LEFT);
            groupLabel.setStyle("""
                    -fx-background-color: transparent;
                    -fx-font-size: 12px;
                    -fx-text-fill: #333333;
                    """);

            groupLabel.setOnMouseEntered(e -> {
                if (currentGroupLabel[0] != null) {
                    currentGroupLabel[0].setStyle("""
                            -fx-background-color: transparent;
                            -fx-font-size: 12px;
                            -fx-text-fill: #333333;
                            """);
                }
                currentGroupLabel[0] = groupLabel;
                groupLabel.setStyle("""
                        -fx-background-color: -fx-selection-bar;
                        -fx-font-size: 12px;
                        -fx-text-fill: -fx-selection-bar-text;
                        """);
                showGroupItems(group, itemMap);
            });

            groupList.getChildren().add(groupLabel);
        });

        dropdownContent.setOnMouseExited(e -> {
            itemList.setVisible(false);
            if (currentGroupLabel[0] != null) {
                currentGroupLabel[0].setStyle("""
                        -fx-background-color: transparent;
                        -fx-font-size: 12px;
                        -fx-text-fill: #333333;
                        """);
                currentGroupLabel[0] = null;
            }
        });

        dropdownContent.setOnMouseEntered(e -> {
            if (currentGroupLabel[0] != null) {
                itemList.setVisible(true);
            }
        });
    }

    /**
     * 显示指定分组下的选项列表
     * 
     * @param group   分组名称
     * @param itemMap 选项Map,key为选项名称,value为选项值
     */
    private void showGroupItems(String group, Map<String, T> itemMap) {
        itemList.getChildren().clear();

        itemMap.forEach((name, value) -> {
            Label itemLabel = new Label(name);
            itemLabel.setMaxWidth(Double.MAX_VALUE);
            itemLabel.setPrefHeight(24); // 调整为标准高度
            itemLabel.setPadding(new Insets(4, 8, 4, 8));
            itemLabel.setAlignment(Pos.CENTER_LEFT);
            itemLabel.setStyle("""
                    -fx-background-color: transparent;
                    -fx-font-size: 12px;
                    -fx-text-fill: #333333;
                    """);

            itemLabel.setOnMouseEntered(e -> itemLabel.setStyle("""
                    -fx-background-color: -fx-selection-bar;
                    -fx-font-size: 12px;
                    -fx-text-fill: -fx-selection-bar-text;
                    """));

            itemLabel.setOnMouseExited(e -> itemLabel.setStyle("""
                    -fx-background-color: transparent;
                    -fx-font-size: 12px;
                    -fx-text-fill: #333333;
                    """));

            itemLabel.setOnMouseClicked(e -> {
                selectedGroup = group;
                selectedName = name;
                selectedValueProperty.set(value);
                displayField.setText(String.format("%s/%s", group, name));

                // 触发选择监听器
                if (onSelectListener != null) {
                    onSelectListener.accept(new GroupSelectEvent<>(group, name, value));
                }

                popup.hide();
            });

            itemList.getChildren().add(itemLabel);
        });

        itemList.setVisible(true);
    }

    /**
     * 设置提示文本
     * 
     * @param promptText 提示文本
     */
    public void setPromptText(String promptText) {
        this.promptText = promptText;
        displayField.setPromptText(promptText);
    }

    /**
     * 获取提示文本
     * 
     * @return 提示文本
     */
    public String getPromptText() {
        return promptText;
    }

    /**
     * 清除选择
     */
    public void clearSelection() {
        if (!allowClear) {
            return;
        }
        selectedGroup = null;
        selectedName = null;
        selectedValueProperty.set(null);
        displayField.setText("");

        // 触发选择监听器，传递null值表示清除选择
        if (onSelectListener != null) {
            onSelectListener.accept(new GroupSelectEvent<>(null, null, null));
        }
    }

    /**
     * 设置选中项
     * 
     * @param group 分组名称
     * @param name  选项名称
     */
    public void setSelectedItem(String group, String name) {
        if (dataMap == null || !dataMap.containsKey(group) || !dataMap.get(group).containsKey(name)) {
            clearSelection();
            return;
        }
        Map<String, T> groupMap = dataMap.get(group);
        T value = groupMap != null ? groupMap.get(name) : null;
        selectedGroup = group;
        selectedName = name;
        selectedValueProperty.set(value);
        displayField.setText(String.format("%s/%s", group, name));

        // 触发选择监听器
        if (onSelectListener != null) {
            onSelectListener.accept(new GroupSelectEvent<>(group, name, value));
        }
    }

    /**
     * 获取当前选中的值
     * 
     * @return 选中的值
     */
    public T getValue() {
        return selectedValueProperty.get();
    }

    /**
     * 设置分组列表宽度
     * 
     * @param width 宽度值
     */
    public void setGroupListWidth(double width) {
        this.groupListWidth = width;
        if (groupList != null) {
            groupList.setPrefWidth(width);
        }
    }

    /**
     * 获取分组列表宽度
     * 
     * @return 分组列表宽度
     */
    public double getGroupListWidth() {
        return groupListWidth;
    }

    /**
     * 设置选项列表宽度
     * 
     * @param width 宽度值
     */
    public void setItemListWidth(double width) {
        this.itemListWidth = width;
        if (itemList != null) {
            itemList.setPrefWidth(width);
        }
    }

    /**
     * 获取选项列表宽度
     * 
     * @return 选项列表宽度
     */
    public double getItemListWidth() {
        return itemListWidth;
    }

    /**
     * 设置是否允许清除选择
     * 
     * @param allowClear true表示允许清除，false表示不允许清除
     */
    public void setAllowClear(boolean allowClear) {
        this.allowClear = allowClear;
        // 立即更新清除按钮的可见性
        String currentText = displayField.getText();
        boolean shouldShow = allowClear && currentText != null && !currentText.isEmpty();
        clearButton.setManaged(shouldShow);
        clearButton.setVisible(shouldShow);
    }

    /**
     * 获取是否允许清除选择
     * 
     * @return true表示允许清除，false表示不允许清除
     */
    public boolean isAllowClear() {
        return allowClear;
    }
}
