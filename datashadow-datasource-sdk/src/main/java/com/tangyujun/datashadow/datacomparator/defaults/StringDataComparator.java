package com.tangyujun.datashadow.datacomparator.defaults;

import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * 字符串数据比较器
 * 用于比较两个字符串是否相等
 * 支持配置:
 * 1. 是否忽略大小写
 * 2. 是否将null和空字符串视为相等
 * 
 * 使用场景:
 * 1. 需要比较字符串是否相等
 * 2. 需要忽略大小写比较字符串
 * 3. 需要特殊处理null和空字符串的场景
 */
public class StringDataComparator implements DataComparator {

    /**
     * 生成字符串数据比较器
     * 注册为系统内置比较器,显示名称为"字符串"
     * 默认配置:
     * - 不忽略大小写
     * - 将null和空字符串视为相等
     * 
     * @return 字符串数据比较器生成器
     */
    @DataComparatorRegistry(friendlyName = "字符串", group = "内置")
    public static DataComparatorGenerator ignoreAndNullEquals() {
        return () -> new StringDataComparator(false, true);
    }

    /**
     * 默认构造函数
     * 创建一个默认配置的字符串比较器:
     * - 不忽略大小写
     * - 将null和空字符串视为相等
     */
    public StringDataComparator() {
        this(false, true);
    }

    /**
     * 构造函数
     * 创建一个自定义配置的字符串比较器
     * 
     * @param ignoreCase      是否忽略大小写进行比较
     * @param nullEqualsEmpty 是否将null和空字符串视为相等
     */
    public StringDataComparator(boolean ignoreCase, boolean nullEqualsEmpty) {
        this.ignoreCase = ignoreCase;
        this.nullEqualsEmpty = nullEqualsEmpty;
    }

    /**
     * 是否忽略大小写进行比较
     * true: 忽略大小写(如"ABC"和"abc"视为相等)
     * false: 不忽略大小写(如"ABC"和"abc"视为不相等)
     */
    private boolean ignoreCase;

    /**
     * 是否将null和空字符串视为相等
     * true: null和""视为相等
     * false: null和""视为不相等
     */
    private boolean nullEqualsEmpty;

    /**
     * 比较两个对象是否相等
     * 比较规则:
     * 1. 如果两个对象都为null,返回true
     * 2. 如果只有一个对象为null:
     * - 如果配置了nullEqualsEmpty,则检查另一个对象是否为空字符串
     * - 否则返回false
     * 3. 将两个对象转换为字符串后比较:
     * - 如果配置了ignoreCase,使用equalsIgnoreCase比较
     * - 否则使用equals比较
     * 
     * @param o1 要比较的第一个对象
     * @param o2 要比较的第二个对象
     * @return 两个对象是否相等
     */
    @Override
    public boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            if (nullEqualsEmpty) {
                String s1 = o1 == null ? "" : o1.toString();
                String s2 = o2 == null ? "" : o2.toString();
                return s1.isEmpty() && s2.isEmpty();
            }
            return false;
        }
        String s1 = o1.toString();
        String s2 = o2.toString();
        return ignoreCase ? s1.equalsIgnoreCase(s2) : s1.equals(s2);
    }

    /**
     * 导出数据比较器配置
     * 将当前比较器的配置转换为JSON字符串
     * 
     * @return 包含比较器配置的JSON字符串
     */
    @Override
    public String exportComparator() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入数据比较器配置
     * 从JSON字符串中恢复比较器的配置
     * 
     * @param exportValueString 包含比较器配置的JSON字符串
     */
    @Override
    public void importComparator(String exportValueString) {
        StringDataComparator comparator = JSON.parseObject(exportValueString, StringDataComparator.class);
        this.ignoreCase = comparator.ignoreCase;
        this.nullEqualsEmpty = comparator.nullEqualsEmpty;
    }

    /**
     * 获取是否忽略大小写的配置
     * 
     * @return true表示忽略大小写,false表示不忽略大小写
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * 设置是否忽略大小写
     * 
     * @param ignoreCase true表示忽略大小写,false表示不忽略大小写
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * 获取是否将null和空字符串视为相等的配置
     * 
     * @return true表示将null和空字符串视为相等,false表示不视为相等
     */
    public boolean isNullEqualsEmpty() {
        return nullEqualsEmpty;
    }

    /**
     * 设置是否将null和空字符串视为相等
     * 
     * @param nullEqualsEmpty true表示将null和空字符串视为相等,false表示不视为相等
     */
    public void setNullEqualsEmpty(boolean nullEqualsEmpty) {
        this.nullEqualsEmpty = nullEqualsEmpty;
    }

    /**
     * 配置比较器
     * 弹出对话框让用户配置:
     * 1. 是否忽略大小写
     * 2. 是否将null和空字符串视为相等
     * 
     * @param primaryStage 父窗口
     */
    @Override
    public void config(Window primaryStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("字符串比较器配置");
        dialog.setHeaderText("请配置字符串比较器参数");
        dialog.initOwner(primaryStage);

        // 创建复选框
        CheckBox ignoreCaseCheckBox = new CheckBox("忽略大小写");
        ignoreCaseCheckBox.setSelected(ignoreCase);

        CheckBox nullEqualsEmptyCheckBox = new CheckBox("将null和空字符串视为相等");
        nullEqualsEmptyCheckBox.setSelected(nullEqualsEmpty);

        // 创建布局
        VBox content = new VBox(10);
        content.getChildren().addAll(ignoreCaseCheckBox, nullEqualsEmptyCheckBox);
        content.setPadding(new Insets(20, 10, 10, 10));

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ignoreCase = ignoreCaseCheckBox.isSelected();
            nullEqualsEmpty = nullEqualsEmptyCheckBox.isSelected();
        }
    }

    /**
     * 获取比较器的描述信息
     * 
     * @return 返回"字符串"作为该比较器的描述
     */
    @Override
    public String getDescription() {
        return "字符串";
    }
}
