package com.tangyujun.datashadow.exception;

/**
 * 数据访问异常类
 * 用于封装数据源访问过程中发生的异常
 * 继承自RuntimeException,属于非受检异常
 * 
 * 主要用于以下场景:
 * 1. 数据源连接失败
 * 2. 数据读取过程中发生IO异常
 * 3. 数据格式不符合预期
 * 4. 数据转换失败
 * 5. 其他与数据访问相关的异常情况
 */
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 构造数据访问异常
     * 
     * @param message 异常信息描述,用于说明异常的具体原因
     * @param cause   原始异常对象,保存异常调用链,便于异常追踪和调试
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
