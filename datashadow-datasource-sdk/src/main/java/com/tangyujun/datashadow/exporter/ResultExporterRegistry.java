package com.tangyujun.datashadow.exporter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 结果导出器注册器
 * 用于注册结果导出器
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultExporterRegistry {
    /**
     * 导出器名称
     * 
     * @return 导出器名称
     */
    String name();

    /**
     * 导出器分组
     * 
     * @return 导出器分组
     */
    String group();
}
