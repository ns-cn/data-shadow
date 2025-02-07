package com.tangyujun.datashadow.ui.compare.model;

import java.io.File;

/**
 * 导出结果回调接口
 * 用于处理导出成功和失败的回调
 * 
 * 回调方法:
 * 1. onExportSuccess: 导出成功时调用
 * 2. onExportError: 导出失败时调用
 */
public interface ResultExportCallback {
    /**
     * 导出成功回调
     * 当导出操作成功完成时调用
     * 
     * @param message 成功提示消息
     * @param file    导出的文件对象
     */
    void onExportSuccess(String message, File file);

    /**
     * 导出失败回调
     * 当导出操作失败时调用
     * 
     * @param message 错误提示消息
     */
    void onExportError(String message);
}