package com.tangyujun.datashadow.model.datasource;

import lombok.EqualsAndHashCode;

/**
 * 文件数据源
 */
@EqualsAndHashCode(callSuper = true)
public abstract class DataSourceFile extends DataSource {
    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    protected String path;

    /**
     * 设置文件路径
     * 
     * @param path 文件路径
     */
    public void setPath(String path) {
        this.path = path;
    }
}
