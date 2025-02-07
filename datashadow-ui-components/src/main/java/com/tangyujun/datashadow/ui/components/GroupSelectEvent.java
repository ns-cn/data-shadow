package com.tangyujun.datashadow.ui.components;

/**
 * 分组选择事件记录类
 * 用于在选择发生时传递选择的分组、名称和值信息
 * 
 * @param <T>   选项值的类型参数，表示选中值的类型
 * @param group 选中的分组名称
 * @param name  选中的选项名称
 * @param value 选中的选项值
 */
public record GroupSelectEvent<T>(String group, String name, T value) {
    @Override
    public String toString() {
        return String.format("GroupSelectEvent{group='%s', name='%s', value=%s}", group, name, value);
    }
}
