package com.tangyujun.datashadow.model.datasource.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.model.datasource.DataSourceFile;
import com.tangyujun.datashadow.model.exception.DataAccessException;
import com.tangyujun.datashadow.model.exception.DataSourceValidException;

import lombok.EqualsAndHashCode;

/**
 * JSON数据源
 */
@EqualsAndHashCode(callSuper = true)
public class DataSourceJson extends DataSourceFile {

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

}
