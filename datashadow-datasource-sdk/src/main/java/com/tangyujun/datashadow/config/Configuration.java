package com.tangyujun.datashadow.config;

import com.tangyujun.datashadow.ai.Models;

/**
 * 配置数据结构类
 * 用于序列化和反序列化配置数据
 * 
 * 包含的配置项:
 * - pluginDir: 插件目录路径
 * - license: 程序许可证
 */
public class Configuration {

    /**
     * 插件目录路径
     * 默认为用户目录下的 .datashadow/plugins 目录
     * 用于存放自定义数据源插件
     * 可通过配置文件修改
     */
    private String pluginDir;

    /**
     * AI模型
     * 用于指定AI模型
     * 可通过配置文件修改
     */
    private Models aiModel = Models.Qwen25_7B_Instruct; // 设置默认模型

    /**
     * AI API Key
     * 用于指定AI API Key
     * 可通过配置文件修改
     */
    private String aiApiKey;

    /**
     * 程序许可证（暂不考虑实现）
     * 用于存储用户的授权信息
     * 包含授权码等验证信息
     */
    private String license;

    /**
     * 获取插件目录路径
     * 
     * @return 插件目录的完整路径
     */
    public String getPluginDir() {
        return pluginDir;
    }

    /**
     * 设置插件目录路径
     * 
     * @param pluginDir 插件目录的完整路径
     */
    public void setPluginDir(String pluginDir) {
        this.pluginDir = pluginDir;
    }

    /**
     * 获取许可证
     * 
     * @return 许可证字符串,未授权时返回null
     */
    public String getLicense() {
        return license;
    }

    /**
     * 设置许可证
     * 
     * @param license 许可证字符串,包含授权信息
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * 获取AI模型
     * 
     * @return AI模型枚举值
     */
    public Models getAiModel() {
        return aiModel;
    }

    /**
     * 设置AI模型
     * 
     * @param aiModel AI模型枚举值
     */
    public void setAiModel(Models aiModel) {
        this.aiModel = aiModel;
    }

    /**
     * 获取AI API Key
     * 
     * @return AI API Key
     */
    public String getAiApiKey() {
        return aiApiKey;
    }

    /**
     * 设置AI API Key
     * 
     * @param aiApiKey AI API Key
     */
    public void setAiApiKey(String aiApiKey) {
        this.aiApiKey = aiApiKey;
    }
}