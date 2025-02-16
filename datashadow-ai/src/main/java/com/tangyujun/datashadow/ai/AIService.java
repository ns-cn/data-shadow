package com.tangyujun.datashadow.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.Map;
import java.util.HashMap;

/**
 * AI服务类
 * 处理与AI模型的交互逻辑
 */
public class AIService {
    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 验证API Key是否有效
     *
     * @param model  AI模型
     * @param apiKey API密钥
     * @return 验证结果，成功返回true，失败返回false
     * @throws IOException 网络请求异常
     */
    public static boolean validateApiKey(Models model, String apiKey) throws IOException {
        String jsonBody = String.format("""
                {
                    "model": "%s",
                    "messages": [
                        {"role": "user", "content": "Hello"}
                    ]
                }""", model.getModelName());

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", String.format("Bearer %s", apiKey.trim()))
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }

    /**
     * 发送消息到AI服务器
     *
     * @param model          AI模型
     * @param apiKey         API密钥
     * @param messageHistory 消息历史
     * @param onSuccess      成功回调
     * @param onError        错误回调
     */
    public static void sendMessage(Models model,
            String apiKey,
            List<JSONObject> messageHistory,
            Consumer<String> onSuccess,
            Consumer<String> onError) {
        new Thread(() -> {
            try {
                String jsonBody = String.format("""
                        {
                            "model": "%s",
                            "messages": %s,
                            "temperature": 0.7,
                            "max_tokens": 2048,
                            "stream": false
                        }""",
                        model.getModelName(),
                        JSON.toJSONString(messageHistory));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .header("Authorization", String.format("Bearer %s", apiKey.trim()))
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    String responseBody = body != null ? body.string() : "";

                    if (response.isSuccessful() && !responseBody.isEmpty()) {
                        try {
                            JSONObject json = JSON.parseObject(responseBody);
                            String content = json.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            onSuccess.accept(content);
                        } catch (Exception e) {
                            log.error("Failed to parse response", e);
                            onError.accept("解析响应失败: " + e.getMessage());
                        }
                    } else {
                        onError.accept("请求失败 (HTTP " + response.code() + "): " + responseBody);
                    }
                }
            } catch (IOException e) {
                log.error("Failed to send message", e);
                onError.accept("发送失败: " + e.getMessage());
            }
        }).start();
    }

    /**
     * 创建初始系统消息
     *
     * @return 系统消息对象
     */
    public static JSONObject createSystemMessage() {
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "请用自然语言回答，请一定不要使用markdown格式");
        return systemMessage;
    }

    /**
     * 创建用户消息
     *
     * @param content 消息内容
     * @return 用户消息对象
     */
    public static JSONObject createUserMessage(String content) {
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", content);
        return userMessage;
    }

    /**
     * 创建AI助手消息
     *
     * @param content 消息内容
     * @return AI助手消息对象
     */
    public static JSONObject createAssistantMessage(String content) {
        JSONObject assistantMessage = new JSONObject();
        assistantMessage.put("role", "assistant");
        assistantMessage.put("content", content);
        return assistantMessage;
    }

    /**
     * 使用AI分析并建议字段映射关系
     * 
     * @param model         AI模型
     * @param apiKey        API密钥
     * @param itemInfos     数据项信息列表，每个数据项包含code、name和nick
     * @param sourceColumns 数据源字段列表
     * @param onSuccess     成功回调
     * @param onError       错误回调
     */
    public static void suggestMappings(Models model,
            String apiKey,
            List<Map<String, String>> itemInfos,
            List<String> sourceColumns,
            Consumer<Map<String, String>> onSuccess,
            Consumer<String> onError) {
        new Thread(() -> {
            try {
                // 参数验证
                if (model == null || apiKey == null || apiKey.trim().isEmpty()) {
                    onError.accept("AI配置不完整");
                    return;
                }
                if (itemInfos == null || itemInfos.isEmpty() || sourceColumns == null || sourceColumns.isEmpty()) {
                    onError.accept("映射数据不能为空");
                    return;
                }

                // 打印输入信息
                log.info("AI Mapping Input - Data Items:");
                itemInfos.forEach(item -> log.info("  - code: {}, nick: {}",
                        item.get("code"),
                        item.getOrDefault("nick", "")));

                log.info("AI Mapping Input - Source Columns:");
                sourceColumns.forEach(column -> log.info("  - {}", column));

                // 构建提示信息
                StringBuilder itemsStr = new StringBuilder();
                for (Map<String, String> item : itemInfos) {
                    itemsStr.append(String.format("code: %s, name: %s, nick: %s\n",
                            item.get("code"),
                            item.get("name"),
                            item.getOrDefault("nick", "")));
                }

                // 构建系统提示词
                String systemPrompt = """
                        # Role: 数据字段映射专家

                        ## Profile
                        - Language: 中文
                        - Description: 专门分析数据字段名称，提供最优的字段映射方案

                        ## Skills
                        - 精通数据字段语义分析
                        - 擅长识别字段名称相似度
                        - 熟练掌握中英文字段命名规范
                        - 具备数据建模经验

                        ## Goals
                        - 分析源字段和目标字段的语义关联
                        - 提供最合理的字段映射建议
                        - 确保映射结果的准确性

                        ## Rules
                        1. 只返回完全匹配或高度相似的映射关系
                        2. 映射字段名必须完全匹配数据源字段列表中的某一项
                        3. 不添加不存在的字段映射
                        4. 相似度匹配规则：
                           - 完全匹配优先（如：姓名=姓名）
                           - 同义词匹配（如：姓名=名称=名字）
                           - 英文字段需匹配中文语义（如：name=姓名）
                        5. 返回格式必须是JSON对象
                        6. 如果找不到合适的映射，该字段应该被忽略

                        ## Output Format
                        必须返回JSON格式：{"数据项code": "数据源字段名"}

                        ## Example
                        Input:
                        数据项:
                        code: name, name: 姓名, nick: 名字
                        code: age, name: 年龄, nick: 年龄
                        code: address, name: 地址, nick: 住址
                        code: confirm, name: 确认, nick: 确认

                        数据源字段:
                        名称, 年纪, 联系地址，是否确认

                        Output:
                        {"name": "名称", "age": "年纪", "address": "联系地址", "confirm": "是否确认"}

                        ## 相似度匹配示例
                        1. 完全匹配：
                           - 姓名 = 姓名
                           - 年龄 = 年龄
                        2. 同义词匹配：
                           - 姓名 = 名称 = 名字
                           - 年龄 = 年纪 = 岁数
                           - 地址 = 住址 = 联系地址
                           - 确认 = 是否确认
                        3. 英文匹配：
                           - name = 姓名 = 名称
                           - age = 年龄 = 年纪
                           - address = 地址 = 联系地址
                           - confirm = 确认 = 是否确认
                        """;

                // 构建用户提示词
                String userPrompt = String.format("""
                        请分析以下数据并返回最优的字段映射方案：

                        数据项:
                        %s

                        数据源字段:
                        %s
                        """,
                        itemsStr.toString(),
                        String.join(", ", sourceColumns));

                String jsonBody = String.format("""
                        {
                            "model": "%s",
                            "messages": [
                                {
                                    "role": "system",
                                    "content": "%s"
                                },
                                {
                                    "role": "user",
                                    "content": "%s"
                                }
                            ],
                            "response_format": {"type": "json_object"},
                            "temperature": 0.1,
                            "top_p": 0.8,
                            "max_tokens": 2048
                        }""",
                        model.getModelName(),
                        systemPrompt.replace("\"", "\\\"").replace("\n", "\\n"),
                        userPrompt.replace("\"", "\\\"").replace("\n", "\\n"));

                Request request = new Request.Builder()
                        .url(API_URL)
                        .header("Authorization", String.format("Bearer %s", apiKey.trim()))
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    ResponseBody body = response.body();
                    String responseBody = body != null ? body.string() : "";

                    if (response.isSuccessful() && !responseBody.isEmpty()) {
                        try {
                            JSONObject json = JSON.parseObject(responseBody);
                            String content = json.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // 打印AI返回的原始映射结果
                            log.info("AI Mapping Response - Raw Content:\n{}", content);

                            // 解析返回的JSON映射关系
                            Map<String, String> mappings = JSON.parseObject(content,
                                    new TypeReference<Map<String, String>>() {
                                    });

                            // 验证映射结果
                            if (mappings == null || mappings.isEmpty()) {
                                log.warn("AI returned empty mapping result");
                                onError.accept("AI未能提供有效的映射建议");
                                return;
                            }

                            // 创建新的映射Map来存储有效的映射
                            Map<String, String> validMappings = new HashMap<>();

                            // 验证并只保留有效的映射
                            mappings.forEach((code, field) -> {
                                if (sourceColumns.contains(field)) {
                                    validMappings.put(code, field);
                                    log.info("AI Mapping Result - Valid: {} -> {}", code, field);
                                } else {
                                    log.warn("AI Mapping Result - Invalid: {} -> {}", code, field);
                                }
                            });

                            // 检查是否有有效的映射
                            if (validMappings.isEmpty()) {
                                log.warn("No valid mappings found in AI suggestion");
                                onError.accept("AI未能提供有效的映射建议");
                                return;
                            }

                            // 打印最终有效的映射结果
                            log.info("AI Mapping Final Result - Total valid mappings: {}", validMappings.size());
                            validMappings.forEach((code, field) -> log.info("  - {} -> {}", code, field));

                            onSuccess.accept(validMappings);
                        } catch (Exception e) {
                            log.error("Failed to parse AI mapping response", e);
                            onError.accept("解析AI响应失败: " + e.getMessage());
                        }
                    } else {
                        JSONObject errorJson = JSON.parseObject(responseBody);
                        String errorMessage = errorJson != null ? errorJson.getString("message") : "未知错误";
                        log.error("AI request failed: {}", responseBody);
                        onError.accept("AI请求失败 (HTTP " + response.code() + "): " + errorMessage);
                    }
                }
            } catch (IOException e) {
                log.error("Failed to get AI mapping suggestions", e);
                onError.accept("获取AI映射建议失败: " + e.getMessage());
            }
        }).start();
    }
}