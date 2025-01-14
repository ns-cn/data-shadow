package com.tangyujun.datashadow.core;

import com.tangyujun.datashadow.dataitem.DataItem;
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
}
