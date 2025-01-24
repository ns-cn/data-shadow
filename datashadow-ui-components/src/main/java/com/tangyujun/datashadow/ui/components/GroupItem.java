package com.tangyujun.datashadow.ui.components;

/**
 * 分组项记录类
 * 用于存储分组项的值和所属分组信息
 * 
 * @param <T>   分组项值的类型参数
 * @param value 分组项的值
 * @param group 所属分组名称
 */
public record GroupItem<T>(T value, String group) {
}
