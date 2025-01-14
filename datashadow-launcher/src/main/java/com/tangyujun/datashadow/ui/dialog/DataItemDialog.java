package com.tangyujun.datashadow.ui.dialog;

import com.tangyujun.datashadow.dataitem.DataItem;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * 数据项编辑对话框
 */
public class DataItemDialog extends Dialog<DataItem> {
    private final TextField codeField = new TextField();
    private final TextField nickField = new TextField();
    private final CheckBox uniqueCheckBox = new CheckBox();
    private final TextArea comparatorArea = new TextArea();
    private final TextArea remarkArea = new TextArea();

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
        comparatorArea.setPromptText("// 请输入自定义比较器代码\n" +
                "// 参数: value1, value2 - 待比较的两个值\n" +
                "// 返回: true - 相等, false - 不相等\n" +
                "function compare(value1, value2) {\n" +
                "    return value1 === value2;\n" +
                "}");
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

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("输入错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private DataItem createDataItem() {
        DataItem item = new DataItem();
        item.setUnique(uniqueCheckBox.isSelected());
        item.setCode(codeField.getText().trim());
        item.setNick(nickField.getText().trim());
        item.setComparator(comparatorArea.getText().trim());
        item.setRemark(remarkArea.getText().trim());
        return item;
    }
}