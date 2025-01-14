package com.tangyujun.datashadow.exception;

/**
 * 数据源验证异常类
 * 用于封装数据源验证过程中发生的异常
 * 继承自RuntimeException,属于非受检异常
 * 
 * 主要用于以下场景:
 * 1. 数据源配置参数无效
 * 2. 数据源连接失败
 * 3. 数据源权限验证失败
 * 4. 数据源结构不符合要求
 * 5. 其他与数据源验证相关的异常情况
 */
public class DataSourceValidException extends RuntimeException {

    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 构造数据源验证异常
     * 
     * @param message 异常信息描述,用于说明验证失败的具体原因
     * @param cause   原始异常对象,保存异常调用链,便于异常追踪和调试
     */
    public DataSourceValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
