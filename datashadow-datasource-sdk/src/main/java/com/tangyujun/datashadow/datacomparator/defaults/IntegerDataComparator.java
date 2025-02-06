package com.tangyujun.datashadow.datacomparator.defaults;

import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

import javafx.scene.control.Alert;
import javafx.stage.Window;

/**
 * 整数数据比较器
 * 用于比较两个值是否代表相同的整数
 * 支持功能:
 * 1. 比较两个整数值是否相等
 * 2. 自动处理null值和空字符串
 * 3. 支持Number类型和字符串类型的转换
 * 
 * 使用场景:
 * 1. 需要比较整数值是否相等
 * 2. 处理字符串形式的整数比较
 * 3. 处理不同数值类型(如Long、Integer等)的比较
 */
public class IntegerDataComparator implements DataComparator {

    /**
     * 生成整数数据比较器
     * 注册为系统内置比较器,显示名称为"整数"
     * 
     * @return 整数数据比较器实例
     */
    @DataComparatorRegistry(friendlyName = "整数", group = "内置")
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
     * 6. 对于Number类型,直接获取intValue进行比较
     * 7. 对于其他类型,尝试将toString()结果转换为Integer后比较
     * 8. 如果转换过程出现异常,返回false
     * 
     * @param o1 要比较的第一个值,可以是Number类型或其他可转换为整数的类型
     * @param o2 要比较的第二个值,可以是Number类型或其他可转换为整数的类型
     * @return 两个值是否代表相同的整数
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
     * 导出数据比较器配置
     * 由于整数比较器不需要配置,返回空字符串
     * 
     * @return 空字符串,表示无配置需要导出
     */
    @Override
    public String exportComparator() {
        return "";
    }

    /**
     * 导入数据比较器配置
     * 由于整数比较器不需要配置,此方法为空实现
     * 
     * @param exportValueString 导出的配置字符串(此处未使用)
     */
    @Override
    public void importComparator(String exportValueString) {
    }

    /**
     * 配置整数数据比较器
     * 显示提示对话框,告知用户此比较器无需配置
     * 
     * @param primaryStage 父窗口,用于定位对话框
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
     * 获取比较器描述
     * 返回简单的描述文本,说明这是一个整数比较器
     * 
     * @return 比较器的描述文本
     */
    @Override
    public String getDescription() {
        return "整数比较器";
    }
}
