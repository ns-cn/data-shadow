package com.tangyujun.datashadow.datacomparator;

/**
 * 数据比较器生成器
 * 用于生成和配置数据比较器实例
 * 
 * @author tangyujun
 */
@FunctionalInterface
public interface DataComparatorGenerator {
    /**
     * 生成数据比较器
     * 
     * @return 数据比较器
     */
    DataComparator generate();
}
