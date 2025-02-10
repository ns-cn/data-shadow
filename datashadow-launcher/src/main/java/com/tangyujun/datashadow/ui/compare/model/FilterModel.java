package com.tangyujun.datashadow.ui.compare.model;

/**
 * 过滤模式枚举类
 * 定义了不同的过滤模式及其对应的显示名称
 */
public enum FilterModel {
    ALL("全部数据"),
    ALL_DIFF("所有差异项"),
    PRIMARY("仅主数据源"),
    PRIMARY_DIFF("仅主数据源差异项"),
    SHADOW("仅影子数据源"),
    SHADOW_DIFF("仅影子数据源差异项");

    private final String displayName;

    /**
     * 构造函数
     * 
     * @param displayName 过滤模式的显示名称
     */
    FilterModel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取过滤模式的显示名称
     * 
     * @return 过滤模式的显示名称
     */
    public String getDisplayName() {
        return displayName;
    }
}
