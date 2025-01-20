package com.tangyujun.datashadow.datacomparator;

import javafx.stage.Window;

/**
 * 数据项比较器
 */
public interface DataComparator {
    /**
     * 比较两个数据项
     * 
     * @param o1 数据项1
     * @param o2 数据项2
     * @return 是否相等
     */
    boolean equals(Object o1, Object o2);

    /**
     * 配置数据比较器
     * 
     * @param primaryStage 主舞台
     */
    void config(Window primaryStage);

    /**
     * 将数据比较器序列化为字符串
     * 
     * @return 包含数据比较器配置信息的字符串
     */
    String exportComparator();

    /**
     * 将字符串反序列化为数据比较器
     * 
     * @param exportValueString 包含数据比较器配置信息的字符串
     */
    void importComparator(String exportValueString);

    /**
     * 获取数据比较器的描述
     * 
     * @return 数据比较器的描述
     */
    String getDescription();
}
