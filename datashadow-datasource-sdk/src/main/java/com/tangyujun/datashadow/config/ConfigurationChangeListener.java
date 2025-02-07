package com.tangyujun.datashadow.config;

/**
 * 配置文件变化监听器
 * 
 * 用于监听配置文件的变化
 */
@FunctionalInterface

public interface ConfigurationChangeListener {

    /**
     * 配置文件发生变化时触发
     * 
     * @param configuration 配置文件
     */
    void onConfigurationChange(Configuration configuration);
}
