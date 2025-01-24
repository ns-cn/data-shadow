package com.tangyujun.datashadow.ui.components;

/**
 * 分组选择事件
 * 用于处理分组选择控件的选择事件
 * 
 * @param group 分组名称
 * @param name  选项名称
 * @param value 选项值
 */
public record GroupSelectEvent<T>(String group, String name, T value) {
    @Override
    public String toString() {
        return String.format("GroupSelectEvent{group='%s', name='%s', value=%s}", group, name, value);
    }
}
