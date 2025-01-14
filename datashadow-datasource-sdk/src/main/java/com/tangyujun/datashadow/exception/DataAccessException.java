package com.tangyujun.datashadow.exception;

/**
 * 数据访问异常类
 * 用于封装数据源访问过程中发生的异常
 * 继承自RuntimeException,属于非受检异常
 */
public class DataAccessException extends RuntimeException {

    /**
     * 构造数据访问异常
     * 
     * @param message 异常信息描述
     * @param cause   原始异常对象,保存异常调用链
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
