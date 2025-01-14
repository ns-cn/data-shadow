package com.tangyujun.datashadow.datasource;

import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

/**
 * 内存数据源
 */
public class DataSourceMemory extends DataSource {

    /**
     * 数据集
     */
    private List<Map<String, Object>> data;

    /**
     * 数据源是否有效
     */
    @Override
    public void valid() throws DataSourceValidException {
    }

    /**
     * 获取数据集
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        return data;
    }
}
