package com.tangyujun.datashadow.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
     * 主数据源名称
     */
    private String primaryDataSourceName;

    /**
     * 主数据源
     */
    private DataSource primaryDataSource;

    /**
     * 影子数据源名称
     */
    private String shadowDataSourceName;

    /**
     * 影子数据源
     */
    private DataSource shadowDataSource;

    /**
     * 已注册的所有数据源列表
     */
    private final Map<String, DataSourceGenerator> dataSources = new LinkedHashMap<>();

    /**
     * 数据项变化监听器列表
     */
    private final List<DataItemChangeListener> dataItemChangeListeners = new ArrayList<>();

    /**
     * 数据源变化监听器列表
     */
    private final List<DataSourceChangeListener> dataSourceChangeListeners = new ArrayList<>();

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
     * 清空数据项
     */
    public void clearDataItems() {
        dataItems.clear();
        dataItemChangeListeners.forEach(listener -> listener.onDataItemChanged());
    }

    /**
     * 添加数据项列表
     * 
     * @param items 要添加的数据项列表
     */
    public void addDataItems(List<DataItem> items) {
        dataItems.addAll(items);
        dataItemChangeListeners.forEach(listener -> listener.onDataItemChanged());
    }

    /**
     * 添加数据项
     * 
     * @param item 要添加的数据项
     */
    public void addDataItem(DataItem item) {
        dataItems.add(item);
        dataItemChangeListeners.forEach(listener -> listener.onDataItemCreated(item));
    }

    /**
     * 移除数据项
     * 
     * @param item 要移除的数据项
     */
    public void removeDataItem(DataItem item) {
        int index = dataItems.indexOf(item);
        dataItems.remove(item);
        dataItemChangeListeners.forEach(listener -> listener.onDataItemDeleted(index, item));
    }

    /**
     * 更新数据项
     * 
     * @param index 要更新的数据项的索引
     * @param item  要更新的数据项
     */
    public void updateDataItem(int index, DataItem item) {
        DataItem oldItem = dataItems.get(index);
        dataItems.set(index, item);
        dataItemChangeListeners.forEach(listener -> listener.onDataItemUpdated(index, oldItem, item));
    }

    /**
     * 注册一个新的数据源
     * 
     * @param friendlyName 数据源友好名称
     * @param generator    数据源生成器
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
     * 获取主数据源名称
     * 
     * @return 主数据源名称
     */
    public String getPrimaryDataSourceName() {
        return primaryDataSourceName;
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
     * 获取影子数据源名称
     * 
     * @return 影子数据源名称
     */
    public String getShadowDataSourceName() {
        return shadowDataSourceName;
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
     * @param sourceName        数据源名称
     * @param primaryDataSource 主数据源
     */
    public void setPrimaryDataSource(String sourceName, DataSource primaryDataSource) {
        this.primaryDataSourceName = sourceName;
        this.primaryDataSource = primaryDataSource;
        dataSourceChangeListeners
                .forEach(listener -> listener.onDataSourceChanged(true, sourceName, primaryDataSource));
    }

    /**
     * 设置影子数据源
     * 
     * @param sourceName       数据源名称
     * @param shadowDataSource 影子数据源
     */
    public void setShadowDataSource(String sourceName, DataSource shadowDataSource) {
        this.shadowDataSourceName = sourceName;
        this.shadowDataSource = shadowDataSource;
        dataSourceChangeListeners
                .forEach(listener -> listener.onDataSourceChanged(false, sourceName, shadowDataSource));
    }

    /**
     * 获取所有已注册的数据源
     * 
     * @return 数据源映射表
     */
    public Map<String, DataSourceGenerator> getDataSources() {
        return dataSources;
    }

    /**
     * 添加数据项变化监听器
     * 
     * @param listener 数据项变化监听器
     */
    public void addDataItemChangeListener(DataItemChangeListener listener) {
        dataItemChangeListeners.add(listener);
    }

    /**
     * 添加数据源变化监听器
     * 
     * @param listener 数据源变化监听器
     */
    public void addDataSourceChangeListener(DataSourceChangeListener listener) {
        dataSourceChangeListeners.add(listener);
    }

    /**
     * 移除数据项变化监听器
     * 
     * @param listener 数据项变化监听器
     */
    public void removeDataItemChangeListener(DataItemChangeListener listener) {
        dataItemChangeListeners.remove(listener);
    }

    /**
     * 移除数据源变化监听器
     * 
     * @param listener 数据源变化监听器
     */
    public void removeDataSourceChangeListener(DataSourceChangeListener listener) {
        dataSourceChangeListeners.remove(listener);
    }
}
