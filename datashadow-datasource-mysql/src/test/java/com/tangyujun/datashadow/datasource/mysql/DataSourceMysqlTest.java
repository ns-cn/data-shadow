package com.tangyujun.datashadow.datasource.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import java.sql.DriverManager;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL数据源测试类
 * 用于测试MySQL数据库连接、数据读取等功能
 */
class DataSourceMysqlTest {

    /**
     * MySQL数据源对象
     */
    private DataSourceMysql dataSource;

    /**
     * 测试数据库主机地址
     */
    private static final String HOST = "sql.wsfdb.cn";

    /**
     * 测试数据库端口
     */
    private static final int PORT = 3306;

    /**
     * 测试数据库名称
     */
    private static final String DATABASE = "tangyujunshadow";

    /**
     * 测试数据库用户名
     */
    private static final String USERNAME = "tangyujunshadow";

    /**
     * 测试数据库密码
     */
    private static final String PASSWORD = "shadow";

    /**
     * 保持setUp()方法的调用，防止unused编译警告
     */
    @SuppressWarnings("unused")
    private void keep() {
        setUp();
    }

    /**
     * 每个测试方法执行前的初始化
     * 创建数据源对象并设置连接信息
     */
    @BeforeEach
    void setUp() {
        dataSource = new DataSourceMysql();
        dataSource.setHost(HOST);
        dataSource.setPort(PORT);
        dataSource.setDatabase(DATABASE);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
    }

    /**
     * 测试数据源连接验证功能
     * 验证以下场景:
     * 1. 正确的连接信息
     * 2. 错误的主机地址
     * 3. 空主机
     * 4. 错误的用户名密码
     */
    @Test
    void testValid() {
        // 测试有效连接
        assertDoesNotThrow(() -> dataSource.valid(), "MySQL connection validation should succeed");

        // 测试无效主机
        dataSource.setHost("invalid-host");
        try {
            dataSource.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        // 测试空主机
        dataSource.setHost("");
        try {
            dataSource.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        // 重置主机并测试错误的凭据
        dataSource.setHost(HOST);
        dataSource.setUsername("wrong");
        dataSource.setPassword("wrong");
        try {
            dataSource.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试简单SQL查询数据获取功能
     * 使用简单的SELECT语句验证数据读取和类型转换
     */
    @Test
    void testacquireValues() throws DataAccessException {
        // 使用简单的SELECT语句
        dataSource.setSql("SELECT 1 as num, 'test' as text");

        List<Map<String, Object>> result = dataSource.acquireValues();

        assertNotNull(result);
        assertEquals(1, result.size());

        Map<String, Object> row = result.get(0);
        assertEquals(1L, row.get("num"));
        assertEquals("test", row.get("text"));
    }

    /**
     * 测试无效SQL语句场景
     * 验证在执行不存在的表查询时是否正确抛出异常
     */
    @Test
    void testInvalidSql() throws DataAccessException {
        dataSource.setSql("SELECT * FROM non_existent_table");
        try {
            dataSource.acquireValues();
            fail("预期应该抛出DataAccessException异常");
        } catch (DataAccessException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试复杂SQL查询功能
     * 使用包含UNION ALL和ORDER BY的复杂查询验证数据读取
     */
    @Test
    void testComplexQuery() throws DataAccessException {
        // 测试复杂查询
        dataSource.setSql("""
                SELECT
                    1 as id,
                    '测试数据1' as name
                UNION ALL
                SELECT
                    2 as id,
                    '测试数据2' as name
                ORDER BY id
                """);

        List<Map<String, Object>> result = dataSource.acquireValues();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals("测试数据1", result.get(0).get("name"));
        assertEquals(2L, result.get(1).get("id"));
        assertEquals("测试数据2", result.get(1).get("name"));
    }

    /**
     * 测试直接数据库连接
     * 验证JDBC驱动加载和数据库直接连接功能
     * 输出数据库版本信息
     */
    @Test
    void testDirectConnection() {
        assertDoesNotThrow(() -> {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = String.format(
                    "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai",
                    HOST, PORT, DATABASE);
            try (var connection = DriverManager.getConnection(url, USERNAME, PASSWORD)) {
                assertNotNull(connection.getMetaData().getDatabaseProductVersion());
            }
        });
    }

    /**
     * 测试获取列名
     * 验证获取列名功能是否正常
     */
    @Test
    void testGetColumns() throws DataAccessException {
        dataSource.setSql("SELECT 1 as id, 'test' as name");
        List<String> columns = dataSource.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.contains("id"));
        assertTrue(columns.contains("name"));
    }
}