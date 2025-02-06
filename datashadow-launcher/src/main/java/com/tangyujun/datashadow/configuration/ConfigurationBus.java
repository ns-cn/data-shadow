package com.tangyujun.datashadow.configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;

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
public class ConfigurationBus {

    /**
     * 日志记录器
     * 用于记录配置加载、保存等操作的日志信息
     */
    private static final Logger log = LoggerFactory.getLogger(ConfigurationBus.class);

    /**
     * 单例实例
     * 保证全局只有一个配置总线实例
     */
    private static final ConfigurationBus instance = new ConfigurationBus();

    /**
     * 加载配置
     * 从配置文件加载配置信息
     * 
     * @throws IOException 当配置文件读取失败时抛出
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
        Configuration configuration = new Configuration();
        configuration.setPluginDir(pluginDir);
        configuration.setLicense(license);
        String jsonContent = JSON.toJSONString(configuration);
        String encodedContent = Base64.getEncoder().encodeToString(jsonContent.getBytes());

        try {
            Files.writeString(new File(dataShadowDir + File.separator + "ds.conf").toPath(), encodedContent);
            log.info("Configuration saved successfully");
            log.info("Plugin directory: {}", pluginDir);
            log.info("License: {}", license);
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
        userDir = System.getProperty("user.home");
        dataShadowDir = userDir + File.separator + STORAGE_DIR;
        log.info("User Home: {}", userDir);
        log.info("Data Shadow Dir: {}", dataShadowDir);
        // 读取配置文件
        File configFile = new File(dataShadowDir + File.separator + "ds.conf");

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
                instance.pluginDir = configuration.getPluginDir();
                instance.license = configuration.getLicense();
                log.info("Loaded plugin directory: {}", instance.pluginDir);
                log.info("Loaded license: {}", instance.license);

            } catch (IOException e) {
                log.error("Failed to read configuration file", e);
                instance.pluginDir = dataShadowDir + File.separator + "plugins";
                instance.license = "";
                log.info("Using default plugin directory: {}", instance.pluginDir);
                log.info("Using empty license");
            }
        } else {
            log.info("Configuration file not found, using default configuration");
            instance.pluginDir = dataShadowDir + File.separator + "plugins";
            instance.license = "";
            log.info("Using default plugin directory: {}", instance.pluginDir);
            log.info("Using empty license");
        }
    }

    /**
     * 程序数据存储目录名称
     * 位于用户目录下的.datashadow目录
     * 用于存储程序的所有配置文件和数据文件
     */
    public static final String STORAGE_DIR = ".datashadow";

    /**
     * 配置文件名称
     * 存储在程序数据目录下的.dsconf文件
     * 包含程序的所有配置信息
     */
    public static final String STORAGE_FILE = ".dsconf";

    /**
     * 用户主目录路径
     * 通过System.getProperty("user.home")获取
     * 作为程序数据目录的父目录
     */
    private String userDir;

    /**
     * 程序数据目录的完整路径
     * 由用户目录和STORAGE_DIR组成
     * 存储程序的所有数据文件
     */
    private String dataShadowDir;

    /**
     * 插件目录的完整路径
     * 默认为程序数据目录下的plugins目录
     * 用于存放自定义数据源插件的jar文件
     */
    private String pluginDir;

    /**
     * 程序许可证
     * 用于验证程序的使用权限
     * 包含用户的授权信息
     */
    private String license;

    /**
     * 获取配置总线的单例实例
     * 确保全局使用同一个配置实例
     * 
     * @return ConfigurationBus的唯一实例
     */
    public static ConfigurationBus getInstance() {
        return instance;
    }

    /**
     * 获取用户主目录路径
     * 
     * @return 用户主目录的完整路径,如 /home/username
     */
    public String getUserDir() {
        return userDir;
    }

    /**
     * 设置用户主目录路径
     * 
     * @param userDir 用户主目录的完整路径,如 /home/username
     */
    public void setUserDir(String userDir) {
        this.userDir = userDir;
    }

    /**
     * 获取程序数据目录路径
     * 
     * @return 程序数据目录的完整路径,如 /home/username/.datashadow
     */
    public String getDataShadowDir() {
        return dataShadowDir;
    }

    /**
     * 设置程序数据目录路径
     * 
     * @param dataShadowDir 程序数据目录的完整路径,如 /home/username/.datashadow
     */
    public void setDataShadowDir(String dataShadowDir) {
        this.dataShadowDir = dataShadowDir;
    }

    /**
     * 获取插件目录路径
     * 如果插件目录未设置或不存在,返回默认路径
     * 
     * @return 插件目录的完整路径,如 /home/username/.datashadow/plugins
     */
    public String getPluginDir() {
        // 如果插件目录为空或不存在,返回默认插件目录
        if (pluginDir == null || pluginDir.trim().isEmpty() || !new File(pluginDir).exists()) {
            return System.getProperty("user.home")
                    + File.separator + ".datashadow"
                    + File.separator + "plugins";
        }
        return pluginDir;
    }

    /**
     * 设置插件目录路径
     * 
     * @param pluginDir 插件目录的完整路径,如 /home/username/.datashadow/plugins
     */
    public void setPluginDir(String pluginDir) {
        this.pluginDir = pluginDir;
    }

    /**
     * 获取程序许可证
     * 
     * @return 程序许可证字符串,未授权时返回空字符串
     */
    public String getLicense() {
        return license;
    }

    /**
     * 设置程序许可证
     * 
     * @param license 程序许可证字符串,用于验证程序使用权限
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * 配置数据结构类
     * 用于序列化和反序列化配置数据
     * 
     * 包含的配置项:
     * - pluginDir: 插件目录路径
     * - license: 程序许可证
     */
    private static class Configuration {

        /**
         * 插件目录路径
         * 默认为用户目录下的 .datashadow/plugins 目录
         * 用于存放自定义数据源插件
         * 可通过配置文件修改
         */
        private String pluginDir;

        /**
         * 程序许可证
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
    }
}
