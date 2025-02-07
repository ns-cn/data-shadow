package com.tangyujun.datashadow.datasource.file;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CSV数据源测试类
 * 用于测试CSV格式数据源的读取和验证功能
 */
class DataSourceCsvTest {

    private DataSourceCsv csv;

    /**
     * 保持setUp方法调用，避免unsed警告
     */
    @SuppressWarnings("unused")
    private void keep() {
        setUp();
    }

    @BeforeEach
    void setUp() {
        csv = new DataSourceCsv();
    }

    /**
     * 测试CSV文件路径验证功能
     * 验证:
     * 1. 不存在的文件路径
     * 2. 有效的CSV文件
     * 3. 空路径
     */
    @Test
    void testValid() {
        // 测试文件不存在的情况
        csv.setPath("不存在的文件.csv");
        try {
            csv.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        // 测试正确的CSV文件
        URL resource = getClass().getClassLoader().getResource("csv/test.csv");
        assertNotNull(resource, "Test file not found");
        File file = new File(resource.getFile());
        csv.setPath(file.getAbsolutePath());
        assertDoesNotThrow(() -> csv.valid());

        // 测试空路径
        csv.setPath("");
        try {
            csv.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        csv.setPath(null);
        try {
            csv.valid();
            fail("Should throw DataSourceValidException");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试从CSV文件读取数据
     * 验证:
     * 1. 返回值不为null
     * 2. 数据条数正确
     * 3. 第一行数据内容与预期一致
     */
    @Test
    void testacquireValues() throws DataAccessException {
        // 准备测试数据
        URL resource = getClass().getClassLoader().getResource("csv/test.csv");
        assertNotNull(resource, "测试文件不存在");

        // 使用File来处理路径，这样可以正确处理Windows路径
        File file = new File(resource.getFile());
        csv.setPath(file.getAbsolutePath());

        // 执行测试
        List<Map<String, Object>> values = csv.acquireValues();

        // 验证结果
        assertNotNull(values);
        assertEquals(3, values.size());

        Map<String, Object> firstRow = values.get(0);
        assertEquals("张三", firstRow.get("姓名"));
        assertEquals("25", firstRow.get("年龄"));
        assertEquals("北京", firstRow.get("城市"));
    }

    /**
     * 测试使用错误的文件扩展名时的验证
     * 预期:valid()方法应抛出DataSourceValidException异常
     */
    @Test
    void testValidWithWrongExtension() {
        csv.setPath("test.txt");
        try {
            csv.valid();
            fail("应该抛出DataSourceValidException异常");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试读取空CSV文件
     * 验证:
     * 1. 返回值不为null
     * 2. 返回空列表
     */
    @Test
    void testGetValuesWithEmptyFile() throws DataAccessException {
        // 假设存在一个空的CSV文件
        URL resource = getClass().getClassLoader().getResource("csv/empty.csv");
        assertNotNull(resource, "测试文件不存在");
        File file = new File(resource.getFile());
        csv.setPath(file.getAbsolutePath());

        List<Map<String, Object>> values = csv.acquireValues();
        assertTrue(values.isEmpty());
    }

    /**
     * 测试复杂CSV格式文件的读取
     * 验证:
     * 1. 包含逗号的字段正确读取
     * 2. 包含引号的字段正确读取
     * 3. 特殊字符处理正确
     */
    @Test
    void testComplexCsvFormat() throws DataAccessException {
        URL resource = getClass().getClassLoader().getResource("csv/complex.csv");
        assertNotNull(resource, "测试文件不存在");
        File file = new File(resource.getFile());
        csv.setPath(file.getAbsolutePath());

        List<Map<String, Object>> values = csv.acquireValues();
        assertNotNull(values);

        // 验证包含特殊字符的数据
        Map<String, Object> firstRow = values.get(0);
        assertEquals("张三,李四", firstRow.get("复杂名称")); // 验证包含逗号的字段
        assertEquals("\"测试\"数据", firstRow.get("带引号字段")); // 验证包含引号的字段
    }

    /**
     * 测试获取列名
     */
    @Test
    void testGetColumns() throws DataAccessException {
        // 使用测试CSV文件
        URL resource = getClass().getClassLoader().getResource("csv/test.csv");
        assertNotNull(resource, "测试文件不存在");
        File file = new File(resource.getFile());
        csv.setPath(file.getAbsolutePath());

        // 获取列名列表
        List<String> columns = csv.getColumns();

        // 验证列表不为空且包含预期的列名
        assertNotNull(columns, "列名列表不应为空");
        assertFalse(columns.isEmpty(), "列名列表不应为空");

        // 验证是否包含测试文件中的列名
        assertTrue(columns.contains("姓名"), "应包含'姓名'列");
        assertTrue(columns.contains("年龄"), "应包含'年龄'列");
        assertTrue(columns.contains("城市"), "应包含'城市'列");
    }
}