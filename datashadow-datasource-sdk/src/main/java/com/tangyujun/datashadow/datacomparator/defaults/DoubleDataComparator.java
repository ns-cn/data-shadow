package com.tangyujun.datashadow.datacomparator.defaults;

import java.util.Optional;

import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

/**
 * 浮点数数据比较器
 * 用于比较两个浮点数是否相等
 * 支持配置:
 * 1. 精度设置 - 控制比较时的小数位数
 * 2. 精确匹配 - 当精度小于0时进行精确匹配
 * 
 * 使用场景:
 * 1. 需要比较浮点数时忽略一定精度的误差
 * 2. 需要精确比较浮点数值
 * 3. 处理字符串形式的浮点数比较
 */
public class DoubleDataComparator implements DataComparator {

    /**
     * 精度,用于控制浮点数比较时的精度
     * 例如:
     * - 精度为2时,1.234和1.235视为相等(都会被四舍五入到1.23)
     * - 精度为1时,1.15和1.16视为不相等(分别四舍五入为1.2和1.2)
     * - 小于0时按照精确匹配处理,不进行四舍五入
     */
    private int precision = -1;

    /**
     * 生成浮点数数据比较器
     * 注册为系统内置比较器,显示名称为"浮点数"
     * 
     * @return 浮点数数据比较器实例
     */
    @DataComparatorRegistry(friendlyName = "浮点数", group = "内置")
    public static DataComparatorGenerator generate() {
        return () -> new DoubleDataComparator();
    }

    /**
     * 配置浮点数数据比较器
     * 弹出对话框让用户配置精度值
     * 包含输入验证:
     * 1. 必须是有效的整数
     * 2. 负数表示精确匹配
     * 3. 非负数表示保留的小数位数
     * 
     * @param primaryStage 父窗口
     */
    @Override
    public void config(Window primaryStage) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(precision));
        dialog.setTitle("设置精度");
        dialog.setHeaderText("请输入精度值");
        dialog.setContentText("精度(负数表示精确匹配):");
        dialog.initOwner(primaryStage);
        dialog.getDialogPane().lookupButton(ButtonType.OK)
                .addEventFilter(ActionEvent.ACTION, event -> {
                    String input = dialog.getEditor().getText();
                    try {
                        Integer.valueOf(input);
                    } catch (NumberFormatException e) {
                        event.consume();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("输入错误");
                        alert.setHeaderText("请输入有效的整数");
                        alert.setContentText("您输入的 \"" + input + "\" 不是有效的整数值");
                        alert.initOwner(primaryStage);
                        alert.showAndWait();
                    }
                });
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                precision = Integer.parseInt(result.get());
            } catch (NumberFormatException e) {
                precision = -1;
            }
        }
    }

    /**
     * 默认构造函数
     * 创建一个精度为-1(精确匹配)的比较器
     */
    public DoubleDataComparator() {
    }

    /**
     * 构造函数
     * 创建指定精度的比较器
     * 
     * @param precision 精度值,负数表示精确匹配
     */
    public DoubleDataComparator(int precision) {
        this.precision = precision;
    }

    /**
     * 判断两个浮点数是否相等
     * 处理逻辑：
     * 1. 如果两个浮点数都为null，则返回true
     * 2. 如果一个浮点数为null，另一个浮点数不为null，则返回false
     * 3. 如果两个浮点数都为空字符串，则返回true
     * 4. 如果一个浮点数为空字符串，另一个浮点数不为空字符串，则返回false
     * 5. 如果两个浮点数都为空字符串，则返回true
     * 6. 对于有效的数值:
     * - 如果精度小于0,进行精确匹配
     * - 如果精度大于等于0,根据精度进行四舍五入后比较
     * 
     * @param o1 要比较的第一个值,可以是Number类型或可解析为浮点数的字符串
     * @param o2 要比较的第二个值,可以是Number类型或可解析为浮点数的字符串
     * @return 两个值是否相等
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
            Double d1;
            Double d2;
            if (o1 instanceof Number number) {
                d1 = number.doubleValue();
            } else {
                d1 = Double.valueOf(o1.toString().trim());
            }
            if (o2 instanceof Number number) {
                d2 = number.doubleValue();
            } else {
                d2 = Double.valueOf(o2.toString().trim());
            }

            // 精度小于0时进行精确匹配
            if (precision < 0) {
                return d1.equals(d2);
            }

            // 根据精度进行比较
            double factor = Math.pow(10, precision);
            long l1 = Math.round(d1 * factor);
            long l2 = Math.round(d2 * factor);
            return l1 == l2;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 将数据比较器序列化为字符串
     * 序列化格式:
     * - 仅序列化精度值
     * - 使用String.valueOf()转换为字符串
     * 
     * @return 包含精度值的字符串
     */
    @Override
    public String exportComparator() {
        return String.valueOf(precision);
    }

    /**
     * 将字符串反序列化为数据比较器
     * 反序列化规则:
     * 1. 如果输入为null或空字符串,设置精度为-1
     * 2. 尝试将字符串解析为整数作为精度值
     * 3. 如果解析失败,设置精度为-1
     * 
     * @param exportValueString 包含精度值的字符串
     */
    @Override
    public void importComparator(String exportValueString) {
        if (exportValueString == null || exportValueString.isEmpty()) {
            precision = -1;
        } else {
            try {
                precision = Integer.parseInt(exportValueString);
            } catch (NumberFormatException e) {
                precision = -1;
            }
        }
    }

    /**
     * 获取当前配置的精度值
     * 
     * @return 精度值,负数表示精确匹配
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 设置精度值
     * 
     * @param precision 新的精度值,负数表示精确匹配
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * 获取比较器的描述信息
     * 描述格式:
     * - 基础描述: "浮点数比较器"
     * - 如果设置了非负精度,追加: "，精度为X"
     * 
     * @return 描述字符串
     */
    @Override
    public String getDescription() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("浮点数比较器");
        if (precision >= 0) {
            stringBuilder.append("，精度为").append(precision);
        }
        return stringBuilder.toString();
    }
}
