package com.tangyujun.datashadow.dataitem;

import java.io.Serializable;
import java.util.Objects;

/**
 * 数据项类
 * 用于定义数据源中的数据项属性
 * 包括数据项的代码、唯一性、别名、备注和比较器等信息
 */
public class DataItem implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 数据项代码
     * 用于唯一标识一个数据项
     */
    private String code;

    /**
     * 是否唯一
     * 标识该数据项的值在数据集中是否具有唯一性
     */
    private boolean unique;

    /**
     * 数据项别名
     * 用于显示的友好名称
     */
    private String nick;

    /**
     * 数据项备注
     * 用于描述数据项的详细信息
     */
    private String remark;

    /**
     * 比较器
     * 用于定义该数据项的比较规则
     */
    private String comparator;

    /**
     * 获取数据项代码
     * 
     * @return 数据项代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 设置数据项代码
     * 
     * @param code 数据项代码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 判断数据项是否唯一
     * 
     * @return 如果唯一返回true，否则返回false
     */
    public boolean isUnique() {
        return unique;
    }

    /**
     * 设置数据项的唯一性
     * 
     * @param unique 是否唯一
     */
    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    /**
     * 获取数据项别名
     * 
     * @return 数据项别名
     */
    public String getNick() {
        return nick;
    }

    /**
     * 设置数据项别名
     * 
     * @param nick 数据项别名
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
     * 获取数据项比较器
     * 
     * @return 数据项比较器
     */
    public String getComparator() {
        return comparator;
    }

    /**
     * 设置数据项比较器
     * 
     * @param comparator 数据项比较器
     */
    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    /**
     * 判断两个数据项对象是否相等
     * 
     * @param o 要比较的对象
     * @return 如果两个对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DataItem dataItem = (DataItem) o;
        return unique == dataItem.unique &&
                Objects.equals(code, dataItem.code) &&
                Objects.equals(nick, dataItem.nick) &&
                Objects.equals(remark, dataItem.remark) &&
                Objects.equals(comparator, dataItem.comparator);
    }

    /**
     * 计算对象的哈希码
     * 
     * @return 对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(code, unique, nick, remark, comparator);
    }
}
