package com.tangyujun.datashadow.datatype;

import java.util.List;
import java.util.Map;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.exception.DataAccessException;

/**
 * JSON数据处理工具类
 * 用于解析和处理JSON格式的数据
 * 支持将JSON字符串或字节数组解析为结构化数据
 * 继承自ShadowData基类,提供JSON特定的数据处理实现
 */
public class JsonData extends ShadowData {

    /**
     * 将字节数组转换为字符串后解析JSON数据
     * 首先将字节数组按照默认字符集转换为字符串
     * 然后调用字符串解析方法进行JSON解析
     * 
     * @param bytes JSON格式的字节数组,不能为null
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     * @throws DataAccessException 当输入字节数组为null或JSON解析失败时抛出此异常
     */
    public static List<Map<String, Object>> getValues(byte[] bytes) {
        return getValues(new String(bytes));
    }

    /**
     * 解析JSON字符串数据
     * 使用FastJson库将JSON字符串解析为List<Map>结构
     * 每个Map代表一行数据,key为列名,value为对应的值
     * 
     * @param json JSON格式的字符串,必须是一个JSON数组,且数组元素为对象
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     * @throws DataAccessException 当JSON字符串格式错误或解析失败时抛出此异常
     */
    public static List<Map<String, Object>> getValues(String json) {
        try {
            return JSON.parseObject(json,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            throw new DataAccessException("JSON解析错误: " + e.getMessage(), e);
        }
    }

    /**
     * 从JSON字符串中获取数据列名
     * 首先将JSON字符串解析为List<Map>结构
     * 然后调用父类的getColumns方法获取所有可能的列名
     * 
     * @param json JSON格式的字符串,必须是一个JSON数组,且数组元素为对象
     * @return 数据列名列表,包含所有可能出现的列名
     * @throws DataAccessException 当JSON字符串格式错误或解析失败时抛出此异常
     * @see ShadowData#getColumns(List)
     */
    public static List<String> getColumns(String json) {
        List<Map<String, Object>> object = null;
        try {
            object = JSON.parseObject(json,
                    new TypeReference<List<Map<String, Object>>>() {
                    });
        } catch (Exception e) {
            throw new DataAccessException("JSON解析错误: " + e.getMessage(), e);
        }
        return getColumns(object);
    }

    /**
     * 判断输入字符串是否为JSON格式
     * 检查字符串是否以"["开头和"}"结尾,或者以"{"开头和"}"结尾
     * 如果符合JSON格式,则尝试解析字符串,如果解析成功则返回true,否则返回false
     * 
     * @param content 输入的字符串
     * @return 如果输入字符串为JSON格式则返回true,否则返回false
     */
    public static boolean isJson(String content) {
        content = content.trim();
        if ((content.startsWith("[") && content.endsWith("]")) ||
                (content.startsWith("{") && content.endsWith("}"))) {
            try {
                JSON.parse(content);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
