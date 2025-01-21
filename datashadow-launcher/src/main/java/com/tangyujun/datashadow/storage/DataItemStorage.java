package com.tangyujun.datashadow.storage;

import com.tangyujun.datashadow.dataitem.DataItem;

/**
 * 数据项存储类
 * 用于将数据项序列化存储和反序列化读取
 */
public class DataItemStorage extends Storable<DataItem> {
    /**
     * 数据项编码,作为数据项的唯一标识
     */
    private String code;

    /**
     * 是否唯一,用于标识该数据项是否可以作为数据源的唯一标识
     */
    private boolean unique;

    /**
     * 数据项昵称,用于显示友好名称
     */
    private String nick;

    /**
     * 数据项备注说明
     */
    private String remark;

    /**
     * 数据项比较器
     */
    private DataComparatorStorage comparator;

    /**
     * 获取数据项编码
     * 
     * @return 数据项编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置数据项编码
     * 
     * @param code 数据项编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 获取是否唯一标识
     * 
     * @return 是否唯一标识
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * 设置是否唯一标识
     * 
     * @param unique 是否唯一标识
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * 获取数据项昵称
     * 
     * @return 数据项昵称
     */
    public String getNick() {
        return nick;
    }

    /**
     * 设置数据项昵称
     * 
     * @param nick 数据项昵称
     */
    public void setNick(String nick) {
        this.nick = nick;
    }

    /**
     * 获取数据项备注说明
     * 
     * @return 数据项备注说明
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置数据项备注说明
     * 
     * @param remark 数据项备注说明
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取数据项比较器
     * 
     * @return 数据项比较器
     */
    public DataComparatorStorage getComparator() {
        return comparator;
    }

    /**
     * 设置数据项比较器
     * 
     * @param comparator 数据项比较器
     */
    public void setComparator(DataComparatorStorage comparator) {
        this.comparator = comparator;
    }

    /**
     * 从DataItem对象中读取数据
     * 将DataItem对象的属性值复制到当前对象中
     * 
     * @param object 要读取的DataItem对象
     */
    @Override
    public void from(DataItem object) {
        this.code = object.getCode();
        this.unique = object.isUnique();
        this.nick = object.getNick();
        this.remark = object.getRemark();
        this.comparator = new DataComparatorStorage();
        this.comparator.setGroup(object.getComparatorGroup());
        this.comparator.setFriendlyName(object.getComparatorName());
        this.comparator.from(object.getComparator());
    }

    /**
     * 将当前对象转换为DataItem对象
     * 创建一个新的DataItem对象并将当前对象的属性值复制到新对象中
     * 
     * @return 转换后的DataItem对象
     */
    @Override
    public DataItem to() {
        DataItem item = new DataItem();
        item.setCode(this.code);
        item.setUnique(this.unique);
        item.setNick(this.nick);
        item.setRemark(this.remark);
        if (this.comparator != null) {
            item.setComparatorGroup(this.comparator.getGroup());
            item.setComparatorName(this.comparator.getFriendlyName());
            item.setComparator(this.comparator.to());
        }
        return item;
    }

}
