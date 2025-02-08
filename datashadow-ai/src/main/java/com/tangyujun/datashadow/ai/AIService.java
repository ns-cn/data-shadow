package com.tangyujun.datashadow.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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
}