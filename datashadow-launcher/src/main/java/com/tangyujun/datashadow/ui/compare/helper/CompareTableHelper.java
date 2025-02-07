package com.tangyujun.datashadow.ui.compare.helper;

import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.ui.compare.model.CellResult;
import com.tangyujun.datashadow.ui.compare.model.CompareResult;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

/**
 * 比对表格辅助类
 * 处理表格显示相关的逻辑，包括：
 * 1. 表格列的创建和配置 - 根据数据项动态创建和配置表格列
 * 2. 单元格样式设置 - 根据数据内容设置单元格的显示样式
 * 3. 列标题显示逻辑 - 支持根据显示模式切换列标题的显示方式
 * 4. 差异项标记 - 对比结果中的差异项使用特殊样式标记
 * 5. 数据项校验 - 检查数据项配置的完整性并给出提示
 */
public class CompareTableHelper {

    /**
     * 更新表格列
     * 根据提供的数据项列表和标题显示模式，重新创建并配置表格的所有列
     * 
     * @param resultTable       要更新的表格控件
     * @param dataItems         数据项列表，用于创建表格列
     * @param headerDisplayMode 列标题显示模式，决定使用数据项代码还是别名作为列标题
     */
    public static void updateColumns(TableView<CompareResult> resultTable,
            List<DataItem> dataItems,
            String headerDisplayMode) {
        resultTable.getColumns().clear();

        if (dataItems != null) {
            for (DataItem dataItem : dataItems) {
                TableColumn<CompareResult, String> column = createColumn(dataItem, headerDisplayMode);
                resultTable.getColumns().add(column);
            }
        }
    }

    /**
     * 创建表格列
     * 根据数据项和标题显示模式创建一个新的表格列
     * 
     * @param dataItem          数据项，包含列的基本信息
     * @param headerDisplayMode 列标题显示模式
     * @return 配置好的表格列对象
     */
    private static TableColumn<CompareResult, String> createColumn(DataItem dataItem, String headerDisplayMode) {
        TableColumn<CompareResult, String> column = new TableColumn<>(getColumnHeader(dataItem, headerDisplayMode));
        column.setId(dataItem.getCode());
        column.setPrefWidth(150);

        configureColumnFactory(column, dataItem);

        return column;
    }

    /**
     * 配置列的工厂方法
     * 设置列的值工厂和单元格工厂，处理数据显示和样式
     * 
     * @param column   要配置的表格列
     * @param dataItem 与该列关联的数据项
     */
    private static void configureColumnFactory(TableColumn<CompareResult, String> column, DataItem dataItem) {
        // 设置值工厂 - 从CompareResult中获取对应数据项的显示值
        column.setCellValueFactory(cellData -> {
            CellResult cellResult = cellData.getValue().getCellResult(dataItem.getCode());
            return new SimpleStringProperty(cellResult != null ? cellResult.getDisplayValue() : "");
        });

        // 设置单元格工厂 - 处理单元格的显示样式
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                applyCellStyle(this, item, empty);
            }
        });
    }

    /**
     * 应用单元格样式
     * 根据单元格内容设置不同的显示样式：
     * - 空单元格：清除样式
     * - 差异项(包含❌)：红色字体居中显示
     * - 普通项：居中显示
     * 
     * @param cell  要设置样式的单元格
     * @param item  单元格内容
     * @param empty 单元格是否为空
     */
    private static void applyCellStyle(TableCell<CompareResult, String> cell, String item, boolean empty) {
        if (empty || item == null) {
            cell.setText(null);
            cell.setStyle("");
        } else if (item.contains("❌")) {
            cell.setText(item);
            cell.setStyle("-fx-text-fill: red; -fx-alignment: center;");
        } else {
            cell.setText(item);
            cell.setStyle("-fx-alignment: center;");
        }
    }

    /**
     * 获取列标题
     * 根据显示模式和数据项配置生成列标题：
     * - 当显示模式为"数据项别名优先"且数据项有别名时，使用别名
     * - 其他情况使用数据项代码
     * - 如果数据项未配置比较器，在标题前添加警告标记❗
     * 
     * @param item              数据项对象
     * @param headerDisplayMode 标题显示模式
     * @return 格式化后的列标题
     */
    private static String getColumnHeader(DataItem item, String headerDisplayMode) {
        String header;
        if ("数据项别名优先".equals(headerDisplayMode) && item.getNick() != null && !item.getNick().isEmpty()) {
            header = item.getNick();
        } else {
            header = item.getCode();
        }

        if (item.getComparator() == null) {
            header = "❗" + header;
        }
        return header;
    }
}