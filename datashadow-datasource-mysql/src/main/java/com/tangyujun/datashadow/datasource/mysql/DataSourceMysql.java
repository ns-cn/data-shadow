package com.tangyujun.datashadow.datasource.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Window;

/**
 * MySQL数据源
 * 用于连接MySQL数据库并读取数据
 * 继承自DataSourceDb抽象类
 */
public class DataSourceMysql extends DataSource {
    private static final long serialVersionUID = 1L;

    /**
     * 数据库连接URL
     */
    protected String url;

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
     * 获取数据库连接URL
     * 
     * @return 数据库连接URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置数据库连接URL
     * 
     * @param url 数据库连接URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

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
     * 注册MySQL数据源生成器
     * 
     * @return 数据源生成器
     */
    @DataSourceRegistry(friendlyName = "MySQL")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceMysql();
    }

    /**
     * 验证MySQL数据源连接是否有效
     * 
     * @return 如果连接成功返回true,否则返回false
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (this.url == null || this.url.isBlank() || this.username == null
                || this.username.isBlank() || this.password == null) {
            throw new DataSourceValidException("MySQL连接验证失败: 连接信息不完整", null);
        }
        try {
            System.out.println("正在验证MySQL连接...");
            System.out.println("URL: " + url);
            System.out.println("用户名: " + username);
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (@SuppressWarnings("unused")
            var connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("MySQL连接验证成功！");
            }
        } catch (ClassNotFoundException e) {
            throw new DataSourceValidException("MySQL驱动加载失败: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new DataSourceValidException("MySQL连接验证失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从MySQL数据库中获取数据
     * 执行sql语句并将结果转换为List<Map<String, Object>>格式
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws SQLException 当SQL执行失败或数据库连接出错时抛出
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL驱动加载失败", e);
        }
        try (var connection = DriverManager.getConnection(url, username, password);
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
            throw new DataAccessException("执行MySQL查询失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取MySQL数据源的列名
     * 通过执行SQL语句并解析结果集的元数据来获取列名
     * 
     * @return 列名列表,包含查询结果中所有列的名称
     */
    @Override
    public List<String> getColumns() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DataAccessException("MySQL驱动加载失败", e);
        }
        try (var connection = DriverManager.getConnection(url, username, password);
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
            throw new DataAccessException("获取MySQL列名失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取数据源描述
     * 
     * @return 数据源描述
     */
    @Override
    public String getDescription() {
        return url;
    }

    /**
     * 配置MySQL数据源
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'configure'");
    }
}
