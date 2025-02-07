package com.tangyujun.datashadow.datasource.file;

import org.junit.jupiter.api.Test;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * JSON数据源测试类
 * 用于测试JSON格式数据源的读取和验证功能
 */
public class DataSourceJsonTest {

    /**
     * JSON数据源对象
     */
    private DataSourceJson dataSource;

    /**
     * 有效的JSON测试文件路径
     */
    private final String validJsonPath = "src/test/resources/json/test.json";

    /**
     * 不存在的JSON文件路径,用于测试无效路径场景
     */
    private final String invalidPath = "src/test/resources/json/nonexistent.json";

    /**
     * 错误的文件扩展名路径,用于测试非JSON文件场景
     */
    private final String invalidExtension = "src/test/resources/test.txt";

    /**
     * 测试使用正确的JSON文件路径时的验证
     * 预期:valid()方法不应抛出异常
     */
    @Test
    void testValidWithCorrectPath() {
        dataSource = new DataSourceJson();
        dataSource.setPath(validJsonPath);
        try {
            dataSource.valid();
        } catch (DataSourceValidException e) {
            fail("Valid JSON file path should not throw exception");
        }
    }

    /**
     * 测试使用不存在的文件路径时的验证
     * 预期:valid()方法应抛出DataAccessException
     */
    @Test
    void testValidWithInvalidPath() {
        dataSource = new DataSourceJson();
        dataSource.setPath(invalidPath);
        try {
            dataSource.valid();
            fail("Should throw DataSourceValidException when JSON file does not exist");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试使用错误的文件扩展名时的验证
     * 预期:valid()方法应抛出DataAccessException
     */
    @Test
    void testValidWithInvalidExtension() {
        dataSource = new DataSourceJson();
        dataSource.setPath(invalidExtension);
        try {
            dataSource.valid();
            fail("Should throw DataSourceValidException when file extension is not .json");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试从有效JSON文件读取数据
     * 验证:
     * 1. 返回值不为null
     * 2. 数据条数正确
     * 3. 数据内容与预期一致
     */
    @Test
    void testacquireValues() throws DataAccessException {
        dataSource = new DataSourceJson();
        dataSource.setPath(validJsonPath);
        List<Map<String, Object>> values = dataSource.acquireValues();

        assertNotNull(values, "返回的列表不应为null");
        assertEquals(3, values.size(), "应该包含3条记录");

        // 验证第一条记录
        Map<String, Object> firstRecord = values.get(0);
        assertEquals("张三", firstRecord.get("姓名"));
        assertEquals(25, firstRecord.get("年龄"));
        assertEquals("北京", firstRecord.get("城市"));

        // 验证第二条记录
        Map<String, Object> secondRecord = values.get(1);
        assertEquals("李四", secondRecord.get("姓名"));
        assertEquals(30, secondRecord.get("年龄"));
        assertEquals("上海", secondRecord.get("城市"));
    }

    /**
     * 测试使用无效路径读取数据
     * 验证:
     * 1. 返回值不为null
     * 2. 返回空列表
     */
    /**
     * 测试使用无效路径读取数据时的异常处理
     * 验证:
     * 1. 应该抛出DataAccessException异常
     */
    @Test
    void testGetValuesWithInvalidPath() {
        dataSource = new DataSourceJson();
        dataSource.setPath(invalidPath);
        try {
            dataSource.acquireValues();
            fail("Should throw DataAccessException when reading from non-existent JSON file");
        } catch (DataAccessException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试获取列名
     */
    @Test
    void testGetColumns() throws DataAccessException {
        // 使用测试JSON文件
        dataSource = new DataSourceJson();
        dataSource.setPath(validJsonPath);

        // 获取列名列表
        List<String> columns = dataSource.getColumns();

        // 验证列表不为空且包含预期的列名
        assertNotNull(columns, "列名列表不应为空");
        assertFalse(columns.isEmpty(), "列名列表不应为空");

        // 验证是否包含测试文件中的列名
        assertTrue(columns.contains("姓名"), "应包含'姓名'列");
        assertTrue(columns.contains("年龄"), "应包含'年龄'列");
        assertTrue(columns.contains("城市"), "应包含'城市'列");
    }
}