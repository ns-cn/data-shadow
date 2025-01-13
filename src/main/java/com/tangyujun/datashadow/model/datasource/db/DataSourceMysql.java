package com.tangyujun.datashadow.model.datasource.db;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.model.datasource.DataSourceDb;
import com.tangyujun.datashadow.model.exception.DataAccessException;
import com.tangyujun.datashadow.model.exception.DataSourceValidException;

/**
 * MySQL数据源
 * 用于连接MySQL数据库并读取数据
 * 继承自DataSourceDb抽象类
 */
public class DataSourceMysql extends DataSourceDb {

    /**
     * 验证MySQL数据源连接是否有效
     * 
     * @return 如果连接成功返回true,否则返回false
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (url == null || url.isBlank() || username == null
                || username.isBlank() || password == null) {
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
}
