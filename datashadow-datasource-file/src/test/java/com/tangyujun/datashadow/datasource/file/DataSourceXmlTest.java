package com.tangyujun.datashadow.datasource.file;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XML数据源测试类
 */
class DataSourceXmlTest {

    private DataSourceXml dataSourceXml;
    private String validXmlPath;

    /**
     * 保持setUp()方法的调用，防止unused编译警告
     */
    @SuppressWarnings("unused")
    private void keep() {
        setUp();
    }

    @BeforeEach
    void setUp() {
        dataSourceXml = new DataSourceXml();
        // 获取测试资源文件的路径
        URL resource = getClass().getClassLoader().getResource("xml/test-data.xml");
        assertNotNull(resource, "测试文件 xml/test-data.xml 未找到");
        validXmlPath = new File(resource.getFile()).getAbsolutePath();
    }

    /**
     * 测试有效的XML文件路径
     */
    @Test
    void testValidWithValidPath() {
        dataSourceXml.setPath(validXmlPath);
        assertDoesNotThrow(() -> dataSourceXml.valid());
    }

    /**
     * 测试空路径
     */
    @Test
    void testValidWithEmptyPath() {
        dataSourceXml.setPath("");
        try {
            dataSourceXml.valid();
            fail("应该抛出DataSourceValidException异常");
        } catch (DataSourceValidException e) {
            assertEquals("XML文件路径不能为空", e.getMessage());
        }
    }

    /**
     * 测试错误的文件扩展名
     */
    @Test
    void testValidWithInvalidExtension() {
        dataSourceXml.setPath("test.txt");
        try {
            dataSourceXml.valid();
            fail("应该抛出DataSourceValidException异常");
        } catch (DataSourceValidException e) {
            assertEquals("文件不是XML格式", e.getMessage());
        }
    }

    /**
     * 测试不存在的文件
     */
    @Test
    void testValidWithNonExistentFile() {
        dataSourceXml.setPath("nonexistent.xml");
        try {
            dataSourceXml.valid();
            fail("应该抛出DataSourceValidException异常");
        } catch (DataSourceValidException e) {
            assertEquals("XML文件不存在", e.getMessage());
        }
    }

    /**
     * 测试读取XML数据
     */
    @Test
    void testGetValues() throws DataAccessException {
        dataSourceXml.setPath(validXmlPath);
        List<Map<String, Object>> values = dataSourceXml.getValues();

        // 验证返回的数据列表
        assertNotNull(values);
        assertEquals(3, values.size());

        // 验证第一条数据
        Map<String, Object> firstRow = values.get(0);
        assertEquals("1", firstRow.get("id"));
        assertEquals("张三", firstRow.get("name"));
        assertEquals("20", firstRow.get("age"));
        assertEquals("A", firstRow.get("grade"));

        // 验证第二条数据
        Map<String, Object> secondRow = values.get(1);
        assertEquals("2", secondRow.get("id"));
        assertEquals("李四", secondRow.get("name"));
        assertEquals("21", secondRow.get("age"));
        assertEquals("B", secondRow.get("grade"));
    }

    /**
     * 测试读取格式错误的XML文件
     */
    @Test
    void testGetValuesWithInvalidXml() {
        dataSourceXml.setPath("invalid.xml");
        try {
            dataSourceXml.getValues();
            fail("应该抛出DataAccessException异常");
        } catch (DataAccessException e) {
            assertTrue(e.getMessage().contains("读取XML文件失败"));
        }
    }
}