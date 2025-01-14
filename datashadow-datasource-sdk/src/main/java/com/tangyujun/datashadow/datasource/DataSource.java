package com.tangyujun.datashadow.datasource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Stage;

/**
 * 数据源抽象类
 * 定义了数据源的基本属性和方法
 * 包括数据项映射、数据处理脚本、数据源验证和数据获取等功能
 */
public abstract class DataSource implements Serializable {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 数据集的键值映射(key:数据项代码, value:数据项值)
     * 用于将数据源中的字段映射到统一的数据项代码
     */
    private Map<String, String> keyToCode;

    /**
     * 数据处理脚本（JS）
     * 用于在数据获取后进行自定义处理
     */
    private String scriptProcessor;

    /**
     * 验证数据源是否有效
     * 子类需要实现具体的验证逻辑
     * 
     * @throws DataSourceValidException 当数据源验证失败时抛出此异常
     */
    public abstract void valid() throws DataSourceValidException;

    /**
     * 获取数据集
     * 子类需要实现具体的数据获取逻辑
     * 
     * @return 包含数据的List，每个Map代表一行数据，key为字段名，value为字段值
     * @throws DataAccessException 当数据访问出错时抛出此异常
     */
    public abstract List<Map<String, Object>> getValues() throws DataAccessException;

    /**
     * 获取数据集的列名
     * 
     * @return 列名列表
     */
    public abstract List<String> getColumns();

    /**
     * 获取已配置数据源的简单描述
     * 用于在界面上显示数据源的基本信息
     * 例如: mysql:127.0.0.1:3306/test 或 D:/test.xlsx
     * 
     * @return 数据源的简单描述字符串
     */
    public abstract String getDescription();

    /**
     * 添加数据集的键值映射
     * 用于建立数据源字段与统一数据项代码的对应关系
     * 
     * @param key  数据源中的字段名
     * @param code 对应的数据项代码
     */
    public void addKeyToCode(String key, String code) {
        if (keyToCode == null) {
            keyToCode = new HashMap<>();
        }
        keyToCode.put(key, code);
    }

    /**
     * 获取数据集的键值映射
     * 
     * @return 键值映射Map
     */
    public Map<String, String> getKeyToCode() {
        return keyToCode;
    }

    /**
     * 设置数据集的键值映射
     * 
     * @param keyToCode 键值映射Map
     */
    public void setKeyToCode(Map<String, String> keyToCode) {
        this.keyToCode = keyToCode;
    }

    /**
     * 获取数据处理脚本
     * 
     * @return 数据处理脚本
     */
    public String getScriptProcessor() {
        return scriptProcessor;
    }

    /**
     * 设置数据处理脚本
     * 
     * @param scriptProcessor 数据处理脚本
     */
    public void setScriptProcessor(String scriptProcessor) {
        this.scriptProcessor = scriptProcessor;
    }

    /**
     * 判断两个数据源对象是否相等
     * 
     * @param o 要比较的对象
     * @return 如果两个对象相等返回true，否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DataSource that = (DataSource) o;
        return Objects.equals(keyToCode, that.keyToCode) &&
                Objects.equals(scriptProcessor, that.scriptProcessor);
    }

    /**
     * 计算对象的哈希码
     * 
     * @return 对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(keyToCode, scriptProcessor);
    }

    /**
     * 配置数据源的具体行为，例如打开对话框、选择文件、配置数据库链接信息等
     * 
     * @param primaryStage 主窗口
     */
    public abstract void configure(Stage primaryStage);
}
