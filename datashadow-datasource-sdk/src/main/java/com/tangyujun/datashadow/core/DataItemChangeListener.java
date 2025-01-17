package com.tangyujun.datashadow.core;

import com.tangyujun.datashadow.dataitem.DataItem;

/**
 * 数据项变化监听器
 */
public interface DataItemChangeListener {
    /**
     * 数据项整理发生变化时，例如导入
     */
    void onDataItemChanged();

    /**
     * 单个数据项新增
     * 
     * @param item 新增数据项
     */
    void onDataItemCreated(DataItem item);

    /**
     * 单个数据项修改
     * 
     * @param index   数据项索引下标
     * @param oldItem 旧数据项
     * @param newItem 新的数据项
     */
    void onDataItemUpdated(int index, DataItem oldItem, DataItem newItem);

    /**
     * 单个数据项删除
     * 
     * @param index 数据项索引下标
     * @param item  删除的数据项
     */
    void onDataItemDeleted(int index, DataItem item);
}
