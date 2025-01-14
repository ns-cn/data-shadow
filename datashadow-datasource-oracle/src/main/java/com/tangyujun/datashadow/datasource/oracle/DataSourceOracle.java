package com.tangyujun.datashadow.datasource.oracle;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

/**
 * Oracle数据源
 * 用于连接Oracle数据库并读取数据
 * 继承自DataSource抽象类
 */
public class DataSourceOracle extends DataSource {

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
     * 验证Oracle数据源连接是否有效
     * 
     * @return 如果连接成功返回true,否则返回false
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (url == null || url.isBlank() || username == null
                || username.isBlank() || password == null) {
            throw new DataSourceValidException("Oracle连接验证失败: 连接信息不完整", null);
        }
        try {
            System.out.println("正在验证Oracle连接...");
            System.out.println("URL: " + url);
            System.out.println("用户名: " + username);
            Class.forName("oracle.jdbc.OracleDriver");
            try (@SuppressWarnings("unused")
            var connection = DriverManager.getConnection(url, username, password)) {
                System.out.println("Oracle连接验证成功！");
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
            throw new DataAccessException("执行Oracle查询失败: " + e.getMessage(), e);
        }
    }

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
        return url;
    }

}
