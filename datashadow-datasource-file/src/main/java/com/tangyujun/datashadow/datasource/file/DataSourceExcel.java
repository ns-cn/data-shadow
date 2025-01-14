package com.tangyujun.datashadow.datasource.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import lombok.EqualsAndHashCode;

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
@EqualsAndHashCode(callSuper = true)
public class DataSourceExcel extends DataSourceFile {

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
                Sheet sheet = workbook.getSheetAt(0);
                if (sheet == null) {
                    throw new DataSourceValidException("Excel文件路径格式错误", null);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataSourceValidException("Excel文件路径格式错误", e);
        }
    }

    /**
     * 从Excel文件中获取数据
     * 读取第一个工作表的数据,第一行作为表头
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

                Sheet sheet = workbook.getSheetAt(0);
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
}
