package com.tangyujun.datashadow.dataresult;

/**
 * 表头显示模式枚举类
 * 定义了不同的表头显示模式及其对应的显示名称
 */
public enum HeaderModel {
    CODE("数据项名称"),
    NICK("数据项别名优先");

    private final String displayName;

    /**
     * 构造函数
     * 
     * @param displayName 表头显示模式的显示名称
     */
    HeaderModel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取表头显示模式的显示名称
     * 
     * @return 表头显示模式的显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
}
