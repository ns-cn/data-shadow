package com.tangyujun.datashadow.module.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.DataComparatorRegistry;

/**
 * 数据比较器加载器
 * 用于动态加载和注册所有可用的数据比较器
 * 通过反射机制扫描指定包下带有@DataComparatorRegistry注解的方法
 * 并将这些方法生成的数据比较器注册到DataFactory中
 */
public class DataComparatorListener implements JarDiscoveryListener {
    private static final Logger log = LoggerFactory.getLogger(DataComparatorListener.class);
    private final DataFactory dataFactory = DataFactory.getInstance();

    /**
     * 当发现新的JAR文件时，调用此方法
     * 
     * 加载流程:
     * 1. 扫描JAR文件中的所有类
     * 2. 验证每个类的方法是否带有@DataComparatorRegistry注解
     * 3. 如果方法带有注解,则调用方法生成数据比较器,并注册到DataFactory中
     * 
     * @param classLoader       加载JAR文件的ClassLoader
     * @param discoveredClasses 在JAR文件中发现的类
     */
    @Override
    public void onJarDiscovered(ClassLoader classLoader, Set<Class<?>> discoveredClasses) {
        int loadedCount = 0;
        for (Class<?> clazz : discoveredClasses) {
            for (Method method : clazz.getDeclaredMethods()) {
                DataComparatorRegistry annotation = method.getAnnotation(DataComparatorRegistry.class);
                if (annotation != null && isValidMethod(method)) {
                    try {
                        String group = annotation.group();
                        String friendlyName = annotation.friendlyName();

                        DataComparatorGenerator generator = (DataComparatorGenerator) method.invoke(null);
                        dataFactory.registerDataComparator(group, friendlyName, generator);
                        loadedCount++;

                        log.info("Successfully registered data comparator: {}.{}", group, friendlyName);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.error("Error registering data comparator: {}#{}",
                                clazz.getSimpleName(), method.getName(), e);
                    }
                }
            }
        }
        log.info("Data comparator loading completed, {} comparators loaded", loadedCount);
    }

    /**
     * 验证方法是否符合数据比较器生成器的条件
     * 
     * @param method 要验证的方法
     * @return 如果方法符合条件,则返回true,否则返回false
     */
    private boolean isValidMethod(Method method) {
        return java.lang.reflect.Modifier.isPublic(method.getModifiers()) &&
                java.lang.reflect.Modifier.isStatic(method.getModifiers()) &&
                method.getParameterCount() == 0 &&
                DataComparatorGenerator.class.isAssignableFrom(method.getReturnType());
    }
}
