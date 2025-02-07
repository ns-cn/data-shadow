package com.tangyujun.datashadow.scheme;

import java.util.Map;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;

/**
 * 数据源存储类
 * 用于将数据源对象序列化存储和反序列化读取
 */
public class DataSourceStorage extends Storable<DataSource> {

    /**
     * 数据源分组
     */
    private String group;

    /**
     * 数据源名称
     */
    private String sourceName;

    /**
     * 数据源序列化后的值
     */
    private String sourceValue;

    /**
     * 获取数据源分组
     * 
     * @return 数据源分组
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置数据源分组
     * 
     * @param group 数据源分组
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取数据源名称
     * 
     * @return 数据源名称
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * 设置数据源名称
     * 
     * @param sourceName 数据源名称
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * 获取数据源序列化后的值
     * 
     * @return 数据源序列化后的值
     */
    public String getSourceValue() {
        return sourceValue;
    }

    /**
     * 设置数据源序列化后的值
     * 
     * @param sourceValue 数据源序列化后的值
     */
    public void setSourceValue(String sourceValue) {
        this.sourceValue = sourceValue;
    }

    /**
     * 将数据源对象转换为存储对象
     * 
     * @param object 数据源对象
     */
    @Override
    public void from(DataSource object) {
        if (object == null) {
            return;
        }
        this.sourceValue = object.exportSource();
    }

    /**
     * 将存储对象转换为数据源对象
     * 
     * @return 数据源对象,如果数据源生成器不存在则返回null
     */
    @Override
    public DataSource to() {
        DataFactory factory = DataFactory.getInstance();
        Map<String, Map<String, DataSourceGenerator>> dataSources = factory.getDataSources();
        DataSourceGenerator dataSourceGenerator = dataSources == null ? null
                : dataSources.get(group) == null ? null : dataSources.get(group).get(sourceName);
        if (dataSourceGenerator == null) {
            return null;
        }
        DataSource dataSource = dataSourceGenerator.generate();
        dataSource.importSource(sourceValue);
        return dataSource;
    }
}
