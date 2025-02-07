package com.tangyujun.datashadow.ui.compare;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.opencsv.CSVWriter;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 对比结果导出工具类
 * 支持将对比结果导出为CSV、Excel和JSON格式
 * 
 * @author tangyujun
 * @since 2024-01-01
 */
public class CompareResultExporter {
    private static final Logger log = Logger.getLogger(CompareResultExporter.class.getName());

    private final TableView<CompareResult> resultTable;
    private final Window parentWindow;
    private final ResultExportCallback callback;

    /**
     * 构造函数
     * 
     * @param resultTable 包含对比结果的表格视图
     * @param parentWindow 父窗口，用于显示文件选择对话框
     * @param callback 导出结果回调接口
     */
    public CompareResultExporter(TableView<CompareResult> resultTable, Window parentWindow,
            ResultExportCallback callback) {
        this.resultTable = resultTable;
        this.parentWindow = parentWindow;
        this.callback = callback;
    }

    /**
     * 导出为CSV文件
     * 将对比结果表格数据导出为CSV格式，包含表头和数据行
     * 对于有差异的单元格，使用"❌"分隔主数据源和影子数据源的值
     */
    public void exportToCsv() {
        File file = showSaveFileDialog("CSV Files", "*.csv");
        if (file == null)
            return;

        try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
            ObservableList<CompareResult> data = resultTable.getItems();
            if (!validateData(data))
                return;

            // 写入表头
            String[] headers = resultTable.getColumns().stream()
                    .map(TableColumn::getText)
                    .toArray(String[]::new);
            writer.writeNext(headers);

            // 写入数据行
            for (CompareResult row : data) {
                String[] rowData = resultTable.getColumns().stream()
                        .map(column -> {
                            CellResult cellResult = row.getCellResult(column.getId());
                            return getCellDisplayValue(cellResult);
                        })
                        .toArray(String[]::new);
                writer.writeNext(rowData);
            }
            log.log(Level.INFO, "成功导出CSV文件到: {0}", file.getAbsolutePath());
            callback.onExportSuccess("CSV文件已成功导出到：" + file.getAbsolutePath(), file);
        } catch (IOException e) {
            handleExportError("CSV", e);
        }
    }

    /**
     * 导出为Excel文件
     * 将对比结果表格数据导出为Excel格式，包含以下特性：
     * - 表头使用灰色背景
     * - 差异数据使用黄色背景标注
     * - 自动调整列宽
     * - 对于有差异的单元格，使用"❌"分隔主数据源和影子数据源的值
     */
    public void exportToExcel() {
        File file = showSaveFileDialog("Excel Files", "*.xlsx");
        if (file == null)
            return;

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ObservableList<CompareResult> data = resultTable.getItems();
            if (!validateData(data))
                return;

            XSSFSheet sheet = workbook.createSheet("对比结果");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle diffStyle = createDiffStyle(workbook);

            writeExcelHeader(sheet, headerStyle);
            writeExcelData(sheet, diffStyle);

            // 自动调整列宽
            for (int i = 0; i < resultTable.getColumns().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }

            log.log(Level.INFO, "成功导出Excel文件到: {0}", file.getAbsolutePath());
            callback.onExportSuccess("Excel文件已成功导出到：" + file.getAbsolutePath(), file);

        } catch (IOException e) {
            handleExportError("Excel", e);
        }
    }

    /**
     * 导出为JSON文件
     * 将对比结果表格数据导出为JSON格式，每个单元格包含以下信息：
     * - primaryValue: 主数据源的值
     * - shadowValue: 影子数据源的值
     * - isDifferent: 是否存在差异
     */
    public void exportToJson() {
        File file = showSaveFileDialog("JSON Files", "*.json");
        if (file == null)
            return;

        try {
            ObservableList<CompareResult> data = resultTable.getItems();
            if (!validateData(data))
                return;

            List<JSONObject> jsonRows = new ArrayList<>();
            for (CompareResult row : data) {
                JSONObject jsonRow = new JSONObject();
                for (TableColumn<CompareResult, ?> column : resultTable.getColumns()) {
                    CellResult cellResult = row.getCellResult(column.getId());
                    if (cellResult != null) {
                        JSONObject cellData = new JSONObject();
                        cellData.put("primaryValue", cellResult.getPrimaryValue());
                        cellData.put("shadowValue", cellResult.getShadowValue());
                        cellData.put("isDifferent", cellResult.isDifferent());
                        jsonRow.put(column.getText(), cellData);
                    }
                }
                jsonRows.add(jsonRow);
            }

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(JSON.toJSONString(jsonRows));
            }
            log.log(Level.INFO, "成功导出JSON文件到: {0}", file.getAbsolutePath());
            callback.onExportSuccess("JSON文件已成功导出到：" + file.getAbsolutePath(), file);

        } catch (IOException e) {
            handleExportError("JSON", e);
        }
    }

    /**
     * 显示文件保存对话框
     * 
     * @param description 文件类型描述
     * @param extension 文件扩展名过滤器
     * @return 用户选择的文件，如果用户取消则返回null
     */
    private File showSaveFileDialog(String description, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存文件");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(description, extension));
        return fileChooser.showSaveDialog(parentWindow);
    }

    /**
     * 验证导出数据的有效性
     * 
     * @param data 要导出的数据列表
     * @return 如果数据有效返回true，否则返回false
     */
    private boolean validateData(ObservableList<CompareResult> data) {
        if (data == null || data.isEmpty()) {
            callback.onExportError("没有可导出的数据");
            return false;
        }
        return true;
    }

    /**
     * 处理导出错误
     * 
     * @param format 导出格式名称
     * @param e 异常信息
     */
    private void handleExportError(String format, Exception e) {
        log.log(Level.SEVERE, "导出" + format + "文件失败: ", e);
        callback.onExportError("导出" + format + "文件时发生错误：" + e.getMessage());
    }

    /**
     * 创建Excel表头样式
     * 
     * @param workbook Excel工作簿
     * @return 配置好的单元格样式
     */
    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 创建Excel差异数据样式
     * 
     * @param workbook Excel工作簿
     * @return 配置好的单元格样式
     */
    private CellStyle createDiffStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * 写入Excel表头
     * 
     * @param sheet Excel工作表
     * @param headerStyle 表头样式
     */
    private void writeExcelHeader(XSSFSheet sheet, CellStyle headerStyle) {
        XSSFRow headerRow = sheet.createRow(0);
        List<TableColumn<CompareResult, ?>> columns = resultTable.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(columns.get(i).getText());
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 写入Excel数据行
     * 
     * @param sheet Excel工作表
     * @param diffStyle 差异数据样式
     */
    private void writeExcelData(XSSFSheet sheet, CellStyle diffStyle) {
        List<TableColumn<CompareResult, ?>> columns = resultTable.getColumns();
        int rowNum = 1;
        for (CompareResult row : resultTable.getItems()) {
            XSSFRow excelRow = sheet.createRow(rowNum++);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = excelRow.createCell(i);
                TableColumn<CompareResult, ?> column = columns.get(i);
                CellResult cellResult = row.getCellResult(column.getId());

                cell.setCellValue(getCellDisplayValue(cellResult));

                if (cellResult != null && cellResult.isDifferent()) {
                    cell.setCellStyle(diffStyle);
                }
            }
        }
    }

    /**
     * 获取单元格显示值
     * 对于有差异的数据，使用"❌"分隔主数据源和影子数据源的值
     * 
     * @param cellResult 单元格结果对象
     * @return 格式化后的显示字符串
     */
    private String getCellDisplayValue(CellResult cellResult) {
        if (cellResult == null) {
            return "";
        }
        String primaryStr = cellResult.getPrimaryValue() != null ? cellResult.getPrimaryValue().toString() : "";
        String shadowStr = cellResult.getShadowValue() != null ? cellResult.getShadowValue().toString() : "";

        if (cellResult.isDifferent()) {
            return primaryStr + " ❌ " + shadowStr;
        } else {
            return !primaryStr.isEmpty() ? primaryStr : shadowStr;
        }
    }

    /**
     * 导出结果回调接口
     * 用于处理导出成功和失败的回调
     */
    public interface ResultExportCallback {
        /**
         * 导出成功回调
         * 
         * @param message 成功消息
         * @param file 导出的文件
         */
        void onExportSuccess(String message, File file);

        /**
         * 导出失败回调
         * 
         * @param message 错误消息
         */
        void onExportError(String message);
    }
}