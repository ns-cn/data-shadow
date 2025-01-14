package com.tangyujun.datashadow.datasource.file;

import org.junit.jupiter.api.Test;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Excel数据源测试类
 * 用于测试Excel格式(.xls和.xlsx)数据源的读取和验证功能
 */
class DataSourceExcelTest {

    /**
     * 测试Excel文件路径验证功能
     * 验证:
     * 1. 不存在的文件路径
     * 2. .xlsx格式文件
     * 3. .xls格式文件
     * 4. 空路径
     * 5. 错误的文件扩展名
     */
    @Test
    void testValid() {
        DataSourceExcel excel = new DataSourceExcel();

        // 测试文件不存在的情况
        excel.setPath("不存在的文件.xlsx");
        try {
            excel.valid();
            fail("Should throw DataSourceValidException when file does not exist");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        // 测试.xlsx格式
        URL xlsxResource = getClass().getClassLoader().getResource("excel/test.xlsx");
        assertNotNull(xlsxResource, "Test xlsx file not found");
        File xlsxFile = new File(xlsxResource.getFile());
        excel.setPath(xlsxFile.getAbsolutePath());
        assertDoesNotThrow(() -> excel.valid());

        // 测试.xls格式
        URL xlsResource = getClass().getClassLoader().getResource("excel/test.xls");
        assertNotNull(xlsResource, "Test xls file not found");
        File xlsFile = new File(xlsResource.getFile());
        excel.setPath(xlsFile.getAbsolutePath());
        assertDoesNotThrow(() -> excel.valid());

        // 测试空路径
        excel.setPath("");
        try {
            excel.valid();
            fail("Should throw DataSourceValidException when path is empty");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        excel.setPath(null);
        try {
            excel.valid();
            fail("Should throw DataSourceValidException when path is null");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }

        // 测试错误的文件扩展名
        excel.setPath("test.txt");
        try {
            excel.valid();
            fail("Should throw DataSourceValidException when file extension is not .xls or .xlsx");
        } catch (DataSourceValidException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试.xlsx格式Excel文件的数据读取
     * 验证:
     * 1. 返回值不为null
     * 2. 数据条数正确
     * 3. 第一行数据内容与预期一致
     */
    @Test
    void testGetValuesXlsx() throws DataAccessException {
        // 测试.xlsx格式
        DataSourceExcel excel = new DataSourceExcel();
        URL resource = getClass().getClassLoader().getResource("excel/test.xlsx");
        assertNotNull(resource, "xlsx测试文件不存在");
        File file = new File(resource.getFile());
        excel.setPath(file.getAbsolutePath());

        // 执行测试
        List<Map<String, Object>> values = excel.getValues();

        // 验证结果
        assertNotNull(values);
        assertEquals(3, values.size());

        Map<String, Object> firstRow = values.get(0);
        assertEquals("张三", firstRow.get("姓名"));
        assertEquals(25.0, firstRow.get("年龄"));
        assertEquals("北京", firstRow.get("城市"));
    }

    /**
     * 测试.xls格式Excel文件的数据读取
     * 验证:
     * 1. 返回值不为null
     * 2. 数据条数正确
     * 3. 第一行数据内容与预期一致
     */
    @Test
    void testGetValuesXls() throws DataAccessException {
        // 测试.xls格式
        DataSourceExcel excel = new DataSourceExcel();
        URL resource = getClass().getClassLoader().getResource("excel/test.xls");
        assertNotNull(resource, "xls测试文件不存在");
        File file = new File(resource.getFile());
        excel.setPath(file.getAbsolutePath());

        // 执行测试
        List<Map<String, Object>> values = excel.getValues();

        // 验证结果
        assertNotNull(values);
        assertEquals(3, values.size());

        Map<String, Object> firstRow = values.get(0);
        assertEquals("张三", firstRow.get("姓名"));
        assertEquals(25.0, firstRow.get("年龄"));
        assertEquals("北京", firstRow.get("城市"));
    }

    /**
     * 测试空Excel文件的数据读取
     * 验证:
     * 1. 返回空列表
     */
    @Test
    void testGetValuesWithEmptyFile() throws DataAccessException {
        DataSourceExcel excel = new DataSourceExcel();
        URL resource = getClass().getClassLoader().getResource("excel/empty.xlsx");
        assertNotNull(resource, "测试文件不存在");
        File file = new File(resource.getFile());
        excel.setPath(file.getAbsolutePath());

        List<Map<String, Object>> values = excel.getValues();
        assertTrue(values.isEmpty());
    }

    /**
     * 测试复杂Excel格式的数据读取
     * 验证:
     * 1. 日期类型数据
     * 2. 数值类型数据
     * 3. 布尔类型数据
     * 4. 公式计算结果
     */
    @Test
    void testComplexExcelFormat() throws DataAccessException {
        DataSourceExcel excel = new DataSourceExcel();
        URL resource = getClass().getClassLoader().getResource("excel/complex.xlsx");
        assertNotNull(resource, "测试文件不存在");
        File file = new File(resource.getFile());
        excel.setPath(file.getAbsolutePath());

        List<Map<String, Object>> values = excel.getValues();
        assertNotNull(values);
        assertFalse(values.isEmpty());

        Map<String, Object> firstRow = values.get(0);

        // 测试日期类型
        assertTrue(firstRow.get("出生日期") instanceof java.util.Date);

        // 测试数值类型
        assertEquals(1234.56, firstRow.get("工资"));

        // 测试布尔类型
        assertEquals(true, firstRow.get("是否在职"));

        // 测试公式
        Object formulaResult = firstRow.get("年终奖");
        assertTrue(formulaResult instanceof Double || formulaResult instanceof String);
    }
}