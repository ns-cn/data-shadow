package com.tangyujun.datashadow.scheme;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.core.DataFactory;

/**
 * 对比方案类
 * 用于存储和管理数据对比的配置信息,包括数据项和数据源的配置
 * 对比方案包含了数据项列表、主数据源和影子数据源的配置信息
 * 可以将对比方案序列化为字符串进行存储,也可以从字符串反序列化恢复对比方案
 * 提供了从数据工厂获取当前配置和将配置应用到数据工厂的功能
 */
public class ComparisonScheme {
    /**
     * 数据项列表
     * 存储需要进行对比的数据字段信息
     * 每个数据项包含字段的编码、名称、比较器等配置
     */
    List<DataItemStorage> dataItems;

    /**
     * 主数据源配置
     * 存储主数据源的配置信息,包括数据源类型和具体配置
     * 主数据源用于提供基准数据进行比对
     */
    DataSourceStorage primaryDataSource;

    /**
     * 影子数据源配置
     * 存储影子数据源的配置信息,包括数据源类型和具体配置
     * 影子数据源用于提供待比对的数据
     */
    DataSourceStorage shadowDataSource;

    /**
     * 获取数据项列表
     * 
     * @return 返回配置的数据项列表,包含需要对比的字段信息
     */
    public List<DataItemStorage> getDataItems() {
        return dataItems;
    }

    /**
     * 设置数据项列表
     * 
     * @param dataItems 要设置的数据项列表,包含需要对比的字段信息
     */
    public void setDataItems(List<DataItemStorage> dataItems) {
        this.dataItems = dataItems;
    }

    /**
     * 获取主数据源配置
     * 
     * @return 主数据源配置
     */
    public DataSourceStorage getPrimaryDataSource() {
        return primaryDataSource;
    }

    /**
     * 设置主数据源配置
     * 
     * @param primaryDataSource 主数据源配置
     */
    public void setPrimaryDataSource(DataSourceStorage primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    /**
     * 获取影子数据源配置
     * 
     * @return 影子数据源配置
     */
    public DataSourceStorage getShadowDataSource() {
        return shadowDataSource;
    }

    /**
     * 设置影子数据源配置
     * 
     * @param shadowDataSource 影子数据源配置
     */
    public void setShadowDataSource(DataSourceStorage shadowDataSource) {
        this.shadowDataSource = shadowDataSource;
    }

    /**
     * 将对比方案序列化为字符串
     * 使用JSON格式将当前对比方案的所有配置信息转换为字符串
     * 
     * @return 包含对比方案配置信息的JSON字符串
     */
    public String exportScheme() {
        return JSON.toJSONString(this);
    }

    /**
     * 从数据工厂中提取对比方案
     * 获取数据工厂中当前的配置信息,包括数据项列表和数据源配置
     * 将这些配置信息保存到当前对比方案对象中
     */
    public static ComparisonScheme snapshot() {
        DataFactory factory = DataFactory.getInstance();
        ComparisonScheme scheme = new ComparisonScheme();
        // 转换并保存数据项列表
        scheme.dataItems = factory.getDataItems().stream().map(item -> {
            DataItemStorage storage = new DataItemStorage();
            storage.from(item);
            return storage;
        }).collect(Collectors.toList());
        // 保存主数据源配置
        if (factory.getPrimaryDataSource() != null) {
            DataSourceStorage storage = new DataSourceStorage();
            storage.setGroup(factory.getPrimaryDataSourceGroupName());
            storage.setSourceName(factory.getPrimaryDataSourceName());
            storage.from(factory.getPrimaryDataSource());
            scheme.setPrimaryDataSource(storage);
        }
        // 保存影子数据源配置
        if (factory.getShadowDataSource() != null) {
            DataSourceStorage storage = new DataSourceStorage();
            storage.setGroup(factory.getShadowDataSourceGroupName());
            storage.setSourceName(factory.getShadowDataSourceName());
            storage.from(factory.getShadowDataSource());
            scheme.setShadowDataSource(storage);
        }
        return scheme;
    }

    /**
     * 将对比方案应用到数据工厂
     * 将当前对比方案中的配置信息应用到数据工厂中
     * 包括设置数据项列表、主数据源和影子数据源的配置
     */
    public void applyToFactory() {
        DataFactory factory = DataFactory.getInstance();
        // 清空并设置数据项列表
        factory.clearDataItems();
        factory.addDataItems(this.dataItems.stream().map(DataItemStorage::to).collect(Collectors.toList()));
        // 设置主数据源
        if (this.primaryDataSource != null) {
            factory.setPrimaryDataSource(this.primaryDataSource.getGroup(), this.primaryDataSource.getSourceName(),
                    this.primaryDataSource.to());
        }
        // 设置影子数据源
        if (this.shadowDataSource != null) {
            factory.setShadowDataSource(this.shadowDataSource.getGroup(), this.shadowDataSource.getSourceName(),
                    this.shadowDataSource.to());
        }
    }
}
