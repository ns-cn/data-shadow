package com.tangyujun.datashadow.datasource.file;

import java.io.File;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 * Excel数据源
 * 支持读取.xls和.xlsx格式的Excel文件
 * 将Excel表格数据转换为结构化数据
 */
public class DataSourceExcel extends DataSourceFile {

    /**
     * 工作表名称
     */
    private String sheetName;

    /**
     * 注册Excel数据源生成器
     * 
     * @return 数据源生成器
     */
    @DataSourceRegistry(group = "文件", friendlyName = "Excel")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceExcel();
    }

    /**
     * 验证Excel文件路径是否正确
     * 
     * @throws DataSourceValidException 当Excel文件路径格式错误或文件不可读时抛出
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (path == null || path.isBlank()) {
            throw new DataSourceValidException("Excel文件路径不能为空", null);
        }
        try {
            if (!Files.isReadable(Paths.get(path))) {
                throw new DataSourceValidException("Excel文件路径不可读", null);
            }
            String lowercasePath = path.toLowerCase();
            if (!lowercasePath.endsWith(".xls") && !lowercasePath.endsWith(".xlsx")) {
                throw new DataSourceValidException("Excel文件路径格式错误", null);
            }

            try (FileInputStream fis = new FileInputStream(path);
                    Workbook workbook = lowercasePath.endsWith(".xlsx") ? new XSSFWorkbook(fis)
                            : new HSSFWorkbook(fis)) {
                Sheet sheet;
                if (sheetName != null && !sheetName.isBlank()) {
                    sheet = workbook.getSheet(sheetName);
                    if (sheet == null) {
                        throw new DataSourceValidException("Excel工作表不存在: " + sheetName, null);
                    }
                } else {
                    sheet = workbook.getSheetAt(0);
                    if (sheet == null) {
                        throw new DataSourceValidException("Excel文件路径格式错误", null);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataSourceValidException("Excel文件路径格式错误", e);
        }
    }

    /**
     * 从Excel文件中获取数据
     * 读取指定工作表的数据,第一行作为表头
     * 如果未指定工作表名称则读取第一个工作表
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当Excel文件读取失败时抛出
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String lowercasePath = path.toLowerCase();
            try (FileInputStream fis = new FileInputStream(path);
                    Workbook workbook = lowercasePath.endsWith(".xlsx") ? new XSSFWorkbook(fis)
                            : new HSSFWorkbook(fis)) {

                Sheet sheet;
                if (sheetName != null && !sheetName.isBlank()) {
                    sheet = workbook.getSheet(sheetName);
                } else {
                    sheet = workbook.getSheetAt(0);
                }

                Row headerRow = sheet.getRow(0);
                List<String> headers = new ArrayList<>();

                // 读取表头
                for (Cell cell : headerRow) {
                    headers.add(cell.getStringCellValue());
                }

                // 读取数据行
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    Map<String, Object> rowData = new HashMap<>();
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            rowData.put(headers.get(j), getCellValue(cell));
                        }
                    }
                    result.add(rowData);
                }
            }
        } catch (IOException | IllegalArgumentException | IllegalStateException e) {
            throw new DataAccessException("读取Excel文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 获取单元格的值
     * 根据单元格类型返回相应的Java对象
     * 支持字符串、数字、日期、布尔值和公式类型
     * 
     * @param cell Excel单元格对象
     * @return 单元格的值,类型可能为String、Double、Date或Boolean
     */
    private Object getCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue();
                }
                yield cell.getNumericCellValue();
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (IllegalStateException e) {
                    yield cell.getNumericCellValue();
                }
            }
            default -> "";
        };
    }

    /**
     * 获取Excel文件的列名
     * 读取Excel文件第一行作为列名
     * 如果指定了工作表名称则读取指定工作表,否则读取第一个工作表
     * 
     * @return 列名列表,如果读取失败则返回空列表
     */
    @Override
    public List<String> getColumns() {
        try (Workbook workbook = WorkbookFactory.create(new File(path))) {
            Sheet sheet;
            if (sheetName != null && !sheetName.isBlank()) {
                sheet = workbook.getSheet(sheetName);
            } else {
                sheet = workbook.getSheetAt(0);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return new ArrayList<>();
            }
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }
            return headers;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据源的描述信息
     * 用于在界面上显示数据源的基本信息
     * 例如: Excel文件: D:/test.xlsx(Sheet1)
     * 
     * @return 数据源的描述信息字符串
     */
    @Override
    public String getDescription() {
        if (path == null || path.isBlank()) {
            return "";
        }
        return "Excel文件: " + path + (sheetName != null ? "(" + sheetName + ")" : "");
    }

    /**
     * 获取当前选中的工作表名称
     * 
     * @return 工作表名称,如果未指定则返回null
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * 设置要读取的工作表名称
     * 如果不设置则默认读取第一个工作表
     * 
     * @param sheetName 工作表名称
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * 导出为JSON，包含dataType和originData和父类的字段
     * 例如：{"path":"/path/to/file.xlsx","sheetName":"Sheet1","mappings":{}}
     */
    @Override
    public String exportSource() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入为JSON，包含dataType和originData和父类的字段
     * 例如：{"path":"/path/to/file.xlsx","sheetName":"Sheet1","mappings":{}}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void importSource(String exportValueString) {
        // 解析JSON
        Map<String, Object> map = JSON.parseObject(exportValueString, new TypeReference<Map<String, Object>>() {
        });
        if (map == null) {
            return;
        }
        // 解析数据
        try {
            this.setPath((String) map.get("path"));
            this.setSheetName((String) map.get("sheetName"));
            this.setMappings((Map<String, String>) map.get("mappings"));
        } catch (Exception e) {
        }
    }
}
