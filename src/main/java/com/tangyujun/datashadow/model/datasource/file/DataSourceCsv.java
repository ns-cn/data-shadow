package com.tangyujun.datashadow.model.datasource.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.tangyujun.datashadow.model.datasource.DataSourceFile;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.model.exception.DataAccessException;
import com.tangyujun.datashadow.model.exception.DataSourceValidException;

public class DataSourceCsv extends DataSourceFile {

    private String encoding;

    @Override
    public void valid() throws DataSourceValidException {
        if (path == null || path.isBlank()) {
            throw new DataSourceValidException("CSV文件路径不能为空", null);
        }
        try {
            if (!Files.isReadable(Paths.get(path))) {
                throw new DataSourceValidException("CSV文件路径不可读", null);
            }
            // 检查文件扩展名
            if (!path.toLowerCase().endsWith(".csv")) {
                throw new DataSourceValidException("CSV文件路径格式错误", null);
            }
            // 尝试读取CSV文件验证格式
            try (FileReader reader = new FileReader(path,
                    encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
                    CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().build().parse(reader)) {
                if (parser.getHeaderMap() == null || parser.getHeaderMap().isEmpty()) {
                    throw new DataSourceValidException("CSV文件路径格式错误", null);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataSourceValidException("CSV文件路径格式错误", e);
        }
    }

    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (FileReader reader = new FileReader(path,
                encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().build().parse(reader)) {

            for (CSVRecord record : parser) {
                Map<String, Object> rowData = new HashMap<>();
                parser.getHeaderNames().forEach(header -> rowData.put(header, record.get(header)));
                result.add(rowData);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataAccessException("读取CSV文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置CSV文件编码
     * 
     * @param encoding 编码
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
