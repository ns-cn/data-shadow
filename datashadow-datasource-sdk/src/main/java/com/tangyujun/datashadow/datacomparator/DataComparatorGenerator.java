package com.tangyujun.datashadow.datacomparator;

/**
 * 数据比较器生成器
 */
@FunctionalInterface
public interface DataComparatorGenerator {
    /**
     * 生成数据比较器
     * 
     * @param primaryStage 主窗口,用于显示配置界面
     * @return 数据比较器
     */
    DataComparator generate();
}
