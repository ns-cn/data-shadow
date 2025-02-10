package com.tangyujun.datashadow.exporter;

import java.util.List;

import com.tangyujun.datashadow.dataresult.CompareResult;
import com.tangyujun.datashadow.dataresult.FilterModel;
import com.tangyujun.datashadow.dataresult.HeaderModel;

import javafx.stage.Window;

/**
 * 结果导出器接口
 * 定义了导出对比结果的方法
 */
@FunctionalInterface
public interface ResultExporter {

    /**
     * 导出对比结果
     * 
     * @param results     对比结果列表
     * @param window      窗口实例
     * @param filterModel 过滤模式
     * @param headerModel 表头显示模式
     */
    void export(List<CompareResult> results,
            Window window, FilterModel filterModel, HeaderModel headerModel);
}
