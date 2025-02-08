package com.tangyujun.datashadow.ai;

/**
 * AI模型枚举类
 * 定义了系统支持的AI模型类型
 */
public enum Models {

    /**
     * DeepSeek R1 Distill Llama 8B模型
     * 基于Llama架构的蒸馏模型，适用于通用自然语言处理任务
     */
    DeepSeek_R1_Distill_Llama_8B("deepseek-ai/DeepSeek-R1-Distill-Llama-8B", "DeepSeek-R1-Distill-Llama-8B"),

    /**
     * 通义千问2.5-7B指令模型
     * 用于处理自然语言指令和对话
     */

    Qwen25_7B_Instruct("Qwen/Qwen2.5-7B-Instruct", "Qwen2.5-7B-Instruct"),

    ;

    /** 模型名称 */
    private final String modelName;

    /** 模型显示名称 */
    private final String displayName;

    /**
     * 构造函数
     * 
     * @param modelName   模型名称
     * @param displayName 模型显示名称
     */
    Models(String modelName, String displayName) {
        this.modelName = modelName;
        this.displayName = displayName;
    }

    /**
     * 获取模型名称
     * 
     * @return 模型名称字符串
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * 获取模型显示名称
     * 
     * @return 模型显示名称字符串
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据模型名称获取模型
     * 
     * @param name 模型名称
     * @return 模型
     */
    public static Models getModelByName(String name) {
        for (Models model : Models.values()) {
            if (model.getModelName().equals(name)) {

                return model;
            }
        }
        return null;
    }
}
