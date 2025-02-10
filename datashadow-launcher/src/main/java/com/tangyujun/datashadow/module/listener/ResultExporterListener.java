package com.tangyujun.datashadow.module.listener;

import java.lang.reflect.InvocationTargetException;

import com.tangyujun.datashadow.core.Environment;
import com.tangyujun.datashadow.exporter.ResultExporter;
import com.tangyujun.datashadow.exporter.ResultExporterRegistry;
import com.tangyujun.datashadow.module.JarDiscoveryListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * 结果导出器加载器
 * 用于动态加载和注册所有可用的结果导出器
 */
public class ResultExporterListener implements JarDiscoveryListener {
    private static final Logger log = LoggerFactory.getLogger(ResultExporterListener.class);

    /**
     * 扫描指定目录下的jar包,并加载其中的导出器
     */
    @Override
    public void onJarDiscovered(ClassLoader classLoader, Set<Class<?>> discoveredClasses) {
        int loadedCount = 0;
        for (Class<?> clazz : discoveredClasses) {
            ResultExporterRegistry annotation = clazz.getAnnotation(ResultExporterRegistry.class);
            if (annotation != null && isValidExporterClass(clazz)) {
                try {
                    String group = annotation.group();
                    String name = annotation.name();

                    ResultExporter exporter = (ResultExporter) clazz.getDeclaredConstructor().newInstance();
                    Environment.registerExporter(group, name, exporter);
                    loadedCount++;
                    log.info("Successfully registered result exporter: {}", name);
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                        | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                    log.error("Error registering result exporter: {}", clazz.getSimpleName(), e);
                }
            }
        }
        if (loadedCount > 0) {
            log.info("Result exporter loading completed, {} exporters loaded", loadedCount);
        }
    }

    /**
     * 检查类是否是有效的导出器类
     * 
     * @param clazz 要检查的类
     * @return 是否是有效的导出器类
     */
    private boolean isValidExporterClass(Class<?> clazz) {
        return !Modifier.isAbstract(clazz.getModifiers()) &&
                !Modifier.isInterface(clazz.getModifiers()) &&
                ResultExporter.class.isAssignableFrom(clazz);
    }
}
