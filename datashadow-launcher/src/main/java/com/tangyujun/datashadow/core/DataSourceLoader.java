package com.tangyujun.datashadow.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;

/**
 * 数据源加载器
 * 用于动态加载和注册所有可用的数据源
 * 通过反射机制扫描指定包下带有@DataSourceRegistry注解的方法
 * 并将这些方法生成的数据源注册到DataFactory中
 */
public class DataSourceLoader {
    private static final Logger log = LoggerFactory.getLogger(DataSourceLoader.class);

    /**
     * 数据工厂实例,用于注册数据源
     */
    private final DataFactory dataFactory = DataFactory.getInstance();

    /**
     * 加载并注册所有可用官方提供的数据源
     * 扫描指定包下所有类中带有@DataSourceRegistry注解的方法
     * 方法必须满足以下条件:
     * 1. 有@DataSourceRegistry注解
     * 2. 是public static方法
     * 3. 无参数
     * 4. 返回值类型为DataSourceGenerator
     * 符合条件的方法将被调用,生成的DataSourceGenerator实例将被注册到DataFactory中
     */
    public void loadOfficial() {
        log.info("Start loading data sources...");
        log.debug("ClassLoader: {}", this.getClass().getClassLoader());
        log.debug("ClassPath: {}", System.getProperty("java.class.path"));
        // 修改 Reflections 的配置
        Reflections reflections = new Reflections(new org.reflections.util.ConfigurationBuilder()
                .setUrls(org.reflections.util.ClasspathHelper.forPackage("com.tangyujun.datashadow.datasource"))
                .setScanners(
                        org.reflections.scanners.Scanners.SubTypes,
                        org.reflections.scanners.Scanners.TypesAnnotated,
                        org.reflections.scanners.Scanners.MethodsAnnotated));

        // 直接使用 getMethodsAnnotatedWith 来获取带有特定注解的方法
        Set<Method> methods = reflections.getMethodsAnnotatedWith(DataSourceRegistry.class);
        log.debug("Found {} annotated methods", methods.size());

        int loadedCount = 0;
        for (Method method : methods) {
            // 检查方法是否满足其他条件
            if (Modifier.isPublic(method.getModifiers()) &&
                    Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 0 &&
                    DataSourceGenerator.class.isAssignableFrom(method.getReturnType())) {

                try {
                    DataSourceRegistry annotation = method.getAnnotation(DataSourceRegistry.class);
                    String friendlyName = annotation.friendlyName();
                    log.info("Found data source registration method: {}#{}, friendly name: {}",
                            method.getDeclaringClass().getSimpleName(), method.getName(), friendlyName);

                    DataSourceGenerator generator = (DataSourceGenerator) method.invoke(null);
                    dataFactory.registerDataSource(friendlyName, generator);
                    loadedCount++;
                    log.info("Successfully registered data source: {}", friendlyName);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Error occurred while registering data source: {}#{}",
                            method.getDeclaringClass().getSimpleName(), method.getName(), e);
                }
            }
        }
        log.info("Data source loading completed, {} data sources loaded", loadedCount);
    }

    /**
     * 加载并注册所有自定义数据源
     * 加载指定目录下的所有jar包
     * 并扫描指定目录下所有jar包中所有类中带有@DataSourceRegistry注解的方法
     * 方法必须满足以下条件:
     * 1. 有@DataSourceRegistry注解
     * 2. 是public static方法
     * 3. 无参数
     * 4. 返回值类型为DataSourceGenerator
     * 
     * @param path 自定义数据源放jar的目录
     * @throws IOException
     */
    public void loadCustom(String path) throws IOException {
        log.info("Start loading custom data sources...");
        File file = new File(path);
        if (!file.exists()) {
            log.error("Custom data source directory does not exist: {}", path);
            return;
        }

        // 获取目录下所有jar文件
        File[] jarFiles = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            log.warn("No jar files found in directory: {}", path);
            return;
        }

        // 在扫描之前添加以下代码来验证jar内容
        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                jar.entries().asIterator().forEachRemaining(entry -> {
                    if (entry.getName().endsWith(".class")) {
                        log.debug("Found class file in jar: {}", entry.getName());
                    }
                });
            } catch (IOException e) {
                log.error("Error reading jar file: {}", jarFile.getName(), e);
            }
        }

        // 创建URL数组用于加载jar
        List<URL> urls = new ArrayList<>();
        try {
            for (File jarFile : jarFiles) {
                urls.add(jarFile.toURI().toURL());
                log.info("Found jar file: {}", jarFile.getAbsolutePath());
            }
        } catch (MalformedURLException e) {
            log.error("Error converting jar file path to URL", e);
            return;
        }

        // 创建自定义的类加载器
        URLClassLoader classLoader = new URLClassLoader(urls.toArray(URL[]::new), this.getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(classLoader);

        log.debug("Created URLClassLoader with URLs:");
        for (URL url : urls) {
            log.debug(" - {}", url);
        }

        // 验证类加载器是否可以访问jar
        for (URL url : urls) {
            try {
                try (URLClassLoader testLoader = new URLClassLoader(new URL[] { url })) {
                    testLoader.loadClass("com.tangyujun.datashadow.datasource.file.DataSourceExcel");
                    log.debug("Successfully loaded test class from {}", url);
                }
            } catch (ClassNotFoundException e) {
                log.warn("Could not load test class from {}: {}", url, e.getMessage());
            }
        }

        // 扫描所有jar包中的类
        Reflections reflections = new Reflections(new org.reflections.util.ConfigurationBuilder()
                .setUrls(urls)
                .addClassLoaders(classLoader)
                .setScanners(
                        org.reflections.scanners.Scanners.SubTypes,
                        org.reflections.scanners.Scanners.TypesAnnotated,
                        org.reflections.scanners.Scanners.MethodsAnnotated,
                        org.reflections.scanners.Scanners.Resources)
                .filterInputsBy(input -> {
                    // 确保扫描jar包内的所有类
                    return true;
                }));

        // 在获取方法之前，先尝试扫描所有类
        Set<Class<?>> allTypes = reflections.getSubTypesOf(Object.class);
        log.debug("Found types in jar: {}", allTypes.size());
        allTypes.forEach(type -> log.debug("Found type: {}", type.getName()));

        // 获取所有带注解的方法
        Set<Method> methods = reflections.getMethodsAnnotatedWith(DataSourceRegistry.class);
        log.debug("Found {} annotated methods in custom jars", methods.size());

        int loadedCount = 0;
        for (Method method : methods) {
            // 检查方法是否满足条件
            if (Modifier.isPublic(method.getModifiers()) &&
                    Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterCount() == 0 &&
                    DataSourceGenerator.class.isAssignableFrom(method.getReturnType())) {

                try {
                    DataSourceRegistry annotation = method.getAnnotation(DataSourceRegistry.class);
                    String friendlyName = annotation.friendlyName();
                    log.info("Found custom data source registration method: {}#{}, friendly name: {}",
                            method.getDeclaringClass().getSimpleName(), method.getName(), friendlyName);

                    DataSourceGenerator generator = (DataSourceGenerator) method.invoke(null);
                    dataFactory.registerDataSource(friendlyName, generator);
                    loadedCount++;
                    log.info("Successfully registered custom data source: {}", friendlyName);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Error occurred while registering custom data source: {}#{}",
                            method.getDeclaringClass().getSimpleName(), method.getName(), e);
                }
            }
        }
        log.info("Custom data source loading completed, {} data sources loaded", loadedCount);

        try {
            classLoader.close();
        } catch (IOException e) {
            log.error("Error closing custom class loader", e);
        }
    }
}
