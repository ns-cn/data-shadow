package com.tangyujun.datashadow.storage;

/**
 * 可存储对象的抽象类
 * 用于将对象序列化为字符串存储，或从字符串反序列化为对象
 * 
 * @param <T> 存储对象的类型
 */
public abstract class Storable<T> {

    /**
     * 从对象反序列化
     * 
     * @param object 需要序列化的对象
     */
    public abstract void from(T object);

    /**
     * 将当前对象序列化为目标对象
     * 
     * @return 序列化后的目标对象
     */
    public abstract T to();
}
