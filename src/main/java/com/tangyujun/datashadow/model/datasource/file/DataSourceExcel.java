package com.tangyujun.datashadow.model.datasource.file;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tangyujun.datashadow.model.datasource.DataSourceFile;
import com.tangyujun.datashadow.model.exception.DataAccessException;
import com.tangyujun.datashadow.model.exception.DataSourceValidException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

public class DataSourceExcel extends DataSourceFile {

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
