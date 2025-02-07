package com.tangyujun.datashadow.config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置工厂类
 * 用于管理程序配置信息
 * 采用单例模式确保全局唯一实例
 */
public class ConfigFactory {

    /**
     * 单例实例
     */
    private static final ConfigFactory INSTANCE = new ConfigFactory();

    /**
     * 获取配置工厂单例实例
     * 
     * @return 配置工厂实例
     */
    public static ConfigFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 用户主目录路径
     */
    private String homeDir;

    /**
     * 设置用户主目录路径
     * 
     * @param homeDir 用户主目录路径
     */

    public void setHomeDir(String homeDir) {
        this.homeDir = homeDir;
    }

    /**
     * 配置信息对象
     * 存储所有配置项
     */
    private final Configuration configuration = new Configuration();

    /**
     * 获取配置信息对象
     * 
     * @return 配置信息对象
     */
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 配置变化监听器列表
     * 存储所有配置变化监听器
     */
    private final List<ConfigurationChangeListener> changeListeners = new ArrayList<>();

    /**
     * 私有构造方法
     * 
     * 防止外部实例化
     */
    private ConfigFactory() {
    }

    /**
     * 获取配置文件存储路径
     * 默认为{user.home}/.datashadow/.dsconf
     * 
     * @return 配置文件的完整路径
     */
    public String getStorageFile() {
        return homeDir + File.separator + CONFIG.STORAGE_DIR
                + File.separator + CONFIG.STORAGE_FILE;
    }

    /**
     * 获取插件目录路径
     * 如果配置的插件目录无效则返回默认路径
     * 默认路径为{user.home}/.datashadow/plugins
     * 
     * @return 插件目录的完整路径
     */
    public String getPluginDir() {
        if (configuration.getPluginDir() == null
                || configuration.getPluginDir().trim().isEmpty()
                || !new File(configuration.getPluginDir()).exists()) {
            return homeDir + File.separator + CONFIG.STORAGE_DIR
                    + File.separator + "plugins";
        }
        return configuration.getPluginDir();
    }

    /**
     * 更新配置信息
     * 将新的配置对象的值更新到当前配置中
     * 如果传入null则清空所有配置
     * 
     * @param configuration 新的配置对象,可以为null
     * @param notify        是否通知配置变更监听器
     */
    public void updateConfiguration(Configuration configuration, boolean notify) {
        if (configuration != null) {
            this.configuration.setPluginDir(configuration.getPluginDir());
            this.configuration.setAiModel(configuration.getAiModel());
            this.configuration.setAiApiKey(configuration.getAiApiKey());
            this.configuration.setLicense(configuration.getLicense());
        } else {
            this.configuration.setPluginDir(null);
            this.configuration.setAiModel(null);
            this.configuration.setAiApiKey(null);
            this.configuration.setLicense(null);
        }
        if (notify) {
            notifyChangeListeners();
        }
    }

    /**
     * 添加配置变更监听器
     * 当配置发生变化时会通知所有已注册的监听器
     * 
     * @param listener 配置变更监听器实例
     */
    public void addChangeListener(ConfigurationChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * 移除配置变更监听器
     * 移除后该监听器将不再接收配置变更通知
     * 
     * @param listener 要移除的配置变更监听器实例
     */
    public void removeChangeListener(ConfigurationChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * 通知所有配置变更监听器
     * 遍历所有已注册的监听器并调用其onConfigurationChange方法
     * 在配置发生变更时由updateConfiguration方法调用
     */
    private void notifyChangeListeners() {
        for (ConfigurationChangeListener listener : changeListeners) {
            listener.onConfigurationChange(configuration);
        }
    }
}
