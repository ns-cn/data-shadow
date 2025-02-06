package com.tangyujun.datashadow.module;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangyujun.datashadow.module.listener.JarDiscoveryListener;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.jar.JarFile;

/**
 * 模块加载器
 * 负责发现和加载JAR包，并通知监听器处理
 * 
 * 主要功能:
 * 1. 加载官方内置模块 - 通过扫描指定包名下的类
 * 2. 加载自定义外部模块 - 通过扫描指定目录下的JAR文件
 * 3. 支持注册监听器 - 在发现新模块时通知监听器处理
 * 
 * 使用方式:
 * 1. 创建ModuleLoader实例
 * 2. 注册监听器(如DataSourceLoader、DataComparatorLoader等)
 * 3. 调用loadOfficial()加载内置模块
 * 4. 调用loadCustom()加载外部模块
 */
public class ModuleLoader {
    private static final Logger log = LoggerFactory.getLogger(ModuleLoader.class);

    /**
     * 监听器列表,用于存储所有注册的JAR包发现监听器
     * 当发现新的模块时会通知这些监听器进行处理
     */
    private final List<JarDiscoveryListener> listeners = new ArrayList<>();

    /**
     * 注册JAR包发现监听器
     * 监听器会在发现新模块时被调用,用于处理新发现的类
     * 
     * @param listener 实现了JarDiscoveryListener接口的监听器实例
     */
    public void registerListener(JarDiscoveryListener listener) {
        listeners.add(listener);
    }

    /**
     * 加载官方内置模块
     * 通过反射机制扫描指定包名下的所有类
     * 
     * 加载流程:
     * 1. 获取当前类加载器
     * 2. 创建Reflections实例扫描指定包
     * 3. 获取所有发现的类
     * 4. 通知监听器处理发现的类
     * 
     * @param basePackage 要扫描的基础包名,如"com.tangyujun.datashadow"
     */
    public void loadOfficial(String basePackage) {
        log.info("Start loading official modules from package: {}", basePackage);
        log.debug("Scanning package: {}", basePackage);

        // 获取所有类
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(basePackage))
                .setScanners(Scanners.SubTypes));

        // 获取指定包下的所有类
        Set<Class<?>> discoveredClasses = new HashSet<>();
        Set<String> classNames = reflections.getStore().get(Scanners.SubTypes.index()).values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        for (String className : classNames) {
            try {
                if (className.startsWith(basePackage)) {
                    Class<?> clazz = Class.forName(className);
                    discoveredClasses.add(clazz);
                }
            } catch (ClassNotFoundException e) {
                log.error("Failed to load class: {}", className, e);
            }
        }

        log.debug("Found {} classes in package {}", discoveredClasses.size(), basePackage);
        notifyListeners(getClass().getClassLoader(), discoveredClasses);
    }

    /**
     * 加载自定义外部模块
     * 扫描指定目录下的所有JAR文件并加载
     * 
     * @param pluginsPath 插件目录路径
     * @throws IOException 如果加载过程中发生IO错误
     */
    public void loadCustom(String pluginsPath) throws IOException {
        if (pluginsPath == null || pluginsPath.trim().isEmpty()) {
            log.warn("Plugin directory path is null or empty, skipping custom module loading");
            return;
        }

        File pluginsDir = new File(pluginsPath);
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            log.warn("Plugin directory does not exist or is not a directory: {}", pluginsPath);
            return;
        }

        File[] jarFiles = pluginsDir.listFiles((d, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            log.warn("No jar files found in directory: {}", pluginsPath);
            return;
        }

        List<URL> urls = new ArrayList<>();
        for (File jarFile : jarFiles) {
            validateJarFile(jarFile);
            urls.add(jarFile.toURI().toURL());
            log.info("Found jar file: {}", jarFile.getAbsolutePath());
        }

        try (URLClassLoader classLoader = new URLClassLoader(urls.toArray(URL[]::new), getClass().getClassLoader())) {
            Thread.currentThread().setContextClassLoader(classLoader);

            Reflections reflections = new Reflections(new org.reflections.util.ConfigurationBuilder()
                    .setUrls(urls)
                    .addClassLoaders(classLoader)
                    .setScanners(org.reflections.scanners.Scanners.SubTypes));

            Set<Class<?>> discoveredClasses = reflections.getSubTypesOf(Object.class);
            notifyListeners(classLoader, discoveredClasses);
        }
    }

    /**
     * 验证JAR文件的有效性
     * 检查JAR文件是否可以正常打开且包含class文件
     * 
     * @param jarFile 要验证的JAR文件
     * @throws IOException 如果JAR文件无法打开或验证过程出错
     */
    private void validateJarFile(File jarFile) throws IOException {
        try (JarFile jar = new JarFile(jarFile)) {
            jar.entries().asIterator().forEachRemaining(entry -> {
                if (entry.getName().endsWith(".class")) {
                    log.debug("Found class file in jar: {}", entry.getName());
                }
            });
        }
    }

    /**
     * 通知所有监听器处理新发现的类
     * 遍历所有注册的监听器,调用其onJarDiscovered方法
     * 如果某个监听器处理失败,会记录错误日志但不影响其他监听器
     * 
     * @param classLoader       用于加载发现的类的类加载器
     * @param discoveredClasses 发现的类集合
     */
    private void notifyListeners(ClassLoader classLoader, Set<Class<?>> discoveredClasses) {
        for (JarDiscoveryListener listener : listeners) {
            try {
                listener.onJarDiscovered(classLoader, discoveredClasses);
            } catch (Exception e) {
                log.error("Error notifying listener: {}", listener.getClass().getName(), e);
            }
        }
    }
}