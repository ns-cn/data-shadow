package com.tangyujun.datashadow.datasource.oracle;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Oracle数据源
 * 用于连接Oracle数据库并读取数据
 * 继承自DataSource抽象类
 */
public class DataSourceOracle extends DataSource {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceOracle.class);

    /**
     * 生成Oracle数据源
     * 
     * @return Oracle数据源
     */
    @DataSourceRegistry(group = "数据库", friendlyName = "Oracle")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceOracle();
    }

    /**
     * 数据库主机地址
     */
    protected String host;

    /**
     * 数据库端口
     */
    protected int port = 1521;

    /**
     * 数据库服务名/SID
     */
    protected String service;

    /**
     * 是否使用SID模式(默认使用服务名模式)
     */
    protected boolean useSid = false;

    /**
     * 构建数据库连接URL
     * 
     * @return 数据库连接URL
     */
    protected String buildUrl() {
        if (useSid) {
            return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, service);
        } else {
            return String.format("jdbc:oracle:thin:@%s:%d/%s", host, port, service);
        }
    }

    /**
     * 获取数据库主机地址
     * 
     * @return 数据库主机地址
     */
    public String getHost() {
        return host;
    }

    /**
     * 设置数据库主机地址
     * 
     * @param host 数据库主机地址
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * 获取数据库端口
     * 
     * @return 数据库端口
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置数据库端口
     * 
     * @param port 数据库端口
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 获取数据库服务名/SID
     * 
     * @return 数据库服务名/SID
     */
    public String getService() {
        return service;
    }

    /**
     * 设置数据库服务名/SID
     * 
     * @param service 数据库服务名/SID
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * 是否使用SID模式
     * 
     * @return 是否使用SID模式
     */
    public boolean isUseSid() {
        return useSid;
    }

    /**
     * 设置是否使用SID模式
     * 
     * @param useSid 是否使用SID模式
     */
    public void setUseSid(boolean useSid) {
        this.useSid = useSid;
    }

    /**
     * 数据库用户名
     */
    protected String username;

    /**
     * 数据库密码
     */
    protected String password;

    /**
     * 查询SQL语句
     */
    protected String sql;

    /**
     * 获取数据库用户名
     * 
     * @return 数据库用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置数据库用户名
     * 
     * @param username 数据库用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取数据库密码
     * 
     * @return 数据库密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置数据库密码
     * 
     * @param password 数据库密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取查询SQL语句
     * 
     * @return 查询SQL语句
     */
    public String getSql() {
        return sql;
    }

    /**
     * 设置查询SQL语句
     * 
     * @param sql 查询SQL语句
     */
    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * 验证Oracle数据源连接是否有效
     * 
     * @return 如果连接成功返回true,否则返回false
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (host == null || host.isBlank() ||
                service == null || service.isBlank() ||
                username == null || username.isBlank() ||
                password == null) {
            throw new DataSourceValidException("Oracle连接验证失败: 连接信息不完整", null);
        }
        try {
            System.out.println("正在验证Oracle连接...");
            String url = buildUrl();
            System.out.println("URL: " + url);
            System.out.println("用户名: " + username);
            Class.forName("oracle.jdbc.OracleDriver");
            try (var connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Oracle连接验证成功！");
                System.out.println("连接信息: " + connection.getMetaData().getURL() + ", 用户名: "
                        + connection.getMetaData().getUserName());
            }
        } catch (ClassNotFoundException e) {
            throw new DataSourceValidException("Oracle驱动加载失败: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataSourceValidException("Oracle连接验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从Oracle数据库中获取数据
     * 执行sql语句并将结果转换为List<Map<String, Object>>格式
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当SQL执行失败或数据库连接出错时抛出
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Oracle驱动加载失败", e);
        }
        try (var connection = DriverManager.getConnection(buildUrl(), username, password);
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)) {

            var metaData = resultSet.getMetaData();
            var columnCount = metaData.getColumnCount();
            var result = new ArrayList<Map<String, Object>>();

            while (resultSet.next()) {
                var row = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    var columnName = metaData.getColumnLabel(i);
                    var value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                result.add(row);
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException("执行Oracle查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取Oracle数据源的列名
     * 
     * @return 列名列表
     */
    @Override
    public List<String> getColumns() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("Oracle驱动加载失败", e);
        }
        try (var connection = DriverManager.getConnection(buildUrl(), username, password);
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)) {

            var metaData = resultSet.getMetaData();
            var columnCount = metaData.getColumnCount();
            var columns = new ArrayList<String>();

            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnLabel(i));
            }
            return columns;
        } catch (SQLException e) {
            throw new DataAccessException("获取Oracle列名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取Oracle数据源的描述
     * 
     * @return 数据源描述
     */
    @Override
    public String getDescription() {
        return String.format("%s:%d/%s", host, port, service);
    }

    /**
     * 配置Oracle数据源
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(primaryStage);
        stage.setTitle("Oracle数据源配置");

        // 创建主布局
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));

        // 数据库连接配置区域
        VBox connectionConfig = new VBox(10);
        Label connectionLabel = new Label("数据库连接配置");
        connectionLabel.setStyle("-fx-font-weight: bold");
        connectionConfig.getChildren().add(connectionLabel);

        // 主机和端口
        HBox hostPortBox = new HBox(10);
        Label hostLabel = new Label("主机地址:");
        hostLabel.setPrefWidth(100);
        TextField hostField = new TextField(host);
        hostField.setPromptText("localhost");
        HBox.setHgrow(hostField, Priority.ALWAYS);

        Label portLabel = new Label("端口:");
        portLabel.setPrefWidth(60);
        TextField portField = new TextField(String.valueOf(port));
        portField.setPrefWidth(100);
        portField.setPromptText("1521");

        hostPortBox.getChildren().addAll(hostLabel, hostField, portLabel, portField);

        // 服务名/SID
        HBox serviceBox = new HBox(10);
        Label serviceLabel = new Label("服务名/SID:");
        serviceLabel.setPrefWidth(100);
        TextField serviceField = new TextField(service);
        serviceField.setPromptText("XE");
        HBox.setHgrow(serviceField, Priority.ALWAYS);

        Label useSidLabel = new Label("使用SID:");
        useSidLabel.setPrefWidth(60);
        CheckBox useSidCheckBox = new CheckBox();
        useSidCheckBox.setSelected(useSid);
        HBox sidBox = new HBox(10);
        sidBox.setPrefWidth(100);
        sidBox.getChildren().add(useSidCheckBox);

        serviceBox.getChildren().addAll(serviceLabel, serviceField, useSidLabel, sidBox);

        // 用户名和密码
        GridPane credentialsGrid = new GridPane();
        credentialsGrid.setHgap(10);
        credentialsGrid.setVgap(10);

        Label usernameLabel = new Label("用户名:");
        TextField usernameField = new TextField(username);
        usernameField.setPromptText("system");
        GridPane.setHgrow(usernameField, Priority.ALWAYS);

        Label passwordLabel = new Label("密码:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText(password);
        GridPane.setHgrow(passwordField, Priority.ALWAYS);

        credentialsGrid.add(usernameLabel, 0, 0);
        credentialsGrid.add(usernameField, 1, 0);
        credentialsGrid.add(passwordLabel, 0, 1);
        credentialsGrid.add(passwordField, 1, 1);

        connectionConfig.getChildren().addAll(hostPortBox, serviceBox, credentialsGrid);

        // SQL查询配置区域
        VBox sqlConfig = new VBox(10);
        Label sqlLabel = new Label("SQL查询配置");
        sqlLabel.setStyle("-fx-font-weight: bold");
        TextArea sqlArea = new TextArea(sql);
        sqlArea.setPromptText("SELECT * FROM table");
        VBox.setVgrow(sqlArea, Priority.ALWAYS);
        sqlConfig.getChildren().addAll(sqlLabel, sqlArea);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button testButton = new Button("测试连接");
        Button confirmButton = new Button("确定");
        Button cancelButton = new Button("取消");

        buttonBox.getChildren().addAll(testButton, confirmButton, cancelButton);

        // 添加所有组件到主布局
        mainLayout.getChildren().addAll(connectionConfig, sqlConfig, buttonBox);
        VBox.setVgrow(sqlConfig, Priority.ALWAYS);

        // 设置按钮事件
        testButton.setOnAction(event -> {
            try {
                DataSourceOracle testDs = new DataSourceOracle();
                testDs.setHost(hostField.getText());
                testDs.setPort(Integer.parseInt(portField.getText()));
                testDs.setService(serviceField.getText());
                testDs.setUseSid(useSidCheckBox.isSelected());
                testDs.setUsername(usernameField.getText());
                testDs.setPassword(passwordField.getText());
                testDs.valid();
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("连接测试");
                alert.setHeaderText(null);
                alert.setContentText("连接成功！");
                alert.showAndWait();
            } catch (DataSourceValidException | NumberFormatException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("连接测试");
                alert.setHeaderText(null);
                alert.setContentText("连接失败：" + e.getMessage());
                alert.showAndWait();
            }
        });

        confirmButton.setOnAction(event -> {
            try {
                setHost(hostField.getText());
                setPort(Integer.parseInt(portField.getText()));
                setService(serviceField.getText());
                setUseSid(useSidCheckBox.isSelected());
                setUsername(usernameField.getText());
                setPassword(passwordField.getText());
                setSql(sqlArea.getText());
                callback.onConfigureFinished();
                stage.close();
            } catch (NumberFormatException e) {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("配置错误");
                alert.setHeaderText(null);
                alert.setContentText("端口号必须是数字");
                alert.showAndWait();
            }
        });

        cancelButton.setOnAction(event -> stage.close());

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /**
     * 导出为JSON，包含dataType和originData和父类的字段
     * 例如：{"host":"localhost","port":1521,"service":"XE","username":"system","password":"******","sql":"SELECT
     * * FROM table"}
     */
    @Override
    public String exportSource() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入为JSON，包含dataType和originData和父类的字段
     * 例如：{"host":"localhost","port":1521,"service":"XE","username":"system","password":"******","sql":"SELECT
     * * FROM table","mappings":{}}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void importSource(String exportValueString) {
        Map<String, Object> map = JSON.parseObject(exportValueString, new TypeReference<Map<String, Object>>() {
        });
        if (map == null) {
            return;
        }
        try {
            this.setHost((String) map.get("host"));
            this.setPort((Integer) map.get("port"));
            this.setService((String) map.get("service"));
            this.setUsername((String) map.get("username"));
            this.setPassword((String) map.get("password"));
            this.setSql((String) map.get("sql"));
            this.setUseSid((Boolean) map.get("useSid"));
            this.setMappings((Map<String, String>) map.get("mappings"));
        } catch (Exception e) {
            logger.error("解析数据源配置时发生错误: " + e.getMessage());
        }
    }
}
