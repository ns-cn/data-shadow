package com.tangyujun.datashadow.datasource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源注册器
 * 只能注解在类的public static方法上
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceRegistry {
    /**
     * 数据源友好名称
     * 
     * @return 数据源友好名称
     */
    String friendlyName();
}
