package com.tangyujun.datashadow.datasource.db;

import com.tangyujun.datashadow.datasource.DataSource;

public abstract class DataSourceDb extends DataSource {
    private static final long serialVersionUID = 1L;

    protected String url;
    protected String username;
    protected String password;
    protected String sql;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
