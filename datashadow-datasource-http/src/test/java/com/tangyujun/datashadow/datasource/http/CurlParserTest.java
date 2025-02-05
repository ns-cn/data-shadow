package com.tangyujun.datashadow.datasource.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CURL命令解析器测试类
 * 用于测试CurlParser类的各种解析功能
 * 包含以下测试场景:
 * 1. 基本GET请求解析
 * 2. 带请求头的POST请求解析
 * 3. 带引号的URL解析
 * 4. 转义字符处理
 * 5. 无效CURL命令处理
 * 6. 复杂CURL命令解析
 * 7. Windows格式CURL命令解析
 * 8. 复杂请求头和data-raw参数解析
 */
class CurlParserTest {

    /**
     * 测试基本GET请求的解析
     * 验证简单GET请求URL的正确解析,以及默认值的正确设置
     */
    @Test
    @DisplayName("测试基本GET请求解析")
    void testBasicGetRequest() {
        String curl = "curl https://api.example.com/data";
        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://api.example.com/data", result.getUrl());
        assertEquals("GET", result.getMethod());
        assertTrue(result.getHeaders().isEmpty());
        assertNull(result.getBody());
    }

    /**
     * 测试带请求头的POST请求解析
     * 验证:
     * 1. POST方法的正确识别
     * 2. 请求头的正确解析
     * 3. 请求体的正确解析
     */
    @Test
    @DisplayName("测试带请求头的POST请求解析")
    void testPostRequestWithHeaders() {
        String curl = "curl -X POST 'https://api.example.com/data' " +
                "-H 'Content-Type: application/json' " +
                "-H 'Authorization: Bearer token123' " +
                "-d '{\"name\":\"test\"}'";

        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://api.example.com/data", result.getUrl());
        assertEquals("POST", result.getMethod());
        assertEquals(2, result.getHeaders().size());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));
        assertEquals("Bearer token123", result.getHeaders().get("Authorization"));
        assertEquals("{\"name\":\"test\"}", result.getBody());
    }

    /**
     * 测试带引号的URL解析
     * 验证包含空格和特殊字符的URL能够被正确解析
     */
    @Test
    @DisplayName("测试带引号的URL解析")
    void testQuotedUrl() {
        String curl = "curl \"https://api.example.com/data?param=value with space\"";
        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://api.example.com/data?param=value with space", result.getUrl());
    }

    /**
     * 测试转义字符的处理
     * 验证JSON字符串中的转义字符能够被正确处理
     */
    @Test
    @DisplayName("测试转义字符处理")
    void testEscapeCharacters() {
        String curl = "curl -X POST 'https://api.example.com/data' " +
                "-d '{\\\"key\\\":\\\"value with \\\"quotes\\\"\\\"}'";

        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("{\"key\":\"value with \"quotes\"\"}", result.getBody());
    }

    /**
     * 测试无效CURL命令的处理
     * 验证解析器对各种无效输入的异常处理
     * 
     * @param invalidCurl 无效的CURL命令
     */
    @ParameterizedTest
    @DisplayName("测试无效的CURL命令")
    @ValueSource(strings = {
            "",
            "invalid command",
            "curlhttps://api.example.com",
            "curl"
    })
    void testInvalidCurlCommand(String invalidCurl) {
        assertThrows(IllegalArgumentException.class, () -> CurlParser.parse(invalidCurl));
    }

    /**
     * 测试缺少URL的CURL命令处理
     * 验证在缺少必要URL参数时是否正确抛出异常
     */
    @Test
    @DisplayName("测试缺少URL的CURL命令")
    void testMissingUrl() {
        String curl = "curl -X POST -H 'Content-Type: application/json'";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CurlParser.parse(curl));

        assertEquals("CURL命令中未找到URL", exception.getMessage());
    }

    /**
     * 测试复杂CURL命令的解析
     * 验证解析器处理包含多个参数的复杂CURL命令的能力:
     * 1. PUT方法
     * 2. 多个请求头
     * 3. 复杂JSON请求体
     */
    @Test
    @DisplayName("测试复杂的CURL命令")
    void testComplexCurlCommand() {
        String curl = "curl 'https://api.example.com/data'" +
                " -X PUT" +
                " -H 'Content-Type: application/json'" +
                " -H 'Accept: application/json'" +
                " -H 'Authorization: Bearer abc123'" +
                " --data '{" +
                "\"name\":\"test\"," +
                "\"description\":\"test description\"," +
                "\"tags\":[\"tag1\",\"tag2\"]" +
                "}'";

        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://api.example.com/data", result.getUrl());
        assertEquals("PUT", result.getMethod());
        assertEquals(3, result.getHeaders().size());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));
        assertEquals("application/json", result.getHeaders().get("Accept"));
        assertEquals("Bearer abc123", result.getHeaders().get("Authorization"));
        assertTrue(result.getBody().contains("\"tags\":[\"tag1\",\"tag2\"]"));
    }

    /**
     * 测试Windows格式CURL命令的解析
     * 验证解析器处理Windows风格的多行CURL命令的能力:
     * 1. 反斜杠换行
     * 2. 双引号字符串
     * 3. 转义序列
     */
    @Test
    @DisplayName("测试Windows格式的CURL命令")
    void testWindowsStyleCurl() {
        String curl = "curl -X POST \"https://api.example.com/data\" \\\n" +
                "  -H \"Content-Type: application/json\" \\\n" +
                "  -H \"Authorization: Bearer token123\" \\\n" +
                "  -d \"{\\\"key\\\":\\\"value\\\"}\"";

        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://api.example.com/data", result.getUrl());
        assertEquals("POST", result.getMethod());
        assertEquals(2, result.getHeaders().size());
        assertEquals("application/json", result.getHeaders().get("Content-Type"));
        assertEquals("Bearer token123", result.getHeaders().get("Authorization"));
        assertEquals("{\"key\":\"value\"}", result.getBody());
    }

    /**
     * 测试复杂请求头和data-raw参数的解析
     * 验证解析器处理:
     * 1. 复杂的Accept-Language头
     * 2. 包含特殊字符的User-Agent
     * 3. data-raw参数
     * 4. 自动设置POST方法
     */
    @Test
    @DisplayName("测试复杂请求头和data-raw参数")
    void testComplexHeadersAndDataRaw() {
        String curl = "curl 'https://gitee.com/graphql' \\\n" +
                "  -H 'Accept: */*' \\\n" +
                "  -H 'Accept-Language: zh-CN,zh;q=0.9,en;q=0.8' \\\n" +
                "  -H 'Cookie: user_locale=zh-CN' \\\n" +
                "  -H 'sec-ch-ua: \"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\"' \\\n" +
                "  --data-raw '{\"query\":\"query floatingPendant\"}'\n";

        CurlParser.CurlParseResult result = CurlParser.parse(curl);

        assertEquals("https://gitee.com/graphql", result.getUrl());
        assertEquals("POST", result.getMethod()); // --data-raw 默认使用POST方法
        assertTrue(result.getHeaders().containsKey("Accept"));
        assertEquals("*/*", result.getHeaders().get("Accept"));
        assertTrue(result.getHeaders().containsKey("sec-ch-ua"));
        assertEquals("\"Not A(Brand\";v=\"8\", \"Chromium\";v=\"132\"",
                result.getHeaders().get("sec-ch-ua"));
        assertTrue(result.getBody().contains("floatingPendant"));
    }
}