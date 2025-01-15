package com.tangyujun.datashadow.datasource.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Window;

/**
 * JSON数据源
 */
public class DataSourceJson extends DataSourceFile {

    /**
     * 注册JSON数据源生成器
     * 
     * @return 数据源生成器
     */
    @DataSourceRegistry(friendlyName = "JSON")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceJson();
    }

    /**
     * 验证JSON文件路径是否正确
     * 
     * @throws DataSourceValidException 当JSON文件路径格式错误时抛出
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (path == null || path.isBlank()) {
            throw new DataSourceValidException("JSON文件路径不能为空", null);
        }

        String lowercasePath = path.toLowerCase();
        if (!lowercasePath.endsWith(".json")) {
            throw new DataSourceValidException("文件不是JSON格式", null);
        }
        // 检查文件是否存在且可读
        File file = new File(path);
        if (!file.exists()) {
            throw new DataSourceValidException("JSON文件不存在", null);
        }
        if (!file.canRead()) {
            throw new DataSourceValidException("JSON文件无法读取", null);
        }
    }

    /**
     * 从JSON文件中获取数据
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当JSON文件读取失败时抛出
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String jsonContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path)), "UTF-8");
            result = JSON.parseObject(jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (IOException e) {
            throw new DataAccessException("读取JSON文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 获取JSON文件的列名
     * 读取JSON文件第一行作为列名
     * 
     * @return 列名列表,如果读取失败则返回空列表
     */
    @Override
    public List<String> getColumns() {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
            List<Map<String, Object>> data = JSON.parseObject(jsonContent,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            if (data != null && !data.isEmpty()) {
                // 返回第一条数据的所有键作为列名
                return new ArrayList<>(data.get(0).keySet());
            }
            return new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据源的描述信息
     * 用于在界面上显示数据源的基本信息
     * 例如: JSON文件: D:/test.json
     * 
     * @return 数据源的描述信息字符串
     */
    @Override
    public String getDescription() {
        if (path == null || path.isBlank()) {
            return "";
        }
        return "JSON文件: " + path;
    }

    /**
     * 配置JSON文件数据源
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
