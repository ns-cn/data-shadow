package com.tangyujun.datashadow.ui.datasource;

import com.tangyujun.datashadow.ai.AIService;
import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.datasource.DataSource;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.tangyujun.datashadow.config.ConfigFactory;
import com.tangyujun.datashadow.config.Configuration;

/**
 * 数据源映射对话框
 * 用于配置数据项与数据源字段之间的映射关系
 * 
 * 主要功能:
 * 1. 显示数据项列表和对应的数据源字段下拉选择
 * 2. 支持自动映射和重建映射功能
 * 3. 提供确认和取消操作
 * 4. 返回配置好的映射关系
 */
public class DataSourceMappingDialog extends Stage {

    private static final Logger log = LoggerFactory.getLogger(DataSourceMappingDialog.class);

    /**
     * 存储数据项与数据源字段的映射关系
     */
    private final Map<DataItem, String> mappings = new HashMap<>();

    /**
     * 当前操作的数据源对象
     */
    private final DataSource dataSource;

    /**
     * 数据源中的所有字段列表
     */
    private final List<String> sourceColumns;

    /**
     * 标记用户是否点击了确认按钮
     */
    private boolean confirmed = false;

    /**
     * 构造函数
     * 
     * @param owner      父窗口
     * @param dataSource 需要配置映射的数据源
     */
    public DataSourceMappingDialog(Window owner, DataSource dataSource) {
        this.dataSource = dataSource;
        this.sourceColumns = dataSource.getColumns();

        // 初始化映射关系,回显已有的映射
        DataFactory.getInstance().getDataItems().forEach(dataItem -> {
            // 添加空值检查
            Map<String, String> sourceMappings = dataSource.getMappings();
            if (sourceMappings != null) {
                String mappedField = sourceMappings.get(dataItem.getCode());
                if (mappedField != null) {
                    mappings.put(dataItem, mappedField);
                }
            }
        });

        // 设置对话框属性
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("数据源字段映射");
        setWidth(500); // 设置默认宽度
        setHeight(350); // 设置默认高度
        setMinWidth(400);
        setMinHeight(300);

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // 标题
        Label titleLabel = new Label("数据源字段映射");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 操作按钮区域
        VBox buttonBox = createButtonBox();

        // 映射表格
        TableView<DataItem> mappingTable = createMappingTable();
        VBox.setVgrow(mappingTable, Priority.ALWAYS);

        // 底部按钮
        HBox bottomButtons = createBottomButtons();

        root.getChildren().addAll(titleLabel, buttonBox, mappingTable, bottomButtons);

        Scene scene = new Scene(root);
        setScene(scene);
    }

    /**
     * 创建顶部操作按钮区域
     * 包含自动映射、重建映射按钮和提示说明
     * 
     * @return 按钮容器
     */
    private VBox createButtonBox() {
        // 使用VBox来实现垂直布局
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #eee;");

        // 按钮容器
        HBox buttonBox = new HBox(10);
        Button autoMapButton = new Button("自动映射");
        Button rebuildButton = new Button("重建映射");
        Button clearButton = new Button("清空映射");

        // 提示文本
        Label tipLabel = new Label("注：自动映射将根据字段名称相似度追加映射关系，重建映射将根据数据源字段创建新的数据项，清空映射将清除所有已建立的映射关系");
        tipLabel.setWrapText(true);
        tipLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-padding: 5 0 0 0;");
        tipLabel.setMaxWidth(460); // 设置最大宽度以强制换行

        autoMapButton.setOnAction(event -> handleAutoMap());
        rebuildButton.setOnAction(event -> handleRebuild());
        clearButton.setOnAction(event -> handleClearMappings());

        // 将按钮添加到按钮容器
        buttonBox.getChildren().addAll(autoMapButton, rebuildButton, clearButton);

        // 将按钮容器和提示文本添加到垂直布局容器
        vbox.getChildren().addAll(buttonBox, tipLabel);

        return vbox;
    }

    /**
     * 创建映射关系表格
     * 显示数据项名称和对应的数据源字段选择下拉框
     * 
     * @return 映射表格控件
     */
    @SuppressWarnings("unchecked")
    private TableView<DataItem> createMappingTable() {
        TableView<DataItem> table = new TableView<>();

        // 数据项名称列
        TableColumn<DataItem, String> nameColumn = new TableColumn<>("数据项名称");
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayName()));
        nameColumn.setPrefWidth(150);

        // 数据源字段列
        TableColumn<DataItem, String> mappingColumn = new TableColumn<>("数据源字段");
        mappingColumn.setPrefWidth(150);
        mappingColumn.setCellFactory(column -> new TableCell<>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                // 添加一个空选项
                List<String> options = new ArrayList<>();
                options.add(""); // 空选项
                options.addAll(sourceColumns);
                comboBox.setItems(FXCollections.observableArrayList(options));
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.valueProperty().addListener((observable, oldValue, newVal) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        if (newVal != null && !newVal.isEmpty()) {
                            mappings.put(getTableRow().getItem(), newVal);
                        } else {
                            mappings.remove(getTableRow().getItem());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DataItem dataItem = getTableRow().getItem();
                    if (dataItem != null) {
                        comboBox.setValue(mappings.get(dataItem));
                        setGraphic(comboBox);
                    }
                }
            }
        });

        table.getColumns().addAll(nameColumn, mappingColumn);
        table.setItems(DataFactory.getInstance().getDataItems());

        return table;
    }

    /**
     * 创建底部按钮区域
     * 包含确定和取消按钮
     * 
     * @return 按钮容器
     */
    private HBox createBottomButtons() {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("取消");
        Button confirmButton = new Button("确定");
        confirmButton.setStyle("-fx-background-color: #1890ff; -fx-text-fill: white;");

        cancelButton.setOnAction(event -> close());
        confirmButton.setOnAction(event -> {
            // 将映射结果回写到数据源
            dataSource.clearMappings(); // 先清除所有映射
            mappings.forEach((dataItem, sourceField) -> {
                if (sourceField != null && !sourceField.isEmpty()) {
                    dataSource.addMapping(dataItem.getCode(), sourceField);
                }
            });
            confirmed = true;
            close();
        });

        buttonBox.getChildren().addAll(cancelButton, confirmButton);
        return buttonBox;
    }

    /**
     * 处理自动映射按钮点击事件
     * 优先使用AI进行智能映射，如果AI映射失败则使用传统方式
     */
    private void handleAutoMap() {
        // 获取配置
        Configuration config = ConfigFactory.getInstance().getConfiguration();

        // 检查AI配置是否完整
        if (config.getAiModel() == null || config.getAiApiKey() == null || config.getAiApiKey().trim().isEmpty()) {
            log.info("AI configuration not found, using traditional mapping");
            performTraditionalMapping();
            return;
        }

        // 显示等待对话框
        Alert waitingDialog = new Alert(Alert.AlertType.INFORMATION);
        waitingDialog.setTitle("请稍候");
        waitingDialog.setHeaderText(null);
        waitingDialog.setContentText("正在使用AI分析字段映射关系...");
        waitingDialog.show();

        // 准备数据项信息
        List<Map<String, String>> itemInfos = DataFactory.getInstance().getDataItems().stream()
                .map(item -> {
                    Map<String, String> info = new HashMap<>();
                    info.put("code", item.getCode());
                    if (item.getNick() != null) {
                        info.put("nick", item.getNick());
                    }
                    return info;
                })
                .collect(Collectors.toList());

        // 尝试使用AI进行映射
        AIService.suggestMappings(
                config.getAiModel(),
                config.getAiApiKey(),
                itemInfos,
                sourceColumns,
                // AI映射成功回调
                aiMappings -> {
                    Platform.runLater(() -> {
                        waitingDialog.close();
                        boolean aiMappingUsed = false;

                        // 应用AI建议的映射
                        for (DataItem dataItem : DataFactory.getInstance().getDataItems()) {
                            String suggestedField = aiMappings.get(dataItem.getCode());
                            if (suggestedField != null && sourceColumns.contains(suggestedField)) {
                                mappings.put(dataItem, suggestedField);
                                aiMappingUsed = true;
                            }
                        }

                        // 如果AI没有提供有效映射，使用传统方式
                        if (!aiMappingUsed) {
                            performTraditionalMapping();
                            return;
                        }

                        // 刷新表格显示
                        @SuppressWarnings("unchecked")
                        TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
                        if (table != null) {
                            table.refresh();
                        }

                        // 显示完成提示
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("提示");
                        alert.setHeaderText(null);
                        alert.setContentText("自动映射完成 (使用AI辅助分析)");
                        alert.showAndWait();
                    });
                },
                // AI映射失败回调，使用传统方式
                error -> {
                    Platform.runLater(() -> {
                        waitingDialog.close();
                        log.warn("AI mapping failed, falling back to traditional mapping: {}", error);
                        performTraditionalMapping();
                    });
                });
    }

    /**
     * 使用传统方式进行字段映射
     */
    private void performTraditionalMapping() {
        // 遍历所有数据项
        for (DataItem dataItem : DataFactory.getInstance().getDataItems()) {
            // 如果已经有映射关系，则跳过
            if (mappings.containsKey(dataItem)) {
                continue;
            }

            String bestMatch = null;
            double bestSimilarity = 0;

            // 遍历数据源字段，寻找最佳匹配
            for (String sourceField : sourceColumns) {
                // 计算相似度
                double similarity = calculateSimilarity(dataItem.getCode().toLowerCase(), sourceField.toLowerCase());
                if (similarity > bestSimilarity && similarity > 0.5) { // 设置相似度阈值
                    bestSimilarity = similarity;
                    bestMatch = sourceField;
                }
            }

            // 如果找到匹配的字段,建立映射关系
            if (bestMatch != null) {
                mappings.put(dataItem, bestMatch);
            }
        }

        // 刷新表格显示
        @SuppressWarnings("unchecked")
        TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
        if (table != null) {
            table.refresh();
        }

        // 显示完成提示
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText("自动映射完成 (使用传统匹配方式)");
        alert.showAndWait();
    }

    /**
     * 计算两个字符串的相似度
     */
    private double calculateSimilarity(String str1, String str2) {
        // 使用Levenshtein距离计算相似度
        int distance = levenshteinDistance(str1, str2);
        int maxLength = Math.max(str1.length(), str2.length());
        return maxLength == 0 ? 1.0 : (1.0 - (double) distance / maxLength);
    }

    /**
     * 计算Levenshtein距离
     */
    private int levenshteinDistance(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= str2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + 1,
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }

        return dp[str1.length()][str2.length()];
    }

    /**
     * 处理重建映射按钮点击事件
     * 根据数据源字段创建新的映射关系
     */
    private void handleRebuild() {
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认重建映射");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("重建映射将根据数据源字段创建新的数据项。是否继续？");
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 获取现有数据项的code列表
            List<String> existingCodes = DataFactory.getInstance().getDataItems().stream()
                    .map(DataItem::getCode)
                    .collect(Collectors.toList());
            // 根据数据源字段创建新的数据项
            for (String column : sourceColumns) {
                // 如果字段已经存在对应的数据项，则跳过
                if (existingCodes.contains(column)) {
                    continue;
                }
                DataItem newItem = new DataItem();
                newItem.setCode(column);
                // 添加到数据工厂
                DataFactory.getInstance().addDataItem(newItem);
                // 建立映射关系
                mappings.put(newItem, column);
            }
            // 刷新表格显示
            @SuppressWarnings("unchecked")
            TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
            if (table != null) {
                table.refresh();
            }
            // 显示完成提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("重建映射完成");
            alert.showAndWait();
        }
    }

    /**
     * 处理清空映射按钮点击事件
     * 清除所有已建立的字段映射关系
     */
    private void handleClearMappings() {
        // 显示确认对话框
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("确认清空映射");
        confirmDialog.setHeaderText(null);
        confirmDialog.setContentText("确定要清空所有字段映射关系吗？");
        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 清空映射关系
            mappings.clear();

            // 刷新表格显示
            @SuppressWarnings("unchecked")
            TableView<DataItem> table = (TableView<DataItem>) getScene().lookup(".table-view");
            if (table != null) {
                table.refresh();
            }

            // 显示完成提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("提示");
            alert.setHeaderText(null);
            alert.setContentText("映射关系已清空");
            alert.showAndWait();
        }
    }

    /**
     * 获取映射结果
     * 如果用户点击了确认按钮,返回配置好的映射关系
     * 否则返回null表示用户取消了操作
     * 
     * @return 数据项与数据源字段的映射关系,取消操作时返回null
     */
    public Map<DataItem, String> getMappings() {
        return confirmed ? mappings : null;
    }
}