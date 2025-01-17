package com.tangyujun.datashadow.core;

/**
 * 数据源变化监听器
 */
public interface DataSourceChangeListener {
    /**
     * 数据源变化时触发
     */
    void onDataSourceChanged(boolean isPrimary);
}
