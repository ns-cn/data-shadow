package com.tangyujun.datashadow.exporter.defaults;

import com.tangyujun.datashadow.dataresult.CellResult;
import com.tangyujun.datashadow.dataresult.CompareResult;
import com.tangyujun.datashadow.dataresult.FilterModel;
import com.tangyujun.datashadow.dataresult.HeaderModel;
import com.tangyujun.datashadow.exporter.ResultExporter;
import com.tangyujun.datashadow.exporter.ResultExporterRegistry;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.tangyujun.datashadow.exporter.ExportDialogHelper;

/**
 * CSV格式导出器
 * 使用Apache Commons CSV实现数据导出
 * 
 * @author tangyujun
 */
@ResultExporterRegistry(name = "CSV", group = "文件")
public class CsvResultExporter implements ResultExporter {
    private static final Logger log = Logger.getLogger(CsvResultExporter.class.getName());

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

        FileWriter fileWriter = null;
        CSVPrinter printer = null;
        try {
            fileWriter = new FileWriter(file);
            printer = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);

            // 写入表头
            String[] headers = getHeaders(results);
            printer.printRecord((Object[]) headers);

            // 写入数据行
            for (CompareResult row : results) {
                String[] rowData = getRowData(row, headers);
                printer.printRecord((Object[]) rowData);
            }

            // 刷新并关闭资源
            printer.flush();
            printer.close();
            fileWriter.close();

            log.log(Level.INFO, "成功导出CSV文件到: {0}", file.getAbsolutePath());

            // 显示成功对话框
            ExportDialogHelper.showSuccessDialog(
                    "导出成功",
                    "CSV文件已成功导出到：" + file.getAbsolutePath(),
                    file,
                    window);

        } catch (IOException e) {
            log.log(Level.SEVERE, "导出CSV文件失败: ", e);
            ExportDialogHelper.showErrorDialog(
                    "导出失败",
                    "导出CSV文件时发生错误",
                    e,
                    window);
            throw new RuntimeException("导出CSV文件时发生错误：" + e.getMessage());
        } finally {
            // 确保资源被关闭
            try {
                if (printer != null) {
                    printer.close();
                }
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "关闭资源时发生错误", e);
            }
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
        fileChooser.setTitle("保存CSV文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        return fileChooser.showSaveDialog(window);
    }

    /**
     * 获取表头数组
     * 
     * @param results 数据结果列表
     * @return 表头数组
     */
    private String[] getHeaders(List<CompareResult> results) {
        CompareResult firstRow = results.get(0);
        return firstRow.getCellResults().keySet().toArray(String[]::new);
    }

    /**
     * 获取行数据
     * 
     * @param row     数据行
     * @param headers 表头数组
     * @return 行数据数组
     */
    private String[] getRowData(CompareResult row, String[] headers) {
        String[] rowData = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            CellResult cellResult = row.getCellResult(headers[i]);
            rowData[i] = cellResult.getDisplayValue();
        }
        return rowData;
    }
}
