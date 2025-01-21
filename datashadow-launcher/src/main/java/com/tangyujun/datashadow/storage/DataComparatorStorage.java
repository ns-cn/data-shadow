package com.tangyujun.datashadow.storage;

import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datacomparator.DataComparator;
import com.tangyujun.datashadow.datacomparator.DataComparatorGenerator;

/**
 * 数据比较器存储类
 * 用于将数据比较器序列化存储和反序列化读取
 */
public class DataComparatorStorage extends Storable<DataComparator> {

    /**
     * 数据比较器组名
     */
    private String group;

    /**
     * 数据比较器友好名称
     */
    private String friendlyName;

    /**
     * 数据比较器配置信息
     */
    private String config;

    /**
     * 获取数据比较器组名
     * 
     * @return 数据比较器组名
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置数据比较器组名
     * 
     * @param group 数据比较器组名
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取数据比较器友好名称
     * 
     * @return 数据比较器友好名称
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * 设置数据比较器友好名称
     * 
     * @param friendlyName 数据比较器友好名称
     */
    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    /**
     * 获取数据比较器配置信息
     * 
     * @return 数据比较器配置信息
     */
    public String getConfig() {
        return config;
    }

    /**
     * 设置数据比较器配置信息
     * 
     * @param config 数据比较器配置信息
     */
    public void setConfig(String config) {
        this.config = config;
    }

    /**
     * 从数据比较器对象中提取配置信息
     * 
     * @param object 数据比较器对象
     */
    @Override
    public void from(DataComparator object) {
        this.config = object.exportComparator();
    }

    /**
     * 根据存储的配置信息创建数据比较器对象
     * 
     * @return 数据比较器对象,如果无法找到对应的比较器生成器则返回null
     */
    @Override
    public DataComparator to() {
        DataFactory factory = DataFactory.getInstance();
        DataComparatorGenerator comparatorGenerator = factory.getDataComparator(this.group, this.friendlyName);
        if (comparatorGenerator == null) {
            return null;
        }
        DataComparator comparator = comparatorGenerator.generate();
        comparator.importComparator(this.config);
        return comparator;
    }
}
