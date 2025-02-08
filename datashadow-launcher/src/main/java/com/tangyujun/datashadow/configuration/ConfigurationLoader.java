package com.tangyujun.datashadow.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.config.ConfigFactory;
import com.tangyujun.datashadow.config.Configuration;
import com.tangyujun.datashadow.config.ConfigurationChangeListener;

/**
 * 配置总线类
 * 负责程序配置的加载、保存和管理
 * 采用单例模式实现
 * 
 * 主要功能:
 * 1. 管理程序的全局配置信息
 * 2. 提供配置的读写接口
 * 3. 持久化配置到文件系统
 * 4. 在启动时自动加载配置
 * 
 * 配置项包括:
 * - 用户主目录
 * - 程序数据目录
 * - 插件目录
 * - 程序许可证
 */
public class ConfigurationLoader implements ConfigurationChangeListener {

    /**
     * 日志记录器
     * 用于记录配置加载、保存等操作的日志信息
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationLoader.class);

    /**
     * 单例实例
     * 保证全局只有一个配置总线实例
     */
    private static final ConfigurationLoader instance;

    static {
        instance = new ConfigurationLoader();
        ConfigFactory.getInstance().addChangeListener(instance);
    }

    /**
     * 私有构造方法
     * 初始化时添加配置变更监听器
     */
    private ConfigurationLoader() {
        // 移除在构造函数中注册监听器
    }

    /**
     * 加载配置
     * 从配置文件加载配置信息
     * 
     * @throws IOException 当配置文件读取失败时抛出
     * 
     */
    public static void load() throws IOException {
        instance.loafFromFile();
    }

    /**
     * 保存配置文件
     * 将当前配置保存到用户目录下的.datashadow/ds.conf文件中
     * 配置内容会被Base64编码后保存
     */
    public static void save() {
        instance.saveToFile();
    }

    /**
     * 保存配置文件的具体实现
     * 将配置对象转换为JSON,Base64编码后写入文件
     * 
     * 保存步骤:
     * 1. 创建配置对象并设置当前值
     * 2. 转换为JSON字符串
     * 3. Base64编码
     * 4. 写入文件
     */
    private void saveToFile() {
        Configuration configuration = ConfigFactory.getInstance().getConfiguration();
        String jsonContent = JSON.toJSONString(configuration);
        String encodedContent = Base64.getEncoder().encodeToString(jsonContent.getBytes());
        try {
            Files.writeString(new File(ConfigFactory.getInstance().getStorageFile()).toPath(), encodedContent);
            log.info("Configuration saved successfully");
            log.info("Plugin directory: {}", configuration.getPluginDir());
            log.info("AI model: {}", configuration.getAiModel());
            log.info("License: {}", configuration.getLicense());
        } catch (IOException ex) {
            log.error("Failed to save configuration file", ex);
        }
    }

    /**
     * 从文件加载配置的具体实现
     * 
     * 加载步骤:
     * 1. 确定用户目录和程序目录
     * 2. 读取配置文件
     * 3. Base64解码和JSON解析
     * 4. 如果出现异常则使用默认配置
     * 
     * 默认配置:
     * - 插件目录: {user.home}/.datashadow/plugins
     * - 许可证: 空字符串
     */
    private void loafFromFile() {
        String userHome = System.getProperty("user.home");
        ConfigFactory.getInstance().setHomeDir(userHome);
        String storageFile = ConfigFactory.getInstance().getStorageFile();
        log.info("User Home: {}", userHome);
        log.info("Data Shadow Dir: {}", storageFile);
        // 读取配置文件
        File configFile = new File(storageFile);
        if (configFile.exists()) {
            try {
                // 读取文件内容
                String encodedContent = Files.readString(configFile.toPath());
                // Base64解码
                byte[] decodedBytes = Base64.getDecoder().decode(encodedContent);
                String jsonContent = new String(decodedBytes);
                // 解析JSON到配置对象
                Configuration configuration = JSON.parseObject(jsonContent, Configuration.class);
                log.info("Configuration file loaded successfully");
                ConfigFactory.getInstance().updateConfiguration(configuration, false);
                log.info("Loaded plugin directory: {}", configuration.getPluginDir());
            } catch (IOException e) {
                log.error("Failed to read configuration file", e);
                ConfigFactory.getInstance().updateConfiguration(null, false);
                log.info("Using default plugin directory: {}",
                        ConfigFactory.getInstance().getPluginDir());
            }
        } else {
            log.info("Configuration file not found, using default configuration");
            ConfigFactory.getInstance().updateConfiguration(null, false);
            log.info("Using default plugin directory: {}",
                    ConfigFactory.getInstance().getPluginDir());
        }
    }

    /**
     * 配置变更监听器
     * 当配置发生变化时,保存配置到文件
     * 
     * @param configuration 变更后的配置对象
     */
    @Override
    public void onConfigurationChange(Configuration configuration) {
        saveToFile();
    }

}
