package com.tangyujun.datashadow.exporter.defaults;

import com.tangyujun.datashadow.dataresult.CellResult;
import com.tangyujun.datashadow.dataresult.CompareResult;
import com.tangyujun.datashadow.dataresult.FilterModel;
import com.tangyujun.datashadow.dataresult.HeaderModel;
import com.tangyujun.datashadow.exporter.ResultExporter;
import com.tangyujun.datashadow.exporter.ResultExporterRegistry;
import com.tangyujun.datashadow.exporter.ExportDialogHelper;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Excel格式导出器
 * 使用Apache POI实现数据导出
 * 
 * @author tangyujun
 */
@ResultExporterRegistry(name = "Excel", group = "文件")
public class ExcelResultExporter implements ResultExporter {
    private static final Logger log = Logger.getLogger(ExcelResultExporter.class.getName());

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

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("对比结果");

            // 创建样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle diffStyle = createDiffStyle(workbook);
            CellStyle normalStyle = createNormalStyle(workbook);

            // 写入表头
            CompareResult firstRow = results.get(0);
            String[] headers = firstRow.getCellResults().keySet().toArray(String[]::new);
            XSSFRow headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                XSSFCell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            int rowNum = 1;
            for (CompareResult result : results) {
                XSSFRow row = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    XSSFCell cell = row.createCell(i);
                    CellResult cellResult = result.getCellResult(headers[i]);

                    // 设置单元格值
                    cell.setCellValue(cellResult.getDisplayValue());

                    // 设置样式
                    cell.setCellStyle(cellResult.isDifferent() ? diffStyle : normalStyle);
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            log.log(Level.INFO, "成功导出Excel文件到: {0}", file.getAbsolutePath());
            ExportDialogHelper.showSuccessDialog(
                    "导出成功",
                    "Excel文件已成功导出到：" + file.getAbsolutePath(),
                    file,
                    window);

        } catch (IOException e) {
            log.log(Level.SEVERE, "导出Excel文件失败: ", e);
            ExportDialogHelper.showErrorDialog(
                    "导出失败",
                    "导出Excel文件时发生错误",
                    e,
                    window);
            throw new RuntimeException("导出Excel文件时发生错误：" + e.getMessage());
        }
    }

    /**
     * 创建表头样式
     * 
     * @param workbook 工作簿
     * @return 表头样式
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 设置灰色背景
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        // 设置对齐方式
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        // 设置字体
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    /**
     * 创建差异样式
     * 
     * @param workbook 工作簿
     * @return 差异样式
     */
    private CellStyle createDiffStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 设置黄色背景
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // 设置边框
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        // 设置对齐方式
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        // 设置自动换行
        style.setWrapText(true);
        return style;
    }

    /**
     * 创建正常样式
     * 
     * @param workbook 工作簿
     * @return 正常样式
     */
    private CellStyle createNormalStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        // 设置边框
        style.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        style.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        // 设置对齐方式
        style.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        style.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        // 设置自动换行
        style.setWrapText(true);
        return style;
    }

    /**
     * 显示文件保存对话框
     * 
     * @param window 父窗口
     * @return 用户选择的文件
     */
    private File showSaveFileDialog(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存Excel文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        return fileChooser.showSaveDialog(window);
    }
}