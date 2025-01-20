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
 * 浮点数数据比较器（默认精度为-1，表示精确匹配）
 */
public class DoubleDataComparator implements DataComparator {

    /**
     * 精度,用于控制浮点数比较时的精度
     * 例如:精度为2时,1.234和1.235视为相等
     * 小于0时按照精确匹配处理
     */
    private int precision = -1;

    /**
     * 生成浮点数数据比较器
     * 
     * @return 浮点数数据比较器
     */
    @DataComparatorRegistry(friendlyName = "数值", group = "浮点数")
    public static DataComparatorGenerator generate() {
        return () -> new DoubleDataComparator();
    }

    /**
     * 配置浮点数数据比较器
     * 
     * @param primaryStage 主舞台
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
     */
    public DoubleDataComparator() {
    }

    /**
     * 构造函数
     * 
     * @param precision 精度
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
     * 
     * @param o1 浮点数1
     * @param o2 浮点数2
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
     * 
     * @return 包含数据比较器配置信息的字符串
     */
    @Override
    public String exportComparator() {
        return String.valueOf(precision);
    }

    /**
     * 将字符串反序列化为数据比较器
     * 
     * @param exportValueString 包含数据比较器配置信息的字符串
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
     * 获取精度
     * 
     * @return 精度
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * 设置精度
     * 
     * @param precision 精度
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    /**
     * 获取描述
     * 
     * @return 描述
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
