package com.tangyujun.datashadow.datasource;

/**
 * 数据源配置回调接口
 * 用于处理数据源配置过程中的各种状态回调
 * 包括配置成功、失败和取消三种状态的处理
 * 通过此接口可以实现对数据源配置过程的完整监控和处理
 * 
 * @author tangyujun
 */
public interface DataSourceConfigurationCallback {
    /**
     * 当数据源配置成功完成时调用
     * 此方法在数据源所有配置项均正确设置完成后被调用
     * 可以在此方法中执行配置完成后的后续操作，如更新UI、保存配置等
     */
    void onConfigureFinished();

    /**
     * 当数据源配置失败时调用
     * 此方法在配置过程中出现任何错误时被调用
     * 错误可能来自于：
     * - 必填配置项未填写
     * - 配置项格式错误
     * - 连接测试失败
     * - 权限验证失败等
     * 
     * @param errorMessage 错误信息，包含具体的错误原因描述，用于向用户展示或记录日志
     */
    void onConfigureFailed(String errorMessage);

    /**
     * 当数据源配置被取消时调用
     * 此方法在用户主动取消配置过程时被调用
     * 例如：
     * - 点击取消按钮
     * - 关闭配置窗口
     * - 切换到其他配置项等
     * 可以在此方法中执行清理操作，如清除临时数据、重置UI状态等
     */
    void onConfigureCancelled();
}
