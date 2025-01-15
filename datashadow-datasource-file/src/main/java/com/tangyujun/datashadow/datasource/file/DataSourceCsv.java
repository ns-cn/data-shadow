package com.tangyujun.datashadow.datasource.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Window;

/**
 * CSV数据源
 * 支持读取CSV格式的文件
 * 将CSV表格数据转换为结构化数据
 * 支持自定义文件编码
 */
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

    /**
     * 重写equals方法
     * 用于比较两个DataSourceCsv对象是否相等
     * 如果两个对象的path和encoding属性都相等,则认为这两个对象相等
     *
     * @param o 要比较的对象
     * @return 如果对象相等返回true,否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DataSourceCsv that = (DataSourceCsv) o;
        return Objects.equals(encoding, that.encoding);
    }

    /**
     * 重写hashCode方法
     * 根据对象的path和encoding属性生成哈希码
     *
     * @return 对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encoding);
    }

    /**
     * 获取CSV文件的列名
     * 
     * @return 列名列表
     */
    @Override
    public List<String> getColumns() {
        try (Reader reader = Files.newBufferedReader(Paths.get(path),
                encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding))) {
            CSVParser parser = CSVFormat.DEFAULT.parse(reader);
            CSVRecord firstRecord = parser.iterator().next();
            return StreamSupport.stream(firstRecord.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据源的描述信息
     * 用于在界面上显示数据源的基本信息
     * 例如: CSV文件: D:/test.csv(UTF-8)
     * 
     * @return 数据源的描述信息字符串
     */
    @Override
    public String getDescription() {
        if (path == null || path.isBlank()) {
            return "";
        }
        return "CSV文件: " + path + "(" + encoding + ")";
    }

    /**
     * 配置CSV文件数据源
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'configure'");
    }
}
