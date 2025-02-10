package com.tangyujun.datashadow.core;

import java.util.HashMap;
import java.util.Map;

import com.tangyujun.datashadow.exporter.ResultExporter;

/**
 * 环境类，用于管理结果导出器的注册和获取。
 */
public class Environment {

    /**
     * 导出器注册表，用于存储已注册的结果导出器。
     * 键为分组名，值为一个Map，键为导出器名，值为对应的ResultExporter实例。
     */
    private static final Map<String, Map<String, ResultExporter>> exporters = new HashMap<>();

    /**
     * 私有构造函数，防止外部实例化。
     */
    private Environment() {
    }

    /**
     * 注册结果导出器。
     * 
     * @param group    导出器分组名。
     * @param name     导出器名。
     * @param registry 要注册的ResultExporter实例。
     */
    public static void registerExporter(String group, String name, ResultExporter registry) {
        exporters.computeIfAbsent(group, k -> new HashMap<>()).put(name, registry);
    }

    /**
     * 获取所有已注册的结果导出器。
     * 
     * @return 导出器注册表。
     */
    public static Map<String, Map<String, ResultExporter>> getExporters() {
        return exporters;
    }

    /**
     * 
     * 根据分组名和导出器名获取对应的结果导出器
     * 
     * @param grou 器分组名。
     * @param name 导出器名。
     * @return 对应的ResultExporter实例，如果不存在返回null。
     */
    public static ResultExporter getExporter(String group, String name) {
        Map<String, ResultExporter> groupExporters = exporters.get(group);
        return groupExporters != null ? groupExporters.get(name) : null;
    }
}
