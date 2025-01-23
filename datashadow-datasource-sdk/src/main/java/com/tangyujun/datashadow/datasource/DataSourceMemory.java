package com.tangyujun.datashadow.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.beans.property.SimpleStringProperty;

import com.tangyujun.datashadow.datatype.CsvData;
import com.tangyujun.datashadow.datatype.JsonData;
import com.tangyujun.datashadow.datatype.XmlData;

import javafx.stage.Window;

/**
 * 内存数据源
 * 用于存储和管理内存中的数据集
 * 主要用于临时数据的存储和处理
 * 支持XML、JSON、CSV三种格式的数据导入
 * 数据导入后将以List<Map<String, Object>>的形式存储在内存中
 * 
 * 主要功能:
 * 1. 支持XML、JSON、CSV三种格式数据的导入和解析
 * 2. 提供数据预览和编辑功能
 * 3. 数据格式校验和错误提示
 * 4. 支持数据回显和重新编辑
 * 5. 支持自动数据类型推断
 * 
 * @author tangyujun
 */
public class DataSourceMemory extends DataSource {

    /**
     * 原始数据（纯文本，用于数据回显）
     * 保存用户输入的原始文本内容，便于后续编辑时回显
     */
    private String originData;

    /**
     * 数据类型（JSON、XML、CSV）
     * 记录当前数据源使用的数据格式类型
     */
    private String dataType;

    /**
     * 数据源注册
     * 用于生成DataSourceMemory实例
     * 通过@DataSourceRegistry注解标记为可用数据源
     * 
     * @return DataSourceMemory实例
     */
    @DataSourceRegistry(friendlyName = "Memory内存数据源")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceMemory();
    }

    /**
     * 数据集
     * 使用List<Map<String, Object>>存储数据
     * 其中List中的每个Map代表一行数据
     * Map的key为字段名，value为字段值
     * 数据格式示例:
     * [
     * {"name": "张三", "age": 18, "gender": "男"},
     * {"name": "李四", "age": 20, "gender": "女"}
     * ]
     * 
     * 重要说明:
     * 1. 所有数据均以字符串形式存储
     * 2. Map中的key即为数据列名
     * 3. 每行数据的字段数量可以不同
     */
    private List<Map<String, Object>> data;

    /**
     * 验证数据源是否有效
     * 由于是内存数据源，不需要特殊的验证逻辑
     * 数据的有效性在导入时已经进行了验证
     * 
     * @throws DataSourceValidException 当数据源验证失败时抛出此异常
     */
    @Override
    public void valid() throws DataSourceValidException {
    }

    /**
     * 获取数据集
     * 直接返回内存中存储的数据集
     * 如果数据集为null，也直接返回null
     * 
     * @return 返回内存中存储的数据集
     * @throws DataAccessException 当数据访问出错时抛出此异常
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        return data;
    }

    /**
     * 获取数据集的列名
     * 通过获取数据集中第一行数据的所有键名来获取列名
     * 如果数据集为空或为null，返回空列表
     * 
     * 注意:
     * 1. 仅使用第一行数据的字段作为列名
     * 2. 如果数据为空返回空列表而不是null
     * 
     * @return 列名列表，如果数据为空则返回空列表
     */
    @Override
    public List<String> getColumns() {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.get(0).keySet());
    }

    /**
     * 获取已配置数据源的简单描述
     * 用于在界面上显示数据源的基本信息
     * 由于是内存数据源，直接返回固定描述
     * 
     * @return 数据源的简单描述字符串，格式为"内存数据源 [数据类型]"
     */
    @Override
    public String getDescription() {
        return String.format("内存数据源 [%s]", dataType == null ? "未配置" : dataType);
    }

    /**
     * 自动推断数据类型
     * 根据输入内容的特征判断数据类型
     * 
     * @param content 输入的数据内容
     * @return 推断出的数据类型（JSON、XML、CSV）
     */
    private String inferDataType(String content) {
        content = content.trim();
        return JsonData.isJson(content) ? "JSON"
                : XmlData.isXml(content) ? "XML" : CsvData.isCsv(content) ? "CSV" : "JSON";

    }

    /**
     * 解析数据并返回解析结果
     * 支持XML、JSON、CSV三种格式的数据解析
     * 
     * 解析规则:
     * 1. XML格式要求必须有root节点，每个record节点表示一行数据
     * 2. JSON格式必须是对象数组
     * 3. CSV格式第一行必须是列名
     * 
     * @param content 数据内容
     * @param type    数据类型
     * @return 解析后的数据列表
     * @throws Exception 当解析失败时抛出相应异常
     */
    private List<Map<String, Object>> parseData(String content, String type) throws Exception {
        return switch (type) {
            case "XML" -> XmlData.getValues(content);
            case "JSON" -> JsonData.getValues(content);
            case "CSV" -> CsvData.getValues(content);
            default -> new ArrayList<>();
        };
    }

    /**
     * 配置数据源的具体行为
     * 打开一个对话框让用户输入数据
     * 支持XML、JSON、CSV三种格式的数据输入
     * 
     * 界面功能:
     * 1. 数据类型选择下拉框
     * 2. 数据输入文本域
     * 3. 数据预览表格
     * 4. 预览/编辑切换按钮
     * 5. 确定/取消按钮
     * 
     * 数据格式说明：
     * XML格式：<root><record><field1>value1</field1><field2>value2</field2></record></root>
     * JSON格式：[{"field1":"value1","field2":"value2"}]
     * CSV格式：field1,field2\nvalue1,value2
     * 
     * @param primaryStage 主窗口，用于设置对话框的父窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // 创建新的对话框窗口
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("数据录入");

        BorderPane dialogRoot = new BorderPane();
        dialogRoot.setPadding(new Insets(10));

        // 创建数据类型选择下拉框
        ComboBox<String> dataTypeCombo = new ComboBox<>();
        dataTypeCombo.getItems().addAll("JSON", "XML", "CSV");
        dataTypeCombo.setValue(dataType != null ? dataType : "JSON");
        dataTypeCombo.setPrefWidth(150); // 设置下拉框宽度

        // 创建数据输入文本域
        TextArea dataInput = new TextArea();
        dataInput.setWrapText(true);
        dataInput.setPrefRowCount(10);
        // 如果存在原始数据则回显
        if (originData != null) {
            dataInput.setText(originData);
        }

        // 监听文本变化，自动推断数据类型
        dataInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                String inferredType = inferDataType(newValue);
                dataTypeCombo.setValue(inferredType);
            }
        });

        // 创建预览表格
        TableView<Map<String, Object>> previewTable = new TableView<>();

        // 创建预览/编辑切换按钮
        Button toggleButton = new Button("预览");
        toggleButton.setOnAction(event -> {
            try {
                if (toggleButton.getText().equals("预览")) {
                    // 解析数据
                    String content = dataInput.getText();
                    if (content == null || content.trim().isEmpty()) {
                        throw new IllegalArgumentException("请输入数据内容");
                    }

                    List<Map<String, Object>> previewData = parseData(content, dataTypeCombo.getValue());

                    // 清空并重新设置表格列
                    previewTable.getColumns().clear();
                    if (!previewData.isEmpty()) {
                        for (String key : previewData.get(0).keySet()) {
                            TableColumn<Map<String, Object>, String> column = new TableColumn<>(key);
                            column.setCellValueFactory(
                                    d -> new SimpleStringProperty(String.valueOf(d.getValue().get(key))));
                            previewTable.getColumns().add(column);
                        }
                    }

                    // 设置数据
                    previewTable.setItems(FXCollections.observableArrayList(previewData));

                    // 切换视图
                    dataInput.toBack();
                    previewTable.toFront();
                    toggleButton.setText("编辑");
                } else {
                    // 切换回编辑视图
                    previewTable.toBack();
                    dataInput.toFront();
                    toggleButton.setText("预览");
                }
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("预览错误");
                alert.setHeaderText("无法预览数据");
                alert.setContentText("数据格式不正确或解析失败");
                alert.showAndWait();
                // 确保切换回编辑视图
                previewTable.toBack();
                dataInput.toFront();
                toggleButton.setText("预览");
            }
        });

        // 创建按钮
        Button confirmBtn = new Button("确定");
        Button cancelBtn = new Button("取消");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().addAll(confirmBtn, cancelBtn);

        // 创建布局
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(0, 0, 10, 0));
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // 数据类型选择区域
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("数据类型:");
        typeBox.getChildren().addAll(typeLabel, dataTypeCombo, toggleButton);

        // 创建StackPane用于重叠放置输入区域和预览表格
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(previewTable, dataInput);
        VBox.setVgrow(stackPane, Priority.ALWAYS);

        // 数据内容区域
        VBox dataBox = new VBox(5);
        Label dataLabel = new Label("数据内容:");
        dataBox.getChildren().addAll(dataLabel, stackPane);
        VBox.setVgrow(dataBox, Priority.ALWAYS);

        contentBox.getChildren().addAll(typeBox, dataBox);

        dialogRoot.setCenter(contentBox);
        dialogRoot.setBottom(buttonBox);

        // 确定按钮事件处理
        confirmBtn.setOnAction(event -> {
            try {
                String inputContent = dataInput.getText();
                String selectedType = dataTypeCombo.getValue();

                if (inputContent == null || inputContent.trim().isEmpty()) {
                    throw new IllegalArgumentException("请输入数据内容");
                }

                // 保存原始数据和数据类型
                this.originData = inputContent;
                this.dataType = selectedType;

                // 解析数据
                this.data = parseData(inputContent, selectedType);
                callback.onConfigureFinished();
                dialog.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText("数据解析失败");
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        // 取消按钮事件处理
        cancelBtn.setOnAction(event -> {
            callback.onConfigureCancelled();
            dialog.close();
        });
        Scene dialogScene = new Scene(dialogRoot, 500, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    /**
     * 导出为JSON，包含dataType和originData和父类的字段
     * 例如：{"dataType":"JSON","originData":"[{"id":1,"name":"John"},{"id":2,"name":"Jane"}]","mappings":{}}
     */
    @Override
    public String exportSource() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入为JSON，包含dataType和originData和父类的字段
     * 例如：{"dataType":"JSON","originData":"[{"id":1,"name":"John"},{"id":2,"name":"Jane"}]","mappings":{}}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void importSource(String exportValueString) {
        // 解析JSON
        Map<String, Object> map = JSON.parseObject(exportValueString, new TypeReference<Map<String, Object>>() {
        });
        if (map == null) {
            return;
        }
        // 解析数据
        try {
            this.dataType = (String) map.get("dataType");
            this.originData = (String) map.get("originData");
            this.setMappings((Map<String, String>) map.get("mappings"));
            this.data = parseData(originData, dataType);
        } catch (Exception e) {
        }
    }
}