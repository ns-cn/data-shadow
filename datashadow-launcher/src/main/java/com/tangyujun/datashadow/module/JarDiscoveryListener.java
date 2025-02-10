package com.tangyujun.datashadow.module;

import java.util.Set;

/**
 * JAR包发现监听器接口
 * 用于在程序运行时动态发现和加载JAR包中的组件
 * 实现此接口的类可以监听JAR包的发现事件,并对其中的类进行处理
 * 主要用于实现数据源和数据比较器的动态加载功能
 */
public interface JarDiscoveryListener {
    /**
     * 当发现新的JAR包时调用此方法
     * 
     * 处理流程:
     * 1. 使用提供的ClassLoader加载JAR包
     * 2. 扫描JAR包中的所有类
     * 3. 根据具体实现类的需求处理发现的类
     * 4. 例如加载数据源或数据比较器等组件
     * 
     * @param classLoader       用于加载JAR包的类加载器,可用于加载JAR包中的类
     * @param discoveredClasses JAR包中发现的所有类的集合,包含了JAR包中所有可访问的类
     */
    void onJarDiscovered(ClassLoader classLoader, Set<Class<?>> discoveredClasses);
}