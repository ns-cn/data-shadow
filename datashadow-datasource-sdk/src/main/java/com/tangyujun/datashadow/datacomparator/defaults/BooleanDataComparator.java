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
 */
public class BooleanDataComparator implements DataComparator {

    /**
     * 生成布尔值数据比较器
     * 
     * @return 布尔值数据比较器
     */
    @DataComparatorRegistry(friendlyName = "布尔值", group = "System")
    public static DataComparatorGenerator generator() {
        return () -> new BooleanDataComparator();
    }

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
     * 布尔值为true的值列表
     */
    private static final List<String> TRUE_VALUES = Arrays.asList("true", "1", "是", "对", "正确", "ok", "yes", "y", "t");

    /**
     * 配置的布尔值为true的值列表
     */
    private List<String> trueValues;

    /**
     * 是否将null视为true
     */
    private boolean nullAsTrue;

    /**
     * 判断两个布尔值是否相等
     * 
     * @param o1 布尔值1
     * @param o2 布尔值2
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o1, Object o2) {
        return o1 == o2;
    }

    @Override
    public String exportComparator() {
        return JSON.toJSONString(this);
    }

    @Override
    public void importComparator(String exportValueString) {
        BooleanDataComparator booleanDataComparator = JSON.parseObject(exportValueString, BooleanDataComparator.class);
        this.trueValues = booleanDataComparator.getTrueValues();
        this.nullAsTrue = booleanDataComparator.isNullAsTrue();
    }

    /**
     * 获取true值列表
     * 
     * @return true值列表
     */
    public List<String> getTrueValues() {
        return trueValues;
    }

    /**
     * 设置true值列表
     * 
     * @param trueValues true值列表
     */
    public void setTrueValues(List<String> trueValues) {
        this.trueValues = trueValues;
    }

    /**
     * 获取是否将null视为true
     * 
     * @return 是否将null视为true
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

    @Override
    public String getDescription() {
        return "布尔值比较器" + (nullAsTrue ? "(null视为true)" : "(null视为false)");
    }
}
