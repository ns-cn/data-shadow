package com.tangyujun.datashadow.exporter.defaults;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.tangyujun.datashadow.dataresult.CellResult;
import com.tangyujun.datashadow.dataresult.CompareResult;
import com.tangyujun.datashadow.dataresult.FilterModel;
import com.tangyujun.datashadow.dataresult.HeaderModel;
import com.tangyujun.datashadow.exporter.ResultExporter;
import com.tangyujun.datashadow.exporter.ResultExporterRegistry;
import com.tangyujun.datashadow.exporter.ExportDialogHelper;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON格式导出器
 * 使用FastJson2实现数据导出
 * 
 * @author tangyujun
 */
@ResultExporterRegistry(name = "JSON", group = "文件")
public class JsonResultExporter implements ResultExporter {
    private static final Logger log = Logger.getLogger(JsonResultExporter.class.getName());

    @Override
    public void export(List<CompareResult> results, Window window, FilterModel filterModel,
            HeaderModel headerModel) {
        // 检查是否有数据需要导出
        if (results == null || results.isEmpty()) {
            throw new IllegalArgumentException("没有可导出的数据");
        }

        // 显示文件保存对话框，获取用户选择的文件
        File file = showSaveFileDialog(window);
        if (file == null) {
            return;
        }

        try {
            // 构建JSON数据结构
            List<JSONObject> jsonData = new ArrayList<>();

            for (CompareResult result : results) {
                JSONObject rowData = new JSONObject();

                // 遍历每个单元格结果
                for (String key : result.getCellResults().keySet()) {
                    CellResult cellResult = result.getCellResult(key);
                    JSONObject cellData = new JSONObject();

                    // 添加主数据源值
                    cellData.put("primaryValue", cellResult.getPrimaryValue());
                    // 添加影子数据源值
                    cellData.put("shadowValue", cellResult.getShadowValue());
                    // 添加是否存在差异标记
                    cellData.put("isDifferent", cellResult.isDifferent());
                    // 添加显示值
                    cellData.put("displayValue", cellResult.getDisplayValue());

                    rowData.put(key, cellData);
                }

                jsonData.add(rowData);
            }

            // 写入文件
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(JSON.toJSONString(jsonData));
            }
            log.log(Level.INFO, "成功导出JSON文件到: {0}", file.getAbsolutePath());
            // 显示成功对话框
            ExportDialogHelper.showSuccessDialog(
                    "导出成功",
                    "JSON文件已成功导出到：" + file.getAbsolutePath(),
                    file,
                    window);

        } catch (IOException e) {
            log.log(Level.SEVERE, "导出JSON文件失败: ", e);
            ExportDialogHelper.showErrorDialog(
                    "导出失败",
                    "导出JSON文件时发生错误",
                    e,
                    window);
            throw new RuntimeException("导出JSON文件时发生错误：" + e.getMessage());
        }
    }

    /**
     * 显示文件保存对话框
     * 
     * @param window 父窗口
     * @return 用户选择的文件
     */
    private File showSaveFileDialog(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存JSON文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        return fileChooser.showSaveDialog(window);
    }
}