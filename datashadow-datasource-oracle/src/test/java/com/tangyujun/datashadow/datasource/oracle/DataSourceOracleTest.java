package com.tangyujun.datashadow.datasource.oracle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
        DataSourceValidException exception = assertThrows(DataSourceValidException.class,
                invalidDataSource::valid,
                "Should throw DataSourceValidException when connection info is empty");
        assertNotNull(exception.getMessage(), "异常信息不应为空");

        invalidDataSource.setUrl("jdbc:oracle:thin:@invalid:1521/XE");
        invalidDataSource.setUsername(TEST_USERNAME);
        invalidDataSource.setPassword(TEST_PASSWORD);
        exception = assertThrows(DataSourceValidException.class,
                invalidDataSource::valid,
                "Should throw DataSourceValidException when database URL is invalid");
        assertNotNull(exception.getMessage(), "异常信息不应为空");
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

            // 将BigDecimal转换为Integer进行比较
            Object value = result.get(0).get("TEST_COL");
            assertTrue(value instanceof java.math.BigDecimal, "值应该是BigDecimal类型");
            assertEquals(1, ((java.math.BigDecimal) value).intValue(), "TEST_COL value should be 1");
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

    /**
     * 测试获取列名
     */
    @Test
    void testGetColumns() throws DataAccessException {
        dataSource.setSql("SELECT 1 as id, 'test' as name FROM DUAL");
        List<String> columns = dataSource.getColumns();
        assertEquals(2, columns.size());
        assertTrue(columns.contains("ID"));
        assertTrue(columns.contains("NAME"));
    }
}
