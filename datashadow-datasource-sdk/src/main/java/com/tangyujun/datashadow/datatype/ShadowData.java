package com.tangyujun.datashadow.datatype;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据处理的抽象基类
 * 提供了一些通用的数据处理方法
 */
public abstract class ShadowData {

    /**
     * 从解析后的数据中获取列名
     * 通过遍历所有数据行,获取所有可能出现的键作为列名
     * 使用Set去重,确保列名不重复
     * 
     * @param data 解析后的数据列表,每个元素为一个Map表示一行数据
     * @return 数据列名列表,如果数据为空则返回空列表
     */
    public static List<String> getColumns(List<Map<String, Object>> data) {
        if (data != null && !data.isEmpty()) {
            // 使用HashSet存储所有列名,自动去重
            Set<String> allKeys = new HashSet<>();
            // 遍历每一行数据,收集所有可能的列名
            for (Map<String, Object> row : data) {
                allKeys.addAll(row.keySet());
            }
            // 将Set转换为List返回
            return new ArrayList<>(allKeys);
        }
        // 如果数据为空,返回空列表
        return new ArrayList<>();
    }
}