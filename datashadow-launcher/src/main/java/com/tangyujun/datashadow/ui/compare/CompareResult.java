package com.tangyujun.datashadow.ui.compare;

import java.util.HashMap;
import java.util.Map;

/**
 * 比对结果类
 * 用于存储一行数据的所有字段比对结果
 * 包含每个字段的主数据源值、影子数据源值及其差异状态
 */
public class CompareResult {
    /**
     * 存储每个字段的比对结果
     * key为字段编码,value为该字段的比对结果
     */
    private final Map<String, CellResult> cellResults = new HashMap<>();

    /**
     * 添加一个字段的比对结果
     * 
     * @param code   字段编码
     * @param result 字段比对结果
     */
    public void putCellResult(String code, CellResult result) {
        cellResults.put(code, result);
    }

    /**
     * 获取指定字段的比对结果
     * 
     * @param code 字段编码
     * @return 字段比对结果,如果字段不存在则返回null
     */
    public CellResult getCellResult(String code) {
        return cellResults.get(code);
    }

    /**
     * 判断该行数据是否存在差异
     * 
     * @return 如果任一字段存在差异则返回true,否则返回false
     */
    public boolean hasDifferences() {
        return cellResults.values().stream().anyMatch(CellResult::isDifferent);
    }

    /**
     * 获取所有字段的比对结果
     * 
     * @return 包含所有字段比对结果的Map
     */
    public Map<String, CellResult> getCellResults() {
        return cellResults;
    }

    /**
     * 设置所有字段的比对结果
     * 
     * @param cellResults 包含所有字段比对结果的Map
     */
    public void setCellResults(Map<String, CellResult> cellResults) {
        this.cellResults.clear();
        if (cellResults != null) {
            this.cellResults.putAll(cellResults);
        }
    }
}
