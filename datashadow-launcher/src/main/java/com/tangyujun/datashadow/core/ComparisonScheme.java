package com.tangyujun.datashadow.core;

import java.util.List;

import com.tangyujun.datashadow.dataitem.DataItem;

/**
 * 对比方案类
 * 用于存储和管理数据对比的配置信息,包括数据项和数据源的配置
 */
public class ComparisonScheme {
    /**
     * 数据项列表
     * 存储需要进行对比的数据字段信息
     */
    List<DataItem> dataItems;

    /**
     * 主数据源类型名称
     * 存储主数据源的类型标识,用于识别数据源类型
     */
    String primaryDataSourceName;

    /**
     * 主数据源序列化后的配置字符串
     * 存储主数据源的具体配置信息,如数据库连接信息、文件路径等
     */
    String primaryDataSource;

    /**
     * 影子数据源类型名称
     * 存储影子数据源的类型标识,用于识别数据源类型
     */
    String shadowDataSourceName;

    /**
     * 影子数据源序列化后的配置字符串
     * 存储影子数据源的具体配置信息,如数据库连接信息、文件路径等
     */
    String shadowDataSource;

    /**
     * 获取数据项列表
     * 
     * @return 返回配置的数据项列表,包含需要对比的字段信息
     */
    public List<DataItem> getDataItems() {
        return dataItems;
    }

    /**
     * 设置数据项列表
     * 
     * @param dataItems 要设置的数据项列表,包含需要对比的字段信息
     */
    public void setDataItems(List<DataItem> dataItems) {
        this.dataItems = dataItems;
    }

    /**
     * 获取主数据源类型名称
     * 
     * @return 返回主数据源的类型标识
     */
    public String getPrimaryDataSourceName() {
        return primaryDataSourceName;
    }

    /**
     * 设置主数据源类型名称
     * 
     * @param primaryDataSourceName 要设置的主数据源类型标识
     */
    public void setPrimaryDataSourceName(String primaryDataSourceName) {
        this.primaryDataSourceName = primaryDataSourceName;
    }

    /**
     * 获取主数据源序列化后的配置字符串
     * 
     * @return 返回主数据源的配置信息字符串
     */
    public String getPrimaryDataSource() {
        return primaryDataSource;
    }

    /**
     * 设置主数据源序列化后的配置字符串
     * 
     * @param primaryDataSource 要设置的主数据源配置信息字符串
     */
    public void setPrimaryDataSource(String primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    /**
     * 获取影子数据源类型名称
     * 
     * @return 返回影子数据源的类型标识
     */
    public String getShadowDataSourceName() {
        return shadowDataSourceName;
    }

    /**
     * 设置影子数据源类型名称
     * 
     * @param shadowDataSourceName 要设置的影子数据源类型标识
     */
    public void setShadowDataSourceName(String shadowDataSourceName) {
        this.shadowDataSourceName = shadowDataSourceName;
    }

    /**
     * 获取影子数据源序列化后的配置字符串
     * 
     * @return 返回影子数据源的配置信息字符串
     */
    public String getShadowDataSource() {
        return shadowDataSource;
    }

    /**
     * 设置影子数据源序列化后的配置字符串
     * 
     * @param shadowDataSource 要设置的影子数据源配置信息字符串
     */
    public void setShadowDataSource(String shadowDataSource) {
        this.shadowDataSource = shadowDataSource;
    }
}
