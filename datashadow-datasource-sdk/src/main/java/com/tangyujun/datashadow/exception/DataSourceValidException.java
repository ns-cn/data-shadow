package com.tangyujun.datashadow.exception;

/**
 * 数据源验证异常
 * 当数据源验证失败时抛出
 */
public class DataSourceValidException extends RuntimeException {

    /**
     * 构造函数
     * 
     * @param message 错误信息
     * @param cause   原始异常
     */
    public DataSourceValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
