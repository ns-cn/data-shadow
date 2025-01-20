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
 */
public class StringDataComparator implements DataComparator {

    /**
     * 不忽略大小写，将null和空字符串视为相等
     * 
     * @return 数据比较器生成器
     */
    @DataComparatorRegistry(friendlyName = "字符串", group = "System")
    public static DataComparatorGenerator ignoreAndNullEquals() {
        return () -> new StringDataComparator(false, true);
    }

    /**
     * 默认构造函数
     * 
     * 不忽略大小写，将null和空字符串视为相等
     */
    public StringDataComparator() {
        this(false, true);
    }

    /**
     * 构造函数
     * 
     * @param ignoreCase      是否忽略大小写
     * @param nullEqualsEmpty 是否将null和空字符串视为相等
     */
    public StringDataComparator(boolean ignoreCase, boolean nullEqualsEmpty) {
        this.ignoreCase = ignoreCase;
        this.nullEqualsEmpty = nullEqualsEmpty;
    }

    /**
     * 是否忽略大小写
     */
    private boolean ignoreCase;

    /**
     * 是否将null和空字符串视为相等
     */
    private boolean nullEqualsEmpty;

    /**
     * 比较两个对象是否相等
     * 
     * @param o1 对象1
     * @param o2 对象2
     * @return 是否相等
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
     * 导出数据比较器
     * 
     * @return 数据比较器
     */
    @Override
    public String exportComparator() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入数据比较器
     * 
     * @param exportValueString 导出值字符串
     */
    @Override
    public void importComparator(String exportValueString) {
        StringDataComparator comparator = JSON.parseObject(exportValueString, StringDataComparator.class);
        this.ignoreCase = comparator.ignoreCase;
        this.nullEqualsEmpty = comparator.nullEqualsEmpty;
    }

    /**
     * 获取是否忽略大小写
     * 
     * @return 是否忽略大小写
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * 设置是否忽略大小写
     * 
     * @param ignoreCase 是否忽略大小写
     */
    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    /**
     * 获取null是否等于空字符串
     * 
     * @return null是否等于空字符串
     */
    public boolean isNullEqualsEmpty() {
        return nullEqualsEmpty;
    }

    /**
     * 设置null是否等于空字符串
     * 
     * @param nullEqualsEmpty null是否等于空字符串
     */
    public void setNullEqualsEmpty(boolean nullEqualsEmpty) {
        this.nullEqualsEmpty = nullEqualsEmpty;
    }

    /**
     * 配置比较器
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
     * 比较器描述
     * 
     */
    @Override
    public String getDescription() {
        return "字符串";
    }
}
