package com.tangyujun.datashadow.core;

import com.tangyujun.datashadow.datasource.DataSource;

/**
 * 数据源变化监听器
 */
public interface DataSourceChangeListener {
    /**
     * 数据源变化时触发
     * 
     * @param isPrimary  是否为主数据源
     * @param sourceName 数据源名称
     * @param dataSource 数据源
     */
    void onDataSourceChanged(boolean isPrimary, String sourceName, DataSource dataSource);
}
