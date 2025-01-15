package com.tangyujun.datashadow.datasource;

/**
 * 数据源生成器
 */
@FunctionalInterface
public interface DataSourceGenerator {

    /**
     * 生成数据源
     * 
     * @return 数据源
     */
    DataSource generate();
}
