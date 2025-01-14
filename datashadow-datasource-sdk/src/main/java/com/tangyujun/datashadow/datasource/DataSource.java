package com.tangyujun.datashadow.datasource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 数据源
 */
@Getter
@Setter
@EqualsAndHashCode
public abstract class DataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据集的键值映射(key:数据项代码, value:数据项值)
     */
    private Map<String, String> keyToCode;

    /**
     * 数据处理脚本（JS）
     */
    private String scriptProcessor;

    /**
     * 数据源是否有效
     */
    public abstract void valid() throws DataSourceValidException;

    /**
     * 添加数据集的键值映射
     * 
     * @param key  数据项代码
     * @param code 数据项值
     */
    public void addKeyToCode(String key, String code) {
        if (keyToCode == null) {
            keyToCode = new HashMap<>();
        }
        keyToCode.put(key, code);
    }

    /**
     * 获取数据集
     * 
     * @return 数据集
     */
    public abstract List<Map<String, Object>> getValues() throws DataAccessException;
}
