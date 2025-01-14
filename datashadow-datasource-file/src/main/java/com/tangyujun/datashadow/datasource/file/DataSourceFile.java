package com.tangyujun.datashadow.datasource.file;

import com.tangyujun.datashadow.datasource.DataSource;
import java.util.Objects;

/**
 * 文件类型数据源的基类
 * 提供文件路径的基本属性和方法
 */
public abstract class DataSourceFile extends DataSource {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    protected String path;

    /**
     * 获取文件路径
     * 
     * @return 文件路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 设置文件路径
     * 
     * @param path 文件路径
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 判断两个对象是否相等
     * 
     * @param o 要比较的对象
     * @return 如果对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DataSourceFile that = (DataSourceFile) o;
        return Objects.equals(path, that.path);
    }

    /**
     * 计算对象的哈希值
     * 
     * @return 对象的哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }
}
