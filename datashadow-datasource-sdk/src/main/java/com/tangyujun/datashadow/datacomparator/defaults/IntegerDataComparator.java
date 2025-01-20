package com.tangyujun.datashadow.datacomparator.defaults;

import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * 整数数据比较器
 */
public class IntegerDataComparator implements DataComparator {

    /**
     * 生成整数数据比较器
     * 
     * @return 整数数据比较器
     */
    @DataComparatorRegistry(friendlyName = "数值", group = "整数")
    public static DataComparatorGenerator generator() {
        return () -> new IntegerDataComparator();
    }

    /**
     * 判断两个整数是否相等
     * 处理逻辑：
     * 1. 如果两个整数都为null，则返回true
     * 2. 如果一个整数为null，另一个整数不为null，则返回false
     * 3. 如果两个整数都为空字符串，则返回true
     * 4. 如果一个整数为空字符串，另一个整数不为空字符串，则返回false
     * 5. 如果两个整数都为空字符串，则返回true
     * 
     * @param o1 整数1
     * @param o2 整数2
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        try {
            Integer i1;
            Integer i2;
            if (o1 instanceof Number number) {
                i1 = number.intValue();
            } else {
                i1 = Integer.valueOf(o1.toString().trim());
            }
            if (o2 instanceof Number number) {
                i2 = number.intValue();
            } else {
                i2 = Integer.valueOf(o2.toString().trim());
            }
            return i1.equals(i2);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 导出数据比较器
     * 
     * @return 数据比较器
     */
    @Override
    public String exportComparator() {
        return "";
    }

    /**
     * 导入数据比较器
     * 
     * @param exportValueString 导出的数据比较器
     */
    @Override
    public void importComparator(String exportValueString) {
    }

    /**
     * 配置整数数据比较器
     * 
     * @param primaryStage 主舞台
     */
    @Override
    public void config(Window primaryStage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("配置");
        alert.setHeaderText("整数比较器");
        alert.setContentText("整数比较器无需配置");
        alert.initOwner(primaryStage);
        alert.showAndWait();
    }

    /**
     * 获取描述
     * 
     * @return 描述
     */
    @Override
    public String getDescription() {
        return "整数比较器";
    }
}
