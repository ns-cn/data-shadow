package com.tangyujun.datashadow.ui.compare;

/**
 * 单元格比对结果类
 * 用于存储单个单元格的主数据源值和影子数据源值,以及是否存在差异
 * 包含:
 * 1. 主数据源的值
 * 2. 影子数据源的值
 * 3. 是否存在差异的标志
 */
public class CellResult {
    /**
     * 主数据源的值
     */
    private Object primaryValue;

    /**
     * 影子数据源的值
     */
    private Object shadowValue;

    /**
     * 是否存在差异
     */
    private boolean isDifferent;

    /**
     * 获取主数据源的值
     * 
     * @return 主数据源的值
     */
    public Object getPrimaryValue() {
        return primaryValue;
    }

    /**
     * 设置主数据源的值
     * 
     * @param primaryValue 主数据源的值
     */
    public void setPrimaryValue(Object primaryValue) {
        this.primaryValue = primaryValue;
    }

    /**
     * 获取影子数据源的值
     * 
     * @return 影子数据源的值
     */
    public Object getShadowValue() {
        return shadowValue;
    }

    /**
     * 设置影子数据源的值
     * 
     * @param shadowValue 影子数据源的值
     */
    public void setShadowValue(Object shadowValue) {
        this.shadowValue = shadowValue;
    }

    /**
     * 判断是否存在差异
     * 
     * @return 如果存在差异返回true,否则返回false
     */
    public boolean isDifferent() {
        return isDifferent;
    }

    /**
     * 设置是否存在差异
     * 
     * @param different 是否存在差异
     */
    public void setDifferent(boolean different) {
        isDifferent = different;
    }
}