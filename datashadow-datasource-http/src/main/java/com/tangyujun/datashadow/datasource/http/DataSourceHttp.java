package com.tangyujun.datashadow.datasource.http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.datatype.CsvData;
import com.tangyujun.datashadow.datatype.JsonData;
import com.tangyujun.datashadow.datatype.ShadowData;
import com.tangyujun.datashadow.datatype.XmlData;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.Optional;

/**
 * HTTP数据源实现类
 * 支持通过HTTP请求获取JSON、XML、CSV格式的数据
 */
public class DataSourceHttp extends DataSource {

    // HTTP请求方法常量
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String DELETE = "DELETE";
    public static final String HEAD = "HEAD";
    public static final String OPTIONS = "OPTIONS";
    public static final String PATCH = "PATCH";

    /**
     * HTTP响应数据类型常量
     * RESPONSE_TYPE_JSON: JSON格式响应数据
     * RESPONSE_TYPE_XML: XML格式响应数据
     * RESPONSE_TYPE_CSV: CSV格式响应数据
     */
    public static final String RESPONSE_TYPE_JSON = "json";
    public static final String RESPONSE_TYPE_XML = "xml";
    public static final String RESPONSE_TYPE_CSV = "csv";

    /**
     * HTTP请求URL
     */
    private String url;

    /**
     * HTTP请求方法,默认为GET
     */
    private String method = GET;

    /**
     * HTTP请求头
     */
    private Map<String, String> headers = new HashMap<>();

    /**
     * HTTP请求体
     */
    private String body;

    /**
     * 数据源响应数据类型
     * RESPONSE_TYPE_JSON: JSON格式响应数据
     * RESPONSE_TYPE_XML: XML格式响应数据
     * RESPONSE_TYPE_CSV: CSV格式响应数据
     */
    private String responseType = RESPONSE_TYPE_JSON;

    /**
     * 获取数据源生成器
     * 
     * @return 返回HTTP数据源生成器
     */
    @DataSourceRegistry(group = "网络", friendlyName = "HTTP")
    public static DataSourceGenerator getGenerator() {
        return () -> new DataSourceHttp();
    }

    /**
     * 验证数据源配置是否有效
     * 验证URL、请求方法、响应类型是否合法
     * 并尝试发送测试请求验证连接是否可用
     * 
     * @throws DataSourceValidException 数据源验证异常
     */
    @Override
    public void valid() throws DataSourceValidException {
        // 验证URL是否为空
        if (url == null || url.trim().isEmpty()) {
            throw new DataSourceValidException("URL不能为空", null);
        }

        // 验证URL格式
        if (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            throw new DataSourceValidException("URL必须以'http://'或'https://'开头", null);
        }

        // 验证请求方法是否为空且是否合法
        if (method == null || method.trim().isEmpty()) {
            throw new DataSourceValidException("请求方法不能为空", null);
        }

        // 验证响应类型是否合法
        if (responseType == null || responseType.trim().isEmpty()) {
            throw new DataSourceValidException("响应数据类型不能为空", null);
        }
        if (!(RESPONSE_TYPE_JSON.equals(responseType) || RESPONSE_TYPE_XML.equals(responseType)
                || RESPONSE_TYPE_CSV.equals(responseType))) {
            throw new DataSourceValidException("不支持的响应数据类型: " + responseType, null);
        }

        try {
            // 使用OkHttp发送测试请求验证连接是否可用
            getValues();
        } catch (DataAccessException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw new DataSourceValidException("URL格式无效，请检查URL是否正确", e);
            }
            throw new DataSourceValidException("连接测试失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取数据源数据
     * 通过HTTP请求获取数据并根据响应类型解析为Map列表
     * 
     * @return 返回数据列表,每个数据项为一个Map
     * @throws DataAccessException 数据访问异常
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        // 使用OkHttp发送请求获取数据
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        // 添加请求头
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestBuilder::addHeader);
        }

        // 根据请求方法决定是否添加请求体
        if ((POST.equals(method) || PUT.equals(method) ||
                PATCH.equals(method) || DELETE.equals(method)) &&
                body != null && !body.trim().isEmpty()) {
            RequestBody requestBody = RequestBody.create(
                    body.getBytes(StandardCharsets.UTF_8));
            requestBuilder.method(method.toUpperCase(), requestBody);
        } else {
            requestBuilder.method(method.toUpperCase(), null);
        }

        try {
            Response response = client.newCall(requestBuilder.build()).execute();
            if (!response.isSuccessful()) {
                throw new DataAccessException("HTTP请求失败: " + response.code() + " " + response.message(), null);
            }
            // 获取响应内容
            var responseBody = response.body();
            if (responseBody == null) {
                throw new DataAccessException("HTTP响应体为空", null);
            }
            String content = responseBody.string();
            if (content.trim().isEmpty()) {
                throw new DataAccessException("HTTP响应内容为空", null);
            }
            // 根据响应类型解析数据
            return switch (responseType) {
                case RESPONSE_TYPE_JSON -> JsonData.getValues(content);
                case RESPONSE_TYPE_XML -> XmlData.getValues(content);
                case RESPONSE_TYPE_CSV -> CsvData.getValues(content);
                default -> throw new DataAccessException("不支持的响应数据类型: " + responseType, null);
            };
        } catch (IOException e) {
            throw new DataAccessException("获取HTTP数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取数据列名列表
     * 通过获取数据并提取列名
     * 如果获取失败则返回空列表
     * 
     * @return 返回数据列名列表
     */
    @Override
    public List<String> getColumns() {
        List<Map<String, Object>> values;
        try {
            values = getValues();
        } catch (RuntimeException e) {
            return List.of();
        }
        return ShadowData.getColumns(values);
    }

    /**
     * 获取数据源描述
     * 
     * @return 返回数据源描述
     */
    @Override
    public String getDescription() {
        return "网络请求";
    }

    /**
     * 配置数据源
     * 打开配置对话框,用户可以设置URL、请求方法、响应类型、请求头和请求体
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成回调
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("配置HTTP数据源");

        // 创建根布局容器
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // URL输入框
        Label urlLabel = new Label("请求URL：");
        TextField urlField = new TextField(url);
        urlField.setPromptText("请输入HTTP请求URL，例如：https://api.example.com/data");
        VBox urlBox = new VBox(5);
        urlBox.getChildren().addAll(urlLabel, urlField);

        // 请求方法和响应类型选择（放在同一行）
        HBox methodAndTypeBox = new HBox(20);

        // 请求方法选择
        HBox methodBox = new HBox(10);
        methodBox.setAlignment(Pos.CENTER_LEFT);
        Label methodLabel = new Label("请求方法：");
        ComboBox<String> methodCombo = new ComboBox<>(FXCollections.observableArrayList(
                GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH));
        methodCombo.setValue(method);
        methodCombo.setPrefWidth(150);
        methodBox.getChildren().addAll(methodLabel, methodCombo);
        HBox.setHgrow(methodBox, Priority.ALWAYS);

        // 响应类型选择
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeLabel = new Label("响应数据类型：");
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
                RESPONSE_TYPE_JSON, RESPONSE_TYPE_XML, RESPONSE_TYPE_CSV));
        typeCombo.setValue(responseType);
        typeCombo.setPrefWidth(150);
        typeBox.getChildren().addAll(typeLabel, typeCombo);
        HBox.setHgrow(typeBox, Priority.ALWAYS);

        methodAndTypeBox.getChildren().addAll(methodBox, typeBox);

        // 请求头
        Label headersLabel = new Label("请求头：");
        VBox headersBox = new VBox(5);
        headersBox.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-padding: 10;");
        ScrollPane headersScroll = new ScrollPane(headersBox);
        headersScroll.setFitToWidth(true);
        headersScroll.setPrefHeight(150);

        ObservableList<HBox> headerRows = FXCollections.observableArrayList();
        headers.forEach((key, value) -> addHeaderRow(headerRows, headersBox, key, value));

        Button addHeaderBtn = new Button("添加请求头");
        addHeaderBtn.setOnAction(e -> addHeaderRow(headerRows, headersBox, "", ""));

        // 请求体
        Label bodyLabel = new Label("请求体：");
        TextArea bodyArea = new TextArea(body);
        bodyArea.setPromptText("请输入请求体内容（仅POST、PUT、PATCH、DELETE方法可用）");
        bodyArea.setPrefRowCount(4);

        // 根据请求方法禁用/启用请求体
        methodCombo.valueProperty().addListener((obs, old, newVal) -> {
            boolean enableBody = POST.equals(newVal) || PUT.equals(newVal) ||
                    PATCH.equals(newVal) || DELETE.equals(newVal);
            bodyArea.setDisable(!enableBody);
        });
        bodyArea.setDisable(!POST.equals(method) && !PUT.equals(method) &&
                !PATCH.equals(method) && !DELETE.equals(method));

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button importCurlBtn = new Button("从CURL导入");
        importCurlBtn.setOnAction(e -> {
            // 创建输入对话框
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("从CURL导入");
            dialog.setHeaderText(null);
            dialog.setContentText("请输入CURL命令：");

            // 替换默认的TextField为TextArea
            TextArea textArea = new TextArea();
            textArea.setPrefRowCount(25); // 显示5行
            textArea.setPrefColumnCount(50); // 每行大约50个字符
            textArea.setWrapText(true); // 启用自动换行
            textArea.setPromptText("在此粘贴CURL命令...");

            // 设置TextArea的样式
            textArea.setStyle("-fx-font-family: monospace;"); // 使用等宽字体

            // 获取对话框的内容面板并替换输入控件
            DialogPane dialogPane = dialog.getDialogPane();
            // 移除原有的TextField
            dialogPane.getChildren().remove(dialog.getEditor());
            // 添加新的TextArea
            dialogPane.setContent(textArea);

            // 设置对话框大小
            dialogPane.setPrefWidth(600);
            dialog.setResizable(true);

            // 处理确定按钮事件
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return textArea.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    // 解析CURL命令
                    CurlParser.CurlParseResult parseResult = CurlParser.parse(result.get());

                    // 更新UI组件
                    urlField.setText(parseResult.getUrl());
                    methodCombo.setValue(parseResult.getMethod());

                    // 清除现有请求头
                    headerRows.clear();
                    headersBox.getChildren().clear();

                    // 添加新的请求头
                    parseResult.getHeaders().forEach((key, value) -> addHeaderRow(headerRows, headersBox, key, value));

                    // 设置请求体
                    bodyArea.setText(parseResult.getBody());

                    // 显示成功提示
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("导入成功");
                    alert.setHeaderText(null);
                    alert.setContentText("已成功导入CURL命令");
                    alert.showAndWait();

                } catch (IllegalArgumentException ex) {
                    // 显示错误提示
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("导入失败");
                    alert.setHeaderText(null);
                    alert.setContentText("CURL命令解析失败：" + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        Button testBtn = new Button("测试连接");
        testBtn.setOnAction(e -> {
            try {
                updateDataSourceFromUI(urlField, methodCombo, typeCombo, headerRows, bodyArea);
                valid();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("测试结果");
                alert.setHeaderText(null);
                alert.setContentText("连接测试成功！");
                alert.showAndWait();
            } catch (DataSourceValidException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("测试结果");
                alert.setHeaderText(null);
                alert.setContentText("连接测试失败：" + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button okBtn = new Button("确定");
        okBtn.setOnAction(e -> {
            try {
                updateDataSourceFromUI(urlField, methodCombo, typeCombo, headerRows, bodyArea);
                callback.onConfigureFinished();
                stage.close();
            } catch (DataSourceValidException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("错误");
                alert.setHeaderText(null);
                alert.setContentText("配置验证失败：" + ex.getMessage());
                alert.showAndWait();
            }
        });

        Button cancelBtn = new Button("取消");
        cancelBtn.setOnAction(e -> stage.close());

        HBox leftButtons = new HBox(10);
        leftButtons.getChildren().add(importCurlBtn);
        HBox rightButtons = new HBox(10);
        rightButtons.getChildren().addAll(testBtn, okBtn, cancelBtn);

        buttonBox.getChildren().addAll(leftButtons, rightButtons);
        HBox.setHgrow(leftButtons, Priority.ALWAYS);

        // 将所有组件添加到根布局
        root.getChildren().addAll(
                urlBox,
                methodAndTypeBox,
                headersLabel, headersScroll, addHeaderBtn,
                bodyLabel, bodyArea,
                buttonBox);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * 添加请求头输入行
     * 
     * @param headerRows 请求头行列表
     * @param headersBox 请求头容器
     * @param key        请求头名称
     * @param value      请求头值
     */
    protected void addHeaderRow(ObservableList<HBox> headerRows, VBox headersBox, String key, String value) {
        HBox headerRow = new HBox(10);
        TextField keyField = new TextField(key);
        keyField.setPromptText("Header名称");
        TextField valueField = new TextField(value);
        valueField.setPromptText("Header值");
        Button deleteBtn = new Button("删除");

        HBox.setHgrow(keyField, Priority.ALWAYS);
        HBox.setHgrow(valueField, Priority.ALWAYS);

        headerRow.getChildren().addAll(keyField, valueField, deleteBtn);
        headerRows.add(headerRow);
        headersBox.getChildren().add(headerRow);

        deleteBtn.setOnAction(e -> {
            headerRows.remove(headerRow);
            headersBox.getChildren().remove(headerRow);
        });
    }

    /**
     * 从UI组件更新数据源配置
     * 
     * @param urlField    URL输入框
     * @param methodCombo 请求方法下拉框
     * @param typeCombo   响应类型下拉框
     * @param headerRows  请求头行列表
     * @param bodyArea    请求体文本框
     */
    protected void updateDataSourceFromUI(TextField urlField, ComboBox<String> methodCombo,
            ComboBox<String> typeCombo, ObservableList<HBox> headerRows,
            TextArea bodyArea) {
        this.url = urlField.getText();
        this.method = methodCombo.getValue();
        this.responseType = typeCombo.getValue();
        this.headers.clear();
        for (HBox headerRow : headerRows) {
            TextField keyField = (TextField) headerRow.getChildren().get(0);
            TextField valueField = (TextField) headerRow.getChildren().get(1);
            if (!keyField.getText().trim().isEmpty()) {
                this.headers.put(keyField.getText(), valueField.getText());
            }
        }
        this.body = bodyArea.getText();
    }

    /**
     * 导出数据源配置
     * 
     * @return 返回JSON格式的配置字符串
     */
    @Override
    public String exportSource() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入数据源配置
     * 
     * @param exportValueString JSON格式的配置字符串
     */
    @Override
    public void importSource(String exportValueString) {
        DataSourceHttp dataSource = JSON.parseObject(exportValueString, DataSourceHttp.class);
        this.url = dataSource.url;
        this.method = dataSource.method;
        this.headers = dataSource.headers;
        this.body = dataSource.body;
        this.responseType = dataSource.responseType;
    }

    /**
     * 获取请求URL
     * 
     * @return 请求URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置请求URL
     * 
     * @param url 请求URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取请求方法
     * 
     * @return 请求方法
     */
    public String getMethod() {
        return method;
    }

    /**
     * 设置请求方法
     * 
     * @param method 请求方法
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * 获取请求头
     * 
     * @return 请求头Map
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置请求头
     * 
     * @param headers 请求头Map
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * 获取请求体
     * 
     * @return 请求体内容
     */
    public String getBody() {
        return body;
    }

    /**
     * 设置请求体
     * 
     * @param body 请求体内容
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * 获取响应类型
     * 
     * @return 响应类型
     */
    public String getResponseType() {
        return responseType;
    }

    /**
     * 设置响应类型
     * 
     * @param responseType 响应类型
     */
    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }
}
