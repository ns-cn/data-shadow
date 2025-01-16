package com.tangyujun.datashadow.ui.dialog;

import com.tangyujun.datashadow.dataitem.DataItem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 数据项编辑对话框
 * 用于新增或编辑数据项信息
 * 包含以下字段:
 * - 是否唯一: 标识该数据项是否作为唯一标识
 * - 名称: 数据项的代码,必填且必须符合命名规范
 * - 别名: 数据项的显示名称,选填
 * - 自定义比较器: JavaScript代码,用于自定义数据项的比较逻辑
 * - 备注: 数据项的补充说明信息
 */
public class DataItemDialog extends Dialog<DataItem> {
    /**
     * 数据项代码输入框
     */
    private final TextField codeField = new TextField();

    /**
     * 数据项别名输入框
     */
    private final TextField nickField = new TextField();

    /**
     * 是否作为唯一标识的复选框
     */
    private final CheckBox uniqueCheckBox = new CheckBox();

    /**
     * 自定义比较器代码输入区域
     */
    private final TextArea comparatorArea = new TextArea();

    /**
     * 备注信息输入区域
     */
    private final TextArea remarkArea = new TextArea();

    /**
     * 构造函数
     * 
     * @param item 待编辑的数据项,为null时表示新增模式
     */
    public DataItemDialog(DataItem item) {
        // 设置对话框标题
        setTitle(item == null ? "新增数据项" : "编辑数据项");

        // 创建对话框内容
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        // 创建表单
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);

        // 是否唯一
        form.add(new Label("是否唯一:"), 0, 0);
        form.add(uniqueCheckBox, 1, 0);

        // 名称
        form.add(new Label("名称: *"), 0, 1);
        form.add(codeField, 1, 1);
        GridPane.setHgrow(codeField, Priority.ALWAYS);

        // 别名
        form.add(new Label("别名:"), 0, 2);
        form.add(nickField, 1, 2);

        // 自定义比较器
        form.add(new Label("自定义比较器:"), 0, 3);
        comparatorArea.setPrefRowCount(6);
        comparatorArea.setPromptText("""
                // \u8bf7\u8f93\u5165\u81ea\u5b9a\u4e49\u6bd4\u8f83\u5668\u4ee3\u7801
                // \u53c2\u6570: value1, value2 - \u5f85\u6bd4\u8f83\u7684\u4e24\u4e2a\u503c
                // \u8fd4\u56de: true - \u76f8\u7b49, false - \u4e0d\u76f8\u7b49
                function compare(value1, value2) {
                    return value1 === value2;
                }""");
        form.add(comparatorArea, 1, 3);

        // 备注
        form.add(new Label("备注:"), 0, 4);
        remarkArea.setPrefRowCount(3);
        form.add(remarkArea, 1, 4);

        // 如果是编辑模式，填充现有数据
        if (item != null) {
            uniqueCheckBox.setSelected(item.isUnique());
            codeField.setText(item.getCode());
            nickField.setText(item.getNick());
            comparatorArea.setText(item.getComparator());
            remarkArea.setText(item.getRemark());
        }

        // 添加按钮
        ButtonType confirmButtonType = new ButtonType("确定", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("取消", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);

        // 设置结果转换器
        setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                if (validateInput()) {
                    return createDataItem();
                }
                return null;
            }
            return null;
        });

        content.getChildren().add(form);
        getDialogPane().setContent(content);
    }

    /**
     * 验证输入数据的有效性
     * 验证规则:
     * 1. 名称不能为空
     * 2. 名称必须以字母开头
     * 3. 名称只能包含字母、数字和下划线
     * 
     * @return 验证通过返回true,否则返回false
     */
    private boolean validateInput() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            showError("名称不能为空");
            return false;
        }
        if (!code.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            showError("名称必须以字母开头，只能包含字母、数字和下划线");
            return false;
        }
        return true;
    }

    /**
     * 显示错误提示对话框
     * 
     * @param message 错误信息
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("输入错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * 从对话框输入创建数据项
     * 处理流程:
     * 1. 创建新的数据项对象
     * 2. 设置是否唯一标识
     * 3. 获取并处理各输入字段的值
     * 4. 设置必填字段(code)
     * 5. 设置可选字段(nick,comparator,remark)
     * 
     * @return 新创建的数据项对象
     */
    private DataItem createDataItem() {
        DataItem item = new DataItem();

        // 设置基本属性
        item.setUnique(uniqueCheckBox.isSelected());

        // 获取输入值并进行空值处理
        String code = codeField.getText();
        String nick = nickField.getText();
        String comparator = comparatorArea.getText();
        String remark = remarkArea.getText();

        // 设置必填字段，确保不为null
        item.setCode(code != null ? code.trim() : "");

        // 设置选填字段，null值也是允许的
        if (nick != null && !nick.trim().isEmpty()) {
            item.setNick(nick.trim());
        }
        if (comparator != null && !comparator.trim().isEmpty()) {
            item.setComparator(comparator.trim());
        }
        if (remark != null && !remark.trim().isEmpty()) {
            item.setRemark(remark.trim());
        }
        return item;
    }
}