package com.tangyujun.datashadow.model.datasource.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.tangyujun.datashadow.model.exception.DataAccessException;
import com.tangyujun.datashadow.model.exception.DataSourceValidException;

/**
 * Oracle数据源测试类
 */
public class DataSourceOracleTest {

    private DataSourceOracle dataSource;
    private static final String TEST_URL = "jdbc:oracle:thin:@127.0.0.1:1521/XE";
    private static final String TEST_USERNAME = "dbo";
    private static final String TEST_PASSWORD = "caecaodb";

    /**
     * 保持setUp()方法的调用，防止unused编译警告
     */
    @SuppressWarnings("unused")
    private void keep() {
        setUp();
    }

    @BeforeEach
    void setUp() {
        dataSource = new DataSourceOracle();
        dataSource.setUrl(TEST_URL);
        dataSource.setUsername(TEST_USERNAME);
        dataSource.setPassword(TEST_PASSWORD);
    }

    /**
     * 测试Oracle连接验证
     */
    @Test
    void testValid() {
        assertDoesNotThrow(() -> dataSource.valid(), "Oracle connection validation should succeed");

        // 测试无效的连接信息
        DataSourceOracle invalidDataSource = new DataSourceOracle();
        try {
            invalidDataSource.valid();
            fail("Should throw DataSourceValidException when connection info is empty");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        invalidDataSource.setUrl("jdbc:oracle:thin:@invalid:1521/XE");
        invalidDataSource.setUsername(TEST_USERNAME);
        invalidDataSource.setPassword(TEST_PASSWORD);
        try {
            invalidDataSource.valid();
            fail("Should throw DataSourceValidException when database URL is invalid");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试查询数据
     */
    @Test
    void testGetValues() {
        // 设置一个简单的查询语句
        dataSource.setSql("SELECT 1 as TEST_COL FROM DUAL");

        try {
            var result = dataSource.getValues();
            assertNotNull(result, "Query result should not be null");
            assertFalse(result.isEmpty(), "Query result should contain data");
            assertEquals(1, result.size(), "Should return one row");
            assertEquals(1, result.get(0).get("TEST_COL"), "TEST_COL value should be 1");
        } catch (DataAccessException e) {
            fail("Query execution should not throw exception: " + e.getMessage());
        }
    }

    /**
     * 测试无效SQL查询
     */
    @Test
    void testInvalidQuery() {
        dataSource.setSql("SELECT * FROM NON_EXISTENT_TABLE");

        try {
            dataSource.getValues();
            fail("Should throw DataAccessException when querying non-existent table");
        } catch (DataAccessException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试复杂查询
     */
    @Test
    void testComplexQuery() {
        // 这里使用一个稍微复杂的查询，包含多个列和条件
        dataSource.setSql("""
                SELECT
                    SYSDATE as CURRENT_DATE,
                    USER as CURRENT_USER,
                    'TEST' as TEST_STRING
                FROM DUAL
                """);

        try {
            var result = dataSource.getValues();
            assertNotNull(result, "查询结果不应为空");
            assertFalse(result.isEmpty(), "查询结果应该包含数据");

            var row = result.get(0);
            assertNotNull(row.get("CURRENT_DATE"), "CURRENT_DATE不应为空");
            assertNotNull(row.get("CURRENT_USER"), "CURRENT_USER不应为空");
            assertEquals("TEST", row.get("TEST_STRING"), "TEST_STRING应该等于'TEST'");
        } catch (DataAccessException e) {
            fail("查询执行不应抛出异常: " + e.getMessage());
        }
    }
}