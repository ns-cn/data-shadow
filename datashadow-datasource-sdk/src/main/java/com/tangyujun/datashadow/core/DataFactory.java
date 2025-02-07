package com.tangyujun.datashadow.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;
import com.tangyujun.datashadow.datacomparator.defaults.StringDataComparator;
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
     * 主数据源组名称
     */
    private String primaryDataSourceGroupName;

    /**
     * 主数据源名称
     */
    private String primaryDataSourceName;

    /**
     * 主数据源
     */
    private DataSource primaryDataSource;

    /**
     * 影子数据源组名称
     */
    private String shadowDataSourceGroupName;

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
    private final Map<String, Map<String, DataSourceGenerator>> dataSources = new LinkedHashMap<>();

    /**
     * 已注册的所有数据比较器
     * 例如{"数值":{"整数":IntegerDataComparator.generator(),"浮点数":DoubleDataComparator.generator()},
     * "布尔值":{"布尔值":BooleanDataComparator.generator()}}
     */
    private final Map<String, Map<String, DataComparatorGenerator>> dataComparators = new LinkedHashMap<>();

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
     * 通知所有数据项变化监听器
     */
    public void notifyDataItemChangeListeners() {
        dataItemChangeListeners.forEach(listener -> listener.onDataItemChanged());
    }

    /**
     * 注册一个新的数据比较器
     * 
     * @param group        数据比较器组
     * @param friendlyName 数据比较器友好名称
     * 
     * @param generator    数据比较器生成器
     */
    public void registerDataComparator(String group, String friendlyName, DataComparatorGenerator generator) {
        if (dataComparators.containsKey(group)) {
            dataComparators.get(group).put(friendlyName, generator);
        } else {
            Map<String, DataComparatorGenerator> comparators = new LinkedHashMap<>();
            comparators.put(friendlyName, generator);
            dataComparators.put(group, comparators);
        }
    }

    /**
     * 获取数据比较器
     * 
     * @param group        数据比较器组
     * @param friendlyName 数据比较器友好名称
     * @return 数据比较器
     */
    public DataComparatorGenerator getDataComparator(String group, String friendlyName) {
        if (group == null || friendlyName == null) {
            return null;
        }
        Map<String, DataComparatorGenerator> comparators = dataComparators.get(group);
        if (comparators == null) {
            return null;
        }
        return Optional.ofNullable(comparators.get(friendlyName)).orElse(StringDataComparator.ignoreAndNullEquals());
    }

    /**
     * 获取所有已注册的数据比较器
     * 
     * @return 数据比较器映射表
     */
    public Map<String, Map<String, DataComparatorGenerator>> getDataComparators() {
        return dataComparators;
    }

    /**
     * 注册一个新的数据源
     * 
     * @param group        数据源分组
     * @param friendlyName 数据源友好名称
     * @param generator    数据源生成器
     */
    public void registerDataSource(String group, String friendlyName, DataSourceGenerator generator) {
        if (dataSources.containsKey(group)) {
            dataSources.get(group).put(friendlyName, generator);
        } else {
            Map<String, DataSourceGenerator> generators = new LinkedHashMap<>();
            generators.put(friendlyName, generator);
            dataSources.put(group, generators);
        }
    }

    /**
     * 注销一个数据源
     * 
     * @param group        数据源分组名称
     * @param friendlyName 数据源友好名称
     */
    public void unregisterDataSource(String group, String friendlyName) {
        if (group != null && dataSources.containsKey(group)) {
            Map<String, DataSourceGenerator> generators = dataSources.get(group);
            if (generators != null && friendlyName != null) {
                generators.remove(friendlyName);
            }
        }
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
     * 获取主数据源组名称
     * 
     * @return 主数据源组名称
     */
    public String getPrimaryDataSourceGroupName() {
        return primaryDataSourceGroupName;
    }

    /**
     * 设置主数据源组名称
     * 
     * @param primaryDataSourceGroupName 主数据源组名称
     */
    public void setPrimaryDataSourceGroupName(String primaryDataSourceGroupName) {
        this.primaryDataSourceGroupName = primaryDataSourceGroupName;
    }

    /**
     * 获取影子数据源组名称
     * 
     * @return 影子数据源组名称
     */
    public String getShadowDataSourceGroupName() {
        return shadowDataSourceGroupName;
    }

    /**
     * 设置影子数据源组名称
     * 
     * @param shadowDataSourceGroupName 影子数据源组名称
     */
    public void setShadowDataSourceGroupName(String shadowDataSourceGroupName) {
        this.shadowDataSourceGroupName = shadowDataSourceGroupName;
    }

    /**
     * 设置主数据源
     * 
     * @param groupName         数据源分组名称
     * @param sourceName        数据源名称
     * @param primaryDataSource 主数据源实例
     */
    public void setPrimaryDataSource(String groupName, String sourceName, DataSource primaryDataSource) {
        this.primaryDataSourceGroupName = groupName;
        this.primaryDataSourceName = sourceName;
        this.primaryDataSource = primaryDataSource;
        dataSourceChangeListeners
                .forEach(listener -> listener.onDataSourceChanged(true, groupName, sourceName, primaryDataSource));
    }

    /**
     * 设置影子数据源
     * 
     * @param groupName        数据源分组名称
     * @param sourceName       数据源名称
     * @param shadowDataSource 影子数据源实例
     */
    public void setShadowDataSource(String groupName, String sourceName, DataSource shadowDataSource) {
        this.shadowDataSourceGroupName = groupName;
        this.shadowDataSourceName = sourceName;
        this.shadowDataSource = shadowDataSource;
        dataSourceChangeListeners
                .forEach(listener -> listener.onDataSourceChanged(false, groupName, sourceName, shadowDataSource));
    }

    /**
     * 获取所有已注册的数据源
     * 
     * @return 数据源映射表
     */
    public Map<String, Map<String, DataSourceGenerator>> getDataSources() {
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
