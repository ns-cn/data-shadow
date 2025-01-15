package com.tangyujun.datashadow.datasource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.stage.Window;

/**
 * 数据源抽象类
 * 定义了数据源的基本属性和方法
 * 包括数据项映射、数据处理脚本、数据源验证和数据获取等功能
 * 
 * 数据源是程序中最基本的数据来源,可以是文件(Excel、CSV等)或数据库(MySQL、Oracle等)
 * 每个具体的数据源实现类都需要实现该抽象类定义的基本功能
 */
public abstract class DataSource implements Serializable {

    /**
     * 序列化版本号
     * 用于对象序列化时的版本控制
     */
    private static final long serialVersionUID = 1L;

    /**
     * 数据源字段与数据项的映射关系(key:数据项代码, value:数据源字段名)
     * 用于将统一的数据项代码映射到数据源中的字段
     * 
     * 例如:
     * 数据项代码"NAME"可以映射到:
     * - 数据源A中的"姓名"字段
     * - 数据源B中的"name"字段
     */
    private Map<String, String> mappings;

    /**
     * 数据处理脚本（JS）
     * 用于在数据获取后进行自定义处理
     * 
     * 脚本可以对获取的数据进行转换、过滤、计算等操作
     * 在数据比对之前执行,确保数据符合比对要求
     */
    private String scriptProcessor;

    /**
     * 验证数据源是否有效
     * 子类需要实现具体的验证逻辑
     * 
     * 验证内容可能包括:
     * - 文件是否存在且可读
     * - 数据库连接是否可用
     * - 数据格式是否正确
     * - 必要的字段是否存在
     * 
     * @throws DataSourceValidException 当数据源验证失败时抛出此异常
     */
    public abstract void valid() throws DataSourceValidException;

    /**
     * 获取数据集
     * 子类需要实现具体的数据获取逻辑
     * 
     * 实现类需要:
     * 1. 从数据源读取原始数据
     * 2. 将数据转换为统一的Map格式
     * 3. 处理可能的数据类型转换
     * 4. 执行数据处理脚本(如果存在)
     * 
     * @return 包含数据的List，每个Map代表一行数据，key为字段名，value为字段值
     * @throws DataAccessException 当数据访问出错时抛出此异常
     */
    public abstract List<Map<String, Object>> getValues() throws DataAccessException;

    /**
     * 获取数据集的列名
     * 
     * 返回的列名应该是数据源原始的字段名称
     * 用于:
     * 1. 显示数据源结构
     * 2. 配置字段映射关系
     * 3. 数据读取时的字段定位
     * 
     * @return 列名列表
     */
    public abstract List<String> getColumns();

    /**
     * 获取已配置数据源的简单描述
     * 用于在界面上显示数据源的基本信息
     * 
     * 描述应该简明扼要,包含数据源的关键信息
     * 例如:
     * - mysql:127.0.0.1:3306/test
     * - D:/test.xlsx
     * - /data/input/data.csv
     * 
     * @return 数据源的简单描述字符串
     */
    public abstract String getDescription();

    /**
     * 添加字段映射关系
     * 用于建立统一数据项代码与数据源字段的对应关系
     * 
     * 映射关系用于:
     * 1. 统一不同数据源的字段名称
     * 2. 确保数据比对时字段的对应关系正确
     * 3. 支持多语言或不同命名习惯的数据源
     * 
     * @param itemCode    数据项代码
     * @param sourceField 数据源中的字段名
     */
    public void addMapping(String itemCode, String sourceField) {
        if (mappings == null) {
            mappings = new HashMap<>();
        }
        mappings.put(itemCode, sourceField);
    }

    /**
     * 获取字段映射关系
     * 
     * 返回的Map中:
     * - key: 数据源原始字段名
     * - value: 映射后的数据项代码
     * 
     * @return 字段映射关系Map
     */
    public Map<String, String> getMappings() {
        return mappings;
    }

    /**
     * 设置字段映射关系
     * 
     * 用于批量设置或更新字段映射
     * 会完全替换现有的映射关系
     * 
     * @param mappings 字段映射关系Map
     */
    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * 获取数据处理脚本
     * 
     * 脚本用于在数据获取后进行处理
     * 可以实现:
     * - 数据清洗
     * - 格式转换
     * - 数据计算
     * - 自定义处理逻辑
     * 
     * @return 数据处理脚本
     */
    public String getScriptProcessor() {
        return scriptProcessor;
    }

    /**
     * 设置数据处理脚本
     * 
     * 设置新的处理脚本会替换现有脚本
     * 脚本应该是合法的JavaScript代码
     * 
     * @param scriptProcessor 数据处理脚本
     */
    public void setScriptProcessor(String scriptProcessor) {
        this.scriptProcessor = scriptProcessor;
    }

    /**
     * 判断两个数据源对象是否相等
     * 
     * 比较内容包括:
     * 1. 字段映射关系
     * 2. 数据处理脚本
     * 
     * 注意:不同类型的数据源即使配置相同也被视为不相等
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
        return Objects.equals(mappings, that.mappings) &&
                Objects.equals(scriptProcessor, that.scriptProcessor);
    }

    /**
     * 计算对象的哈希码
     * 
     * 哈希码计算基于:
     * 1. 字段映射关系
     * 2. 数据处理脚本
     * 
     * 用于:
     * - Map中的键
     * - 对象比较
     * - 缓存索引
     * 
     * @return 对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(mappings, scriptProcessor);
    }

    /**
     * 配置数据源的具体行为，例如打开对话框、选择文件、配置数据库链接信息等
     * 
     * 实现类需要:
     * 1. 提供合适的配置界面
     * 2. 收集必要的配置信息
     * 3. 验证配置的有效性
     * 4. 通过回调通知配置结果
     * 
     * @param primaryStage 主窗口,用于显示配置界面
     * @param callback     配置完成后的回调函数,用于通知配置结果
     */
    public abstract void configure(Window primaryStage, DataSourceConfigurationCallback callback);

    /**
     * 获取数据源字段与数据项的映射关系
     * 
     * @param code 数据项代码
     * @return 数据源字段名
     */
    public String getMappedField(String code) {
        if (mappings == null) {
            return null;
        }
        return mappings.get(code);
    }

    /**
     * 清除所有映射关系
     * 如果mappings为null则不执行任何操作
     */
    public void clearMappings() {
        if (mappings != null) {
            mappings.clear();
        }
    }
}
