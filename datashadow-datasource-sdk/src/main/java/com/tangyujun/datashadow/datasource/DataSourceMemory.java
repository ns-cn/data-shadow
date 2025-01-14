package com.tangyujun.datashadow.datasource;

import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

/**
 * 内存数据源
 * 用于存储和管理内存中的数据集
 * 主要用于临时数据的存储和处理
 */
public class DataSourceMemory extends DataSource {

    /**
     * 数据集
     * 使用List<Map<String, Object>>存储数据
     * 其中List中的每个Map代表一行数据
     * Map的key为字段名，value为字段值
     */
    private List<Map<String, Object>> data;

    /**
     * 验证数据源是否有效
     * 由于是内存数据源，不需要特殊的验证逻辑
     * 
     * @throws DataSourceValidException 当数据源验证失败时抛出此异常
     */
    @Override
    public void valid() throws DataSourceValidException {
    }

    /**
     * 获取数据集
     * 直接返回内存中存储的数据集
     * 
     * @return 返回内存中存储的数据集
     * @throws DataAccessException 当数据访问出错时抛出此异常
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        return data;
    }
}
