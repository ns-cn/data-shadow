package com.tangyujun.datashadow.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import com.tangyujun.datashadow.datasource.DataSource;

/**
 * 数据对比方案
 */
public class ShadowSchemaManager implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据对比结果的列
     */
    private List<DataItem> dataItems;

    /**
     * 主数据源
     */
    private DataSource primarySource;

    /**
     * 影子数据源
     */
    private DataSource shadowSource;

    /**
     * 获取数据对比结果的列
     * 
     * @return 数据对比结果的列
     */
    public List<DataItem> getDataItems() {
        return dataItems;
    }

    /**
     * 设置数据对比结果的列
     * 
     * @param dataItems 数据对比结果的列
     */
    public void setDataItems(List<DataItem> dataItems) {
        this.dataItems = dataItems;
    }

    /**
     * 获取主数据源
     * 
     * @return 主数据源
     */
    public DataSource getPrimarySource() {
        return primarySource;
    }

    /**
     * 设置主数据源
     * 
     * @param primarySource 主数据源
     */
    public void setPrimarySource(DataSource primarySource) {
        this.primarySource = primarySource;
    }

    /**
     * 获取影子数据源
     * 
     * @return 影子数据源
     */
    public DataSource getShadowSource() {
        return shadowSource;
    }

    /**
     * 设置影子数据源
     * 
     * @param shadowSource 影子数据源
     */
    public void setShadowSource(DataSource shadowSource) {
        this.shadowSource = shadowSource;
    }

    public void export(String path) throws IOException {
        // 使用ObjectOutputStream将当前对象序列化并写入文件
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new IOException("导出数据对比方案失败", e);
        }
    }

    /**
     * 从文件导入数据对比方案
     * 
     * @param path 文件路径
     * @throws IOException            如果发生IO错误
     * @throws ClassNotFoundException 如果找不到相关类
     */
    public void importFrom(String path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            ShadowSchemaManager schema = (ShadowSchemaManager) ois.readObject();
            this.setDataItems(schema.getDataItems());
            this.setPrimarySource(schema.getPrimarySource());
            this.setShadowSource(schema.getShadowSource());
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        }
    }
}
