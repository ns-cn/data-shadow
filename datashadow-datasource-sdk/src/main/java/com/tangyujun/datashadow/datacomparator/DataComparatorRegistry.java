package com.tangyujun.datashadow.datacomparator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据比较器注册器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataComparatorRegistry {
    /**
     * 数据比较器友好名称
     * 
     * @return 数据比较器友好名称
     */
    String friendlyName();

    /**
     * 数据比较器分组
     * 
     * 分组用于在配置界面中显示
     * 如果不指定，则为“未分组”
     * 
     * @return 数据比较器分组
     */
    String group() default "";
}
