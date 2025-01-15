package com.tangyujun.datashadow.datasource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.StringReader;
import java.util.HashMap;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javafx.stage.Window;

/**
 * 内存数据源
 * 用于存储和管理内存中的数据集
 * 主要用于临时数据的存储和处理
 * 支持XML、JSON、CSV三种格式的数据导入
 * 数据导入后将以List<Map<String, Object>>的形式存储在内存中
 */
public class DataSourceMemory extends DataSource {

    /**
     * 原始数据（纯文本，用于数据回显）
     */
    private String originData;

    /**
     * 数据类型（JSON、XML、CSV）
     */
    private String dataType;

    /**
     * 数据源注册
     * 用于生成DataSourceMemory实例
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
     * @return 数据源的简单描述字符串
     */
    @Override
    public String getDescription() {
        return String.format("内存数据源 [%s]", dataType == null ? "未配置" : dataType);
    }

    /**
     * 解析数据并返回解析结果
     * 
     * @param content 数据内容
     * @param type    数据类型
     * @return 解析后的数据列表
     */
    private List<Map<String, Object>> parseData(String content, String type) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        switch (type) {
            case "XML" -> {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                NodeList records = doc.getDocumentElement().getChildNodes();

                for (int i = 0; i < records.getLength(); i++) {
                    Node recordNode = records.item(i);
                    if (recordNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element record = (Element) recordNode;
                        NodeList fields = record.getChildNodes();
                        Map<String, Object> row = new HashMap<>();

                        for (int j = 0; j < fields.getLength(); j++) {
                            Node fieldNode = fields.item(j);
                            if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element field = (Element) fieldNode;
                                row.put(field.getTagName(), field.getTextContent());
                            }
                        }

                        if (!row.isEmpty()) {
                            result.add(row);
                        }
                    }
                }
            }
            case "JSON" -> {
                result = JSON.parseArray(content, new TypeReference<Map<String, Object>>() {
                }.getType());
            }
            case "CSV" -> {
                String[] lines = content.split("\n");
                if (lines.length > 0) {
                    String[] headers = lines[0].split(",");
                    for (int i = 0; i < headers.length; i++) {
                        headers[i] = headers[i].trim();
                    }

                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i].trim();
                        if (!line.isEmpty()) {
                            String[] values = line.split(",");
                            Map<String, Object> row = new HashMap<>();
                            for (int j = 0; j < headers.length && j < values.length; j++) {
                                row.put(headers[j], values[j].trim());
                            }
                            result.add(row);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 配置数据源的具体行为
     * 打开一个对话框让用户输入数据
     * 支持XML、JSON、CSV三种格式的数据输入
     * 数据格式说明：
     * XML格式：<root><record><field1>value1</field1><field2>value2</field2></record></root>
     * JSON格式：[{"field1":"value1","field2":"value2"}]
     * CSV格式：field1,field2\nvalue1,value2
     * 
     * @param primaryStage      主窗口，用于设置对话框的父窗口
     * @param configureFinished 配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, Runnable configureFinished) {
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

        // 创建预览表格
        TableView<Map<String, Object>> previewTable = new TableView<>();

        // 创建预览/编辑切换按钮
        Button toggleButton = new Button("预览");
        toggleButton.setOnAction(e -> {
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
                                    data -> new SimpleStringProperty(String.valueOf(data.getValue().get(key))));
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
        confirmBtn.setOnAction(e -> {
            try {
                String content = dataInput.getText();
                String type = dataTypeCombo.getValue();

                if (content == null || content.trim().isEmpty()) {
                    throw new IllegalArgumentException("请输入数据内容");
                }

                // 保存原始数据和数据类型
                this.originData = content;
                this.dataType = type;

                // 解析数据
                this.data = parseData(content, type);
                configureFinished.run();
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
        cancelBtn.setOnAction(e -> dialog.close());
        Scene dialogScene = new Scene(dialogRoot, 500, 400);
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
