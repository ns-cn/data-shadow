package com.tangyujun.datashadow.datasource.file;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.tangyujun.datashadow.datasource.DataSourceConfigurationCallback;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;
import com.tangyujun.datashadow.datasource.DataSourceRegistry;
import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

/**
 * CSV数据源
 * 支持读取CSV格式的文件
 * 将CSV表格数据转换为结构化数据
 * 支持自定义文件编码
 */
public class DataSourceCsv extends DataSourceFile {

    /**
     * 注册CSV数据源生成器
     * 
     * @return 数据源生成器
     */
    @DataSourceRegistry(group = "文件", friendlyName = "CSV")
    public static DataSourceGenerator generator() {
        return () -> new DataSourceCsv();
    }

    /**
     * CSV文件的编码格式
     * 如果未指定则默认使用UTF-8编码
     */
    private String encoding;

    /**
     * 验证CSV文件路径是否正确
     * 检查文件是否存在、可读、格式是否正确
     * 
     * @throws DataSourceValidException 当CSV文件路径格式错误或文件不可读时抛出
     */
    @Override
    public void valid() throws DataSourceValidException {
        if (path == null || path.isBlank()) {
            throw new DataSourceValidException("CSV文件路径不能为空", null);
        }
        try {
            if (!Files.isReadable(Paths.get(path))) {
                throw new DataSourceValidException("CSV文件路径不可读", null);
            }
            // 检查文件扩展名
            if (!path.toLowerCase().endsWith(".csv")) {
                throw new DataSourceValidException("CSV文件路径格式错误", null);
            }
            // 尝试读取CSV文件验证格式
            try (FileReader reader = new FileReader(path,
                    encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
                    CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().build().parse(reader)) {
                if (parser.getHeaderMap() == null || parser.getHeaderMap().isEmpty()) {
                    throw new DataSourceValidException("CSV文件路径格式错误", null);
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataSourceValidException("CSV文件路径格式错误", e);
        }
    }

    /**
     * 从CSV文件中获取数据
     * 读取CSV文件的所有数据,第一行作为表头
     * 支持自定义编码格式读取
     * 
     * @return 查询结果列表,每行数据以Map形式存储,key为列名,value为列值
     * @throws DataAccessException 当CSV文件读取失败时抛出
     */
    @Override
    public List<Map<String, Object>> acquireValues() throws DataAccessException {
        List<Map<String, Object>> result = new ArrayList<>();
        try (FileReader reader = new FileReader(path,
                encoding != null ? Charset.forName(encoding) : StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().build().parse(reader)) {

            for (CSVRecord record : parser) {
                Map<String, Object> rowData = new HashMap<>();
                parser.getHeaderNames().forEach(header -> rowData.put(header, record.get(header)));
                result.add(rowData);
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataAccessException("读取CSV文件失败: " + path + ", 原因: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 设置CSV文件编码
     * 如果不设置则默认使用UTF-8编码
     * 
     * @param encoding 编码格式,如UTF-8、GBK等
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * 重写equals方法
     * 用于比较两个DataSourceCsv对象是否相等
     * 如果两个对象的path和encoding属性都相等,则认为这两个对象相等
     *
     * @param o 要比较的对象
     * @return 如果对象相等返回true,否则返回false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        DataSourceCsv that = (DataSourceCsv) o;
        return Objects.equals(encoding, that.encoding);
    }

    /**
     * 重写hashCode方法
     * 根据对象的path和encoding属性生成哈希码
     *
     * @return 对象的哈希码
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), encoding);
    }

    /**
     * 获取CSV文件的列名
     * 
     * @return 列名列表
     */
    @Override
    public List<String> getColumns() {
        try (Reader reader = Files.newBufferedReader(Paths.get(path),
                encoding == null ? StandardCharsets.UTF_8 : Charset.forName(encoding))) {
            CSVParser parser = CSVFormat.DEFAULT.parse(reader);
            CSVRecord firstRecord = parser.iterator().next();
            return StreamSupport.stream(firstRecord.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 获取数据源的描述信息
     * 用于在界面上显示数据源的基本信息
     * 例如: CSV文件: D:/test.csv(UTF-8)
     * 
     * @return 数据源的描述信息字符串
     */
    @Override
    public String getDescription() {
        if (path == null || path.isBlank()) {
            return "";
        }
        return encoding == null ? "CSV文件: " + path : "CSV文件: " + path + "(" + encoding + ")";
    }

    /**
     * 导出为JSON，包含dataType和originData和父类的字段
     * 例如：{"path":"/path/to/file.csv","encoding":"UTF-8","mappings":{}}
     */
    @Override
    public String exportSource() {
        return JSON.toJSONString(this);
    }

    /**
     * 导入为JSON，包含dataType和originData和父类的字段
     * 例如：{"path":"/path/to/file.csv","encoding":"UTF-8","mappings":{}}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void importSource(String exportValueString) {
        // 解析JSON
        Map<String, Object> map = JSON.parseObject(exportValueString, new TypeReference<Map<String, Object>>() {
        });
        if (map == null) {
            return;
        }
        // 解析数据
        try {
            this.setPath((String) map.get("path"));
            this.setEncoding((String) map.get("encoding"));
            this.setMappings((Map<String, String>) map.get("mappings"));
        } catch (Exception e) {
        }
    }

    /**
     * 配置CSV数据源
     * 提供文件选择和编码设置功能
     * 
     * @param primaryStage 主窗口
     * @param callback     配置完成后的回调函数
     */
    @Override
    public void configure(Window primaryStage, DataSourceConfigurationCallback callback) {
        // 创建配置对话框
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("配置CSV数据源");

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        // 文件路径选择区域
        HBox pathBox = new HBox(10);
        pathBox.setAlignment(Pos.CENTER_LEFT);
        TextField pathField = new TextField();
        pathField.setEditable(false);
        pathField.setPrefWidth(300);
        if (path != null) {
            pathField.setText(path);
        }
        Button browseButton = new Button("浏览");
        pathBox.getChildren().addAll(new Label("文件路径:"), pathField, browseButton);

        // 编码选择区域
        HBox encodingBox = new HBox(10);
        encodingBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> encodingCombo = new ComboBox<>();
        encodingCombo.getItems().addAll(
                "UTF-8",
                "GBK",
                "GB2312",
                "ISO-8859-1",
                "UTF-16",
                "UTF-16BE",
                "UTF-16LE");
        // 设置默认编码
        encodingCombo.setValue(encoding != null ? encoding : "UTF-8");
        encodingBox.getChildren().addAll(new Label("文件编码:"), encodingCombo);

        // 按钮区域
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button confirmButton = new Button("确定");
        Button cancelButton = new Button("取消");
        buttonBox.getChildren().addAll(cancelButton, confirmButton);

        // 将所有组件添加到根容器
        root.getChildren().addAll(pathBox, encodingBox, buttonBox);

        // 设置浏览按钮事件
        browseButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择CSV文件");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            // 如果已有路径，则设置初始目录
            if (path != null && !path.isBlank()) {
                File currentFile = new File(path);
                if (currentFile.getParentFile() != null) {
                    fileChooser.setInitialDirectory(currentFile.getParentFile());
                }
            }
            File selectedFile = fileChooser.showOpenDialog(dialog);
            if (selectedFile != null) {
                pathField.setText(selectedFile.getAbsolutePath());
            }
        });

        // 设置确定按钮事件
        confirmButton.setOnAction(event -> {
            String selectedPath = pathField.getText();
            String selectedEncoding = encodingCombo.getValue();

            if (selectedPath == null || selectedPath.isBlank()) {
                showError("请选择CSV文件");
                return;
            }

            // 更新数据源配置
            setPath(selectedPath);
            setEncoding(selectedEncoding);

            try {
                // 验证配置
                valid();
                // 通知配置完成
                callback.onConfigureFinished();
                dialog.close();
            } catch (DataSourceValidException e) {
                showError("配置验证失败: " + e.getMessage());
            }
        });

        // 设置取消按钮事件
        cancelButton.setOnAction(event -> {
            callback.onConfigureCancelled();
            dialog.close();
        });

        // 显示对话框
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * 显示错误提示对话框
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
