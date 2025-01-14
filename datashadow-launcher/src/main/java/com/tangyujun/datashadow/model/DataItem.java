package com.tangyujun.datashadow.model;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据项
 */
@Getter
@Setter
@EqualsAndHashCode
public class DataItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据项代码
     */
    private String code;

    /**
     * 是否逻辑唯一的数据字段（多个数据字段则表示联合主键）
     */
    private boolean unique;

    /**
     * 数据项名称（中文，可选，默认空）
     */
    private String nick;

    /**
     * 备注
     */
    private String remark;

    /**
     * 自定义比较器（JS），默认使用toString再比较
     */
    private String comparator;
}
