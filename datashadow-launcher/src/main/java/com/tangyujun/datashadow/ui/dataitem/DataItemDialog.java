package com.tangyujun.datashadow.ui.dataitem;

import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.ui.components.GroupComboBox;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datacomparator.DataComparator;

import javafx.scene.layout.HBox;

import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;

/**
 * 数据项编辑对话框
 * 用于新增或编辑数据项信息
 * 包含以下字段:
 * - 是否唯一: 标识该数据项是否作为唯一标识,用于数据比对时确定记录的唯一性
 * - 名称: 数据项的代码,必填且必须符合命名规范(字母开头,只能包含字母数字下划线)
 * - 别名: 数据项的显示名称,选填,用于界面展示时的友好显示
 * - 比较器: 用于配置数据项的比较逻辑,必填,支持多种比较器类型和自定义配置
 * - 备注: 数据项的补充说明信息,用于记录额外的描述性内容
 * 
 * 对话框功能:
 * 1. 支持新增和编辑两种模式
 * 2. 提供完整的输入验证
 * 3. 支持比较器的动态选择和配置
 * 4. 实时预览比较器配置状态
 */
public class DataItemDialog extends Dialog<DataItem> {
    /**
     * 数据项代码输入框
     * 用于输入数据项的唯一标识符,必须符合命名规范
     */
    private final TextField codeField = new TextField();

    /**
     * 数据项别名输入框
     * 用于输入数据项的显示名称,提供更友好的界面展示
     */
    private final TextField nickField = new TextField();

    /**
     * 是否作为唯一标识的复选框
     * 用于标识该数据项是否作为数据比对时的唯一键
     */
    private final CheckBox uniqueCheckBox = new CheckBox();

    /**
     * 比较器类型选择下拉框
     * 用于选择比较器的主要类型和具体实现
     */
    private final GroupComboBox<DataComparatorGenerator> comparatorCombo;

    /**
     * 比较器配置按钮
     * 用于打开比较器的详细配置界面
     */
    private final Button configButton;

    /**
     * 当前配置预览标签
     * 用于显示当前选择的比较器配置信息
     */
    private final Label currentConfigLabel;

    /**
     * 备注信息输入区域
     * 用于输入数据项的补充说明信息
     */
    private final TextArea remarkArea = new TextArea();

    private DataComparator comparator;

    /**
     * 构造函数
     * 初始化对话框界面,设置布局和事件处理
     * 
     * @param item 待编辑的数据项,为null时表示新增模式,否则为编辑模式并填充现有数据
     */
    public DataItemDialog(DataItem item) {
        // 设置对话框标题
        setTitle(item == null ? "新增数据项" : "编辑数据项");

        // 创建对话框内容
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        // 创建表单
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPrefWidth(360);

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

        // 比较器
        form.add(new Label("比较器: "), 0, 3);

        // 创建比较器选择和配置的容器
        VBox comparatorBox = new VBox(5);

        // 比较器类型选择区域
        HBox typeBox = new HBox(10);

        comparatorCombo = new GroupComboBox<>("请选择比较器");
        comparatorCombo.setPrefWidth(200);
        comparatorCombo.setDataMap(DataFactory.getInstance().getDataComparators());

        configButton = new Button("⚙");
        configButton.setTooltip(new Tooltip("配置比较器"));
        configButton.setDisable(true);

        typeBox.getChildren().addAll(comparatorCombo, configButton);

        // 当前配置预览
        VBox previewBox = new VBox(5);
        previewBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #eee; -fx-padding: 5;");
        currentConfigLabel = new Label("未配置");
        currentConfigLabel.setStyle("-fx-text-fill: #666;");
        previewBox.getChildren().addAll(new Label("当前配置:"), currentConfigLabel);

        // 将类型选择和预览添加到比较器容器
        comparatorBox.getChildren().addAll(typeBox, previewBox);

        // 将整个比较器容器添加到表单
        form.add(comparatorBox, 1, 3, 3, 1);

        // 备注
        form.add(new Label("备注:"), 0, 4);
        remarkArea.setPrefRowCount(3);
        remarkArea.setPrefWidth(200);
        form.add(remarkArea, 1, 4, 3, 1);

        // 如果是编辑模式，填充现有数据
        if (item != null) {
            uniqueCheckBox.setSelected(item.isUnique());
            codeField.setText(item.getCode());
            nickField.setText(item.getNick());
            remarkArea.setText(item.getRemark());

            // 处理比较器相关信息
            if (item.getComparator() != null) {
                this.comparator = item.getComparator();
                // 设置比较器选择
                String group = item.getComparatorGroup();
                String name = item.getComparatorName();
                if (group != null && name != null) {
                    comparatorCombo.setSelectedItem(group, name);
                    configButton.setDisable(false);
                }
                currentConfigLabel.setText(comparator.getDescription());
            } else {
                currentConfigLabel.setText("未配置");
            }
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

        // 阻止对话框在验证失败时关闭
        final Button confirmButton = (Button) getDialogPane().lookupButton(confirmButtonType);
        confirmButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (!validateInput()) {
                event.consume();
            }
        });

        content.getChildren().add(form);
        getDialogPane().setContent(content);

        // 设置事件处理
        setupComparatorEvents();

        // 设置对话框的首选宽度和最小宽度
        getDialogPane().setPrefWidth(440);
        getDialogPane().setMinWidth(400);
    }

    /**
     * 验证输入数据的有效性
     * 验证规则:
     * 1. 名称不能为空 - 确保必填字段已填写
     * 2. 名称必须以字母开头 - 符合标准命名规范
     * 3. 名称只能包含字母、数字和下划线 - 确保命名合法性
     * 4. 比较器类型和比较器必须选择 - 确保数据比较逻辑完整
     * 
     * @return 验证通过返回true,否则返回false并显示相应错误提示
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
     * 用于在输入验证失败时向用户展示错误信息
     * 
     * @param message 错误信息内容
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
     * 1. 创建新的数据项对象 - 初始化基础结构
     * 2. 设置是否唯一标识 - 确定数据项在比对时的角色
     * 3. 获取并处理各输入字段的值 - 包括必填和可选字段
     * 4. 设置必填字段(code) - 确保核心数据的完整性
     * 5. 设置可选字段(nick,comparator,remark) - 补充额外信息
     * 
     * @return 新创建的数据项对象,包含完整的配置信息
     */
    private DataItem createDataItem() {
        DataItem item = new DataItem();

        // 设置基本属性
        item.setUnique(uniqueCheckBox.isSelected());

        // 获取输入值并进行空值处理
        String code = codeField.getText();
        String nick = nickField.getText();
        String remark = remarkArea.getText();

        // 设置必填字段，确保不为null
        item.setCode(code != null ? code.trim() : "");

        // 设置选填字段，null值也是允许的
        if (nick != null && !nick.trim().isEmpty()) {
            item.setNick(nick.trim());
        }

        // 设置比较器相关信息(可选)
        if (comparatorCombo.getValue() != null && comparator != null) {
            String comparatorType = comparatorCombo.getSelectedGroup();
            String comparatorSubType = comparatorCombo.getSelectedName();
            item.setComparatorGroup(comparatorType.trim());
            item.setComparatorName(comparatorSubType.trim());
            item.setComparator(comparator);
        }

        if (remark != null && !remark.trim().isEmpty()) {
            item.setRemark(remark.trim());
        }
        return item;
    }

    /**
     * 设置比较器相关的事件处理
     */
    private void setupComparatorEvents() {
        // 比较器选择事件
        comparatorCombo.setOnSelectListener(event -> {
            DataComparatorGenerator generator = event.value();
            if (generator != null) {
                comparator = generator.generate();
                configButton.setDisable(false);
                currentConfigLabel.setText(comparator.getDescription());
            } else {
                comparator = null;
                configButton.setDisable(true);
            }
        });

        // 配置按钮点击事件
        configButton.setOnAction(e -> {
            if (comparator != null) {
                comparator.config(getDialogPane().getScene().getWindow());
                currentConfigLabel.setText(comparator.getDescription());
            }
        });
    }
}