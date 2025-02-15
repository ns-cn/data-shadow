package com.tangyujun.datashadow.datasource.http;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CURL命令解析工具类
 * 用于解析CURL命令字符串，提取URL、请求方法、请求头和请求体等信息
 */
public class CurlParser {

    /**
     * 解析结果类
     */
    public static class CurlParseResult {
        private String url;
        private String method = "GET";
        private Map<String, String> headers = new HashMap<>();
        private String body;

        // Getters
        public String getUrl() {
            return url;
        }

        public String getMethod() {
            return method;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public String getBody() {
            return body;
        }

        // Setters
        public void setUrl(String url) {
            this.url = url;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public void addHeader(String key, String value) {
            headers.put(key, value);
        }

        public void setBody(String body) {
            this.body = body;
        }
    }

    /**
     * 分割CURL命令，处理引号内的空格和续行符
     */
    private static String[] splitCommand(String command) {
        // 首先处理续行符
        command = command.replaceAll("\\\\\\s*\\n\\s*", " ");

        Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
        Matcher matcher = pattern.matcher(command);

        java.util.List<String> parts = new java.util.ArrayList<>();
        while (matcher.find()) {
            String part = matcher.group();
            // 保留引号内的内容
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1));
            } else if (matcher.group(2) != null) {
                parts.add(matcher.group(2));
            } else {
                parts.add(part);
            }
        }

        return parts.toArray(new String[0]);
    }

    /**
     * 处理请求体中的转义字符
     */
    private static String unescapeData(String data) {
        // 首先移除所有换行符和多余的空格
        data = data.replaceAll("\\s*\\\\\\s*\\n\\s*", "")
                .trim();

        // 如果数据被引号包围，移除外层引号
        if ((data.startsWith("\"") && data.endsWith("\"")) ||
                (data.startsWith("'") && data.endsWith("'"))) {
            data = data.substring(1, data.length() - 1);
        }

        // 处理转义字符，使用临时标记避免冲突
        return data.replace("\\\\", "__ESCAPED_BACKSLASH__") // 临时替换双反斜杠
                .replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("__ESCAPED_BACKSLASH__", "\\"); // 还原双反斜杠
    }

    /**
     * 解析CURL命令字符串
     */
    public static CurlParseResult parse(String curlCommand) {
        if (curlCommand == null || !curlCommand.trim().toLowerCase().startsWith("curl")) {
            throw new IllegalArgumentException("无效的CURL命令");
        }

        CurlParseResult result = new CurlParseResult();
        String[] parts = splitCommand(curlCommand);

        // 查找URL
        boolean foundUrl = false;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.startsWith("http://") || part.startsWith("https://")) {
                result.setUrl(part);
                foundUrl = true;
                break;
            }
        }

        // 解析其他参数
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();

            if (part.equals("-X") && i + 1 < parts.length) {
                result.setMethod(parts[++i]);
            } else if ((part.equals("-H") || part.equals("--header")) && i + 1 < parts.length) {
                String header = parts[++i];
                int colonIndex = header.indexOf(':');
                if (colonIndex > 0) {
                    String key = header.substring(0, colonIndex).trim();
                    String value = header.substring(colonIndex + 1).trim();
                    result.addHeader(key, value);
                }
            } else if ((part.equals("-d") || part.equals("--data") ||
                    part.equals("--data-raw") || part.equals("--data-binary")) &&
                    i + 1 < parts.length) {
                String data = parts[++i];
                result.setBody(unescapeData(data));
                // 如果没有明确指定方法，设置为POST
                if (result.getMethod().equals("GET")) {
                    result.setMethod("POST");
                }
            }
        }

        if (!foundUrl) {
            throw new IllegalArgumentException("CURL命令中未找到URL");
        }

        return result;
    }
}