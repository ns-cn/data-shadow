package com.tangyujun.datashadow.datasource.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

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

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import lombok.EqualsAndHashCode;

/**
 * CSV数据源
 * 支持读取CSV格式的文件
 * 将CSV表格数据转换为结构化数据
 * 支持自定义文件编码
 */
@EqualsAndHashCode(callSuper = true)
public class DataSourceCsv extends DataSourceFile {

    /**
     * CSV文件的编码格式
     * 如果未指定则默认使用UTF-8编码
     */
    private String encoding;

    /**
     * 验证CSV文件路径是否正确
     * 检查文件是否存在、可读、格式是否正确
     * 
     * @throws DataSourceValidException 当CSV文件路径格式错误或文件不可读时抛出
     */
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

    /**
     * 从CSV文件中获取数据
     * 读取CSV文件的所有数据,第一行作为表头
     * 支持自定义编码格式读取
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当CSV文件读取失败时抛出
     */
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
     * 如果不设置则默认使用UTF-8编码
     * 
     * @param encoding 编码格式,如UTF-8、GBK等
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
