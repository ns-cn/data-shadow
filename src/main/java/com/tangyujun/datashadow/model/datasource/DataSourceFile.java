package com.tangyujun.datashadow.model.datasource;

/**
 * 文件数据源
 */
public abstract class DataSourceFile extends DataSource {

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
