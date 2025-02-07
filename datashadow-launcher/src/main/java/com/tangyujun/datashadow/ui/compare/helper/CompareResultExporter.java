package com.tangyujun.datashadow.ui.compare.helper;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.opencsv.CSVWriter;
import com.tangyujun.datashadow.ui.compare.model.CellResult;
import com.tangyujun.datashadow.ui.compare.model.CompareResult;
import com.tangyujun.datashadow.ui.compare.model.ResultExportCallback;

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
 * 主要功能:
 * 1. 导出CSV格式
 * - 包含表头和数据行
 * - 差异数据使用"❌"分隔显示
 * 
 * 2. 导出Excel格式
 * - 表头使用灰色背景
 * - 差异数据使用黄色背景标注
 * - 自动调整列宽
 * - 差异数据使用"❌"分隔显示
 * 
 * 3. 导出JSON格式
 * - 每个单元格包含主数据源值、影子数据源值和差异标记
 * - 保持完整的数据结构
 * 
 * 使用方式:
 * 1. 创建实例时需提供:
 * - TableView对象: 包含要导出的数据
 * - Window对象: 用于显示文件选择对话框
 * - 回调接口: 处理导出成功和失败的情况
 * 
 * 2. 调用相应的导出方法:
 * - exportToCsv()
 * - exportToExcel()
 * - exportToJson()
 * 
 * 错误处理:
 * - 导出前验证数据有效性
 * - 导出过程中的异常会通过回调接口通知
 * - 记录详细的错误日志
 * 
 * 注意事项:
 * - 确保导出目录有写入权限
 * - 大数据量导出可能需要较长时间
 * - Excel导出使用XLSX格式以支持更大数据量
 */
public class CompareResultExporter {
    /** 日志记录器 - 用于记录导出过程中的日志信息 */
    private static final Logger log = Logger.getLogger(CompareResultExporter.class.getName());

    /** 包含对比结果的表格视图 */
    private final TableView<CompareResult> resultTable;
    /** 父窗口 - 用于显示文件选择对话框 */
    private final Window parentWindow;
    /** 导出结果回调接口 - 用于处理导出成功和失败的情况 */
    private final ResultExportCallback callback;

    /**
     * 构造函数
     * 初始化导出工具类,设置必要的组件引用
     * 
     * @param resultTable  包含对比结果的表格视图,提供要导出的数据源
     * @param parentWindow 父窗口,用于显示文件选择对话框
     * @param callback     导出结果回调接口,处理导出成功和失败的情况
     */
    public CompareResultExporter(TableView<CompareResult> resultTable, Window parentWindow,
            ResultExportCallback callback) {
        this.resultTable = resultTable;
        this.parentWindow = parentWindow;
        this.callback = callback;
    }

    /**
     * 导出为CSV文件
     * 将对比结果表格数据导出为CSV格式
     * 
     * 导出步骤:
     * 1. 显示文件保存对话框
     * 2. 验证导出数据的有效性
     * 3. 写入表头信息
     * 4. 逐行写入数据内容
     * 5. 处理导出结果回调
     * 
     * 数据格式:
     * - 使用标准CSV格式
     * - 包含表头和数据行
     * - 差异数据使用"❌"分隔显示
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
     * 将对比结果表格数据导出为Excel格式
     * 
     * 导出步骤:
     * 1. 显示文件保存对话框
     * 2. 验证导出数据的有效性
     * 3. 创建工作簿和样式
     * 4. 写入表头和数据
     * 5. 调整列宽
     * 6. 保存文件
     * 7. 处理导出结果回调
     * 
     * 格式特性:
     * - 表头使用灰色背景
     * - 差异数据使用黄色背景标注
     * - 自动调整列宽
     * - 差异数据使用"❌"分隔显示
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
     * 将对比结果表格数据导出为JSON格式
     * 
     * 导出步骤:
     * 1. 显示文件保存对话框
     * 2. 验证导出数据的有效性
     * 3. 构建JSON数据结构
     * 4. 写入文件
     * 5. 处理导出结果回调
     * 
     * 数据结构:
     * - 每行数据为一个JSON对象
     * - 每个单元格包含:
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
     * 配置并显示文件选择器,让用户选择保存位置
     * 
     * @param description 文件类型描述,如"CSV Files"
     * @param extension   文件扩展名过滤器,如"*.csv"
     * @return 用户选择的文件对象,如果用户取消则返回null
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
     * 检查数据列表是否为空
     * 
     * @param data 要导出的数据列表
     * @return 如果数据有效返回true,否则返回false并通过回调通知错误
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
     * 记录错误日志并通过回调通知错误
     * 
     * @param format 导出格式名称(CSV/Excel/JSON)
     * @param e      异常信息
     */
    private void handleExportError(String format, Exception e) {
        log.log(Level.SEVERE, "导出" + format + "文件失败: ", e);
        callback.onExportError("导出" + format + "文件时发生错误：" + e.getMessage());
    }

    /**
     * 创建Excel表头样式
     * 配置表头的背景色为浅灰色
     * 
     * @param workbook Excel工作簿对象
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
     * 配置差异数据的背景色为浅黄色
     * 
     * @param workbook Excel工作簿对象
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
     * 创建表头行并应用表头样式
     * 
     * @param sheet       Excel工作表对象
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
     * 逐行写入数据并为差异数据应用样式
     * 
     * @param sheet     Excel工作表对象
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
     * 格式化单元格数据,对于差异数据使用"❌"分隔显示
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
}