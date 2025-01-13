package com.tangyujun.datashadow.model.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据库数据源
 */
@Getter
@Setter
public abstract class DataSourceDb extends DataSource {

    /**
     * 数据库连接URL
     */
    protected String url;

    /**
     * 数据库用户名
     */
    protected String username;

    /**
     * 数据库密码
     */
    protected String password;

    /**
     * 查询SQL
     */
    protected String sql;
}
