package com.tangyujun.datashadow.datacomparator.defaults;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * 布尔值数据比较器
 * 用于比较两个值是否代表相同的布尔值
 * 支持配置:
 * 1. 自定义哪些值被视为true
 * 2. 是否将null值视为true
 */
public class BooleanDataComparator implements DataComparator {

    /**
     * 生成布尔值数据比较器
     * 注册为系统内置比较器,显示名称为"布尔值"
     * 
     * @return 布尔值数据比较器实例
     */
    @DataComparatorRegistry(friendlyName = "布尔值", group = "内置")
    public static DataComparatorGenerator generator() {
        return () -> new BooleanDataComparator();
    }

    /**
     * 配置布尔值比较器
     * 弹出对话框让用户配置:
     * 1. 哪些值被视为true(用逗号分隔)
     * 2. 是否将null值视为true
     * 
     * @param primaryStage 父窗口
     */
    @Override
    public void config(Window primaryStage) {
        // 创建对话框
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("布尔值比较器配置");
        dialog.setHeaderText("请配置布尔值比较规则");
        dialog.initOwner(primaryStage);

        // 创建对话框内容
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // 创建true值列表输入框
        TextField trueValuesField = new TextField(String.join(",", TRUE_VALUES));
        grid.add(new Label("视为true的值(逗号分隔):"), 0, 0);
        grid.add(trueValuesField, 1, 0);

        // 创建null值处理选择框
        CheckBox nullAsTrueCheckBox = new CheckBox("将null视为true");
        grid.add(nullAsTrueCheckBox, 0, 1, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 显示对话框并处理结果
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 设置true值列表
            String trueValuesStr = trueValuesField.getText();
            setTrueValues(Arrays.asList(trueValuesStr.split(",")));

            // 设置null值处理方式
            setNullAsTrue(nullAsTrueCheckBox.isSelected());
        }
    }

    /**
     * 默认的布尔值为true的值列表
     * 包含常见的表示true的字符串,如:
     * true, 1, 是, 对, 正确, ok, yes, y, t
     */
    private static final List<String> TRUE_VALUES = Arrays.asList("true", "1", "是", "对", "正确", "ok", "yes", "y", "t");

    /**
     * 配置的布尔值为true的值列表
     * 用户可以通过配置对话框自定义此列表
     */
    private List<String> trueValues;

    /**
     * 是否将null视为true
     * 用户可以通过配置对话框设置此选项
     */
    private boolean nullAsTrue;

    /**
     * 判断两个布尔值是否相等
     * 比较规则:
     * 1. 如果两个对象引用相同,返回true
     * 2. 如果两个对象都为null,返回true
     * 3. 如果只有一个对象为null:
     * - 如果配置了将null视为true,则判断另一个对象是否为true
     * - 否则返回false
     * 4. 如果都不为null,判断两个对象是否都代表true或都代表false
     * 
     * @param o1 要比较的第一个值
     * @param o2 要比较的第二个值
     * @return 两个值是否代表相同的布尔值
     */
    @Override
    public boolean equals(Object o1, Object o2) {
        // 如果两个对象引用相同,直接返回true
        if (o1 == o2) {
            return true;
        }

        // 如果两个对象都为null,返回true
        if (o1 == null && o2 == null) {
            return true;
        }

        // 如果只有一个对象为null
        if (o1 == null || o2 == null) {
            // 如果配置了将null视为true,则判断另一个对象是否为true
            if (nullAsTrue) {
                Object nonNullObj = o1 == null ? o2 : o1;
                return isTrue(nonNullObj);
            }
            return false;
        }

        // 都不为null时,判断两个对象转换为字符串后是否都在trueValues中或都不在
        boolean o1IsTrue = isTrue(o1);
        boolean o2IsTrue = isTrue(o2);
        return o1IsTrue == o2IsTrue;
    }

    /**
     * 判断一个对象是否代表true值
     * 将对象转换为字符串,并检查是否在trueValues列表中
     * 比较时忽略大小写,并去除首尾空格
     * 
     * @param obj 要判断的对象
     * @return 该对象是否代表true值
     */
    private boolean isTrue(Object obj) {
        String strValue = String.valueOf(obj).trim().toLowerCase();
        return trueValues != null && trueValues.contains(strValue);
    }

    /**
     * 导出比较器配置
     * 将当前比较器的配置转换为JSON字符串
     * 
     * @return 包含比较器配置的JSON字符串
     */
    @Override
    public String exportComparator() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入比较器配置
     * 从JSON字符串中恢复比较器的配置
     * 
     * @param exportValueString 包含比较器配置的JSON字符串
     */
    @Override
    public void importComparator(String exportValueString) {
        BooleanDataComparator booleanDataComparator = JSON.parseObject(exportValueString, BooleanDataComparator.class);
        this.trueValues = booleanDataComparator.getTrueValues();
        this.nullAsTrue = booleanDataComparator.isNullAsTrue();
    }

    /**
     * 获取true值列表
     * 
     * @return 当前配置的true值列表
     */
    public List<String> getTrueValues() {
        return trueValues;
    }

    /**
     * 设置true值列表
     * 
     * @param trueValues 新的true值列表
     */
    public void setTrueValues(List<String> trueValues) {
        this.trueValues = trueValues;
    }

    /**
     * 获取是否将null视为true的配置
     * 
     * @return 当前是否将null视为true
     */
    public boolean isNullAsTrue() {
        return nullAsTrue;
    }

    /**
     * 设置是否将null视为true
     * 
     * @param nullAsTrue 是否将null视为true
     */
    public void setNullAsTrue(boolean nullAsTrue) {
        this.nullAsTrue = nullAsTrue;
    }

    /**
     * 获取比较器的描述信息
     * 包含比较器类型和null值处理方式
     * 
     * @return 比较器的描述信息
     */
    @Override
    public String getDescription() {
        return "布尔值比较器" + (nullAsTrue ? "(null视为true)" : "(null视为false)");
    }
}
