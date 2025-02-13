package com.tangyujun.datashadow.ui.menu.dialog;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import java.util.List;
import java.util.Map;
import javafx.scene.layout.Priority;

/**
 * Q&A对话框
 * 展示常见问题及答案
 */
public class QADialog extends Dialog<Void> {

    private static final List<Map.Entry<String, String>> QA_LIST = List.of(
            Map.entry("如何添加数据源?", """
                    1. 在主界面点击"配置数据源"按钮
                    2. 从下拉列表选择数据源类型(Excel、CSV、数据库等)
                    3. 根据提示配置数据源参数
                    4. 点击"测试连接"验证配置是否正确
                    5. 点击"确定"保存配置"""),

            Map.entry("如何配置数据项?", """
                    1. 在数据项维护区域点击"新增"按钮
                    2. 填写数据项名称(必填)和别名(选填)
                    3. 选择是否作为唯一标识
                    4. 选择并配置数据比较器
                    5. 点击"确定"保存数据项"""),

            Map.entry("如何导出比对结果?", """
                    1. 完成数据比对后,在结果区域选择导出格式(Excel、CSV、JSON)
                    2. 点击"导出"按钮
                    3. 选择保存位置
                    4. 等待导出完成"""),

            Map.entry("如何保存和导入比对方案?", """
                    1. 保存方案:
                       - 点击菜单"对比方案" -> "导出方案"
                       - 选择保存位置
                       - 方案将保存为.shadow文件
                    2. 导入方案:
                       - 点击菜单"对比方案" -> "导入方案"
                       - 选择之前保存的.shadow文件
                       - 确认导入即可"""),

            Map.entry("如何安装和管理插件?", """
                    1. 插件安装:
                       - 将插件jar包复制到用户目录下的.datashadow/plugins目录
                       - 重启应用程序生效

                    2. 插件类型:
                       - 数据源插件: 支持新的数据源类型
                       - 比较器插件: 支持新的数据比较方式
                       - 导出器插件：支持新的导出方式"""),

            Map.entry("如何查看和修改修改插件目录?", """
                    1. 查看插件目录:
                       - 点击菜单"设置" -> "系统设置"
                       - 在系统设置对话框中查看插件目录路径

                    2. 默认插件目录:
                       - 位于用户目录下的.datashadow/plugins
                       - 例如Windows: C:\\Users\\用户名\\.datashadow\\plugins
                       - 例如Linux: /home/用户名/.datashadow/plugins

                    3. 修改插件目录:
                       - 点击菜单"设置" -> "系统设置"
                       - 在系统设置对话框中修改插件目录路径
                       - 点击"保存"
                       - 重启应用程序生效

                    4. 注意事项:
                       - 新目录必须具有读权限
                       - 修改后需要手动将插件jar包移动到新目录
                       - 重启后才能加载新目录中的插件"""),

            Map.entry("如何开发自定义插件?", """
                    1. 开发环境准备:
                       - 使用Java 21
                       - 引入datashadow-sdk依赖

                    2. 插件开发步骤:
                       - 实现相应的插件接口
                       - 完成插件类注册
                       - 打包为jar文件

                    3. 插件接口说明:
                       - DataSource: 实现数据源插件
                       - DataComparator: 实现数据比较器插件
                       - DataExporter: 实现数据导出插件

                    4. 插件打包发布:
                       - 使用maven打包为jar
                       - 将jar放入plugins目录
                       - 重启应用加载新插件"""));

    public QADialog(Window owner) {
        setTitle("Q&A 常见问题解答");
        initOwner(owner);

        // 创建内容区域
        VBox content = new VBox(10);
        content.setPrefWidth(600);
        content.setPrefHeight(400);

        // 创建手风琴控件
        Accordion accordion = new Accordion();
        VBox.setVgrow(accordion, Priority.ALWAYS); // 让accordion占满剩余空间

        // 添加QA项
        for (var qa : QA_LIST) {
            TitledPane pane = new TitledPane();
            pane.setText(qa.getKey());

            TextArea answer = new TextArea(qa.getValue());
            answer.setWrapText(true);
            answer.setEditable(false);
            answer.setPrefRowCount(5);
            answer.setStyle("-fx-font-family: monospace;"); // 使用等宽字体

            // 让TextArea占满TitledPane的空间
            VBox answerBox = new VBox(answer);
            VBox.setVgrow(answer, Priority.ALWAYS);
            pane.setContent(answerBox);

            accordion.getPanes().add(pane);
        }

        content.getChildren().add(accordion);

        // 添加关闭按钮
        ButtonType closeButton = new ButtonType("关闭", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().add(closeButton);

        getDialogPane().setContent(content);

        // 设置对话框的最小尺寸
        getDialogPane().setMinWidth(600);
        getDialogPane().setMinHeight(400);
    }
}