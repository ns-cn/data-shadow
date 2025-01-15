package com.tangyujun.datashadow.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

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
     * 加载并注册所有可用的数据源
     * 扫描指定包下所有类中带有@DataSourceRegistry注解的方法
     * 方法必须满足以下条件:
     * 1. 有@DataSourceRegistry注解
     * 2. 是public static方法
     * 3. 无参数
     * 4. 返回值类型为DataSourceGenerator
     * 符合条件的方法将被调用,生成的DataSourceGenerator实例将被注册到DataFactory中
     */
    public void load() {
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
}
