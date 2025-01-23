package com.tangyujun.datashadow.datatype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvData {

    /**
     * 判断输入字符串是否为CSV格式
     * 通过检查每行的逗号数量是否一致来判断
     * 
     * 判断规则:
     * 1. 将输入字符串按换行符分割成行
     * 2. 获取第一行的逗号数量作为基准
     * 3. 检查其他非空行的逗号数量是否与基准相同
     * 4. 如果所有非空行的逗号数量都相同,则判定为CSV格式
     * 
     * 注意:
     * 1. 空行会被跳过不进行检查
     * 2. 如果输入字符串为空或只有一行,返回false
     * 3. 不检查字段内容的合法性,只检查格式
     *
     * @param content 需要判断格式的输入字符串
     * @return 如果是CSV格式返回true,否则返回false
     */
    public static boolean isCsv(String content) {
        // 检查是否为CSV格式
        // CSV格式特征：每行都有相同数量的逗号，且不包含XML/JSON特征
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            int commaCount = lines[0].split(",").length - 1;
            boolean isCSV = true;
            for (String line : lines) {
                if (line.trim().isEmpty())
                    continue;
                if (line.split(",").length - 1 != commaCount) {
                    isCSV = false;
                    break;
                }
            }
            return isCSV;
        }
        return false;
    }

    /**
     * 解析CSV格式字符串并返回数据列表
     * 将CSV字符串解析为结构化数据,每行作为一条记录
     * 
     * 解析规则:
     * 1. 第一行作为列名
     * 2. 从第二行开始为数据内容
     * 3. 每行按逗号分隔成字段
     * 4. 空行会被跳过
     * 5. 使用LinkedHashMap保持字段顺序
     * 
     * 注意:
     * 1. 所有字段值会进行trim处理
     * 2. 如果某行的字段数少于列名数,多余的列将被忽略
     * 3. 如果输入为空或只有列名行,返回空列表
     * 
     * @param content CSV格式的字符串内容
     * @return 解析后的数据列表,每个元素为一个Map表示一行数据
     */
    public static List<Map<String, Object>> getValues(String content) {
        List<Map<String, Object>> result = new ArrayList<>();
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            String[] headers = lines[0].split(",");
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i].trim();
            }

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    String[] values = line.split(",");
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int j = 0; j < headers.length && j < values.length; j++) {
                        row.put(headers[j], values[j].trim());
                    }
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * 获取CSV数据的列名列表
     * 从CSV字符串中提取第一行作为列名
     * 
     * 解析规则:
     * 1. 按换行符分割获取所有行
     * 2. 取第一行作为列名行
     * 3. 按逗号分割列名行获取各列名
     * 
     * 注意:
     * 1. 如果输入为空或没有任何行,返回空列表
     * 2. 列名不会进行trim处理,保持原始格式
     * 
     * @param content CSV格式的字符串内容
     * @return 列名字符串列表,如果输入为空则返回空列表
     */
    public static List<String> getColumns(String content) {
        String[] lines = content.split("\n");
        if (lines.length > 0) {
            return Arrays.asList(lines[0].split(","));
        }
        return new ArrayList<>();
    }
}
