package com.tangyujun.datashadow.core;

import java.util.HashMap;
import java.util.Map;

import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * 数据工厂类
 * 用于管理所有数据项和数据源
 * 采用单例模式确保全局唯一实例
 */
public class DataFactory {
    /**
     * 单例实例
     */
    private static final DataFactory INSTANCE = new DataFactory();

    /**
     * 数据项列表,使用JavaFX的ObservableList实现数据绑定
     */
    private final ObservableList<DataItem> dataItems = FXCollections.observableArrayList();

    /**
     * 主数据源
     */
    private DataSource primaryDataSource;

    /**
     * 影子数据源
     */
    private DataSource shadowDataSource;

    /**
     * 已注册的所有数据源列表
     */
    private final Map<String, DataSourceGenerator> dataSources = new HashMap<>();

    /**
     * 私有构造函数,防止外部实例化
     */
    private DataFactory() {
    }

    /**
     * 获取DataFactory单例实例
     * 
     * @return DataFactory实例
     */
    public static DataFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获取数据项列表
     * 
     * @return 可观察的数据项列表
     */
    public ObservableList<DataItem> getDataItems() {
        return dataItems;
    }

    /**
     * 注册一个新的数据源
     * 
     * @param dataSource 要注册的数据源
     */
    public void registerDataSource(String friendlyName, DataSourceGenerator generator) {
        dataSources.put(friendlyName, generator);
    }

    /**
     * 注销一个数据源
     * 
     * @param friendlyName 数据源友好名称
     */
    public void unregisterDataSource(String friendlyName) {
        dataSources.remove(friendlyName);
    }

    /**
     * 获取主数据源
     * 
     * @return 主数据源
     */
    public DataSource getPrimaryDataSource() {
        return primaryDataSource;
    }

    /**
     * 获取影子数据源
     * 
     * @return 影子数据源
     */
    public DataSource getShadowDataSource() {
        return shadowDataSource;
    }

    /**
     * 设置主数据源
     * 
     * @param primaryDataSource 主数据源
     */
    public void setPrimaryDataSource(DataSource primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    /**
     * 设置影子数据源
     * 
     * @param shadowDataSource 影子数据源
     */
    public void setShadowDataSource(DataSource shadowDataSource) {
        this.shadowDataSource = shadowDataSource;
    }

    /**
     * 获取所有已注册的数据源
     * 
     * @return 数据源映射表
     */
    public Map<String, DataSourceGenerator> getDataSources() {
        return dataSources;
    }
}
