package com.tangyujun.datashadow.dataitem;

import com.tangyujun.datashadow.datacomparator.DataComparator;

/**
 * 数据项类
 * 用于定义数据源中的数据项属性
 */
public class DataItem {
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
     * 比较器组,用于标识数据项的比较器组
     */
    private String comparatorGroup;

    /**
     * 比较器名称,用于标识数据项的比较器
     */
    private String comparatorName;

    /**
     * 比较器,用于定义数据项的比较规则
     */
    private DataComparator comparator;

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
     * 判断数据项是否唯一
     * 
     * @return 是否唯一
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * 设置数据项是否唯一
     * 
     * @param unique 是否唯一
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
     * 获取数据项备注
     * 
     * @return 数据项备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置数据项备注
     * 
     * @param remark 数据项备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取数据项比较器组
     * 
     * @return 数据项比较器组
     */
    public String getComparatorGroup() {
        return comparatorGroup;
    }

    /**
     * 设置数据项比较器组
     * 
     * @param comparatorGroup 数据项比较器组
     */
    public void setComparatorGroup(String comparatorGroup) {
        this.comparatorGroup = comparatorGroup;
    }

    /**
     * 获取数据项比较器名称
     * 
     * @return 数据项比较器名称
     */
    public String getComparatorName() {
        return comparatorName;
    }

    /**
     * 设置数据项比较器名称
     * 
     * @param comparatorName 数据项比较器名称
     */
    public void setComparatorName(String comparatorName) {
        this.comparatorName = comparatorName;
    }

    /**
     * 获取数据项比较器
     * 
     * @return 数据项比较器
     */
    public DataComparator getComparator() {
        return comparator;
    }

    /**
     * 设置数据项比较器
     * 
     * @param comparator 数据项比较器
     */
    public void setComparator(DataComparator comparator) {
        this.comparator = comparator;
    }

    /**
     * 获取数据项显示名称
     * 如果设置了昵称则返回"编码(昵称)"格式
     * 否则直接返回编码
     * 
     * @return 数据项显示名称
     */
    public String getDisplayName() {
        if (this.nick != null && !this.nick.isEmpty()) {
            return String.format("%s(%s)", this.code, this.nick);
        }
        return this.code;
    }
}
