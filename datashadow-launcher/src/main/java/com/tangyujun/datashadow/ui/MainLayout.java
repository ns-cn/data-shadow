package com.tangyujun.datashadow.ui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;

/**
 * 主界面布局类
 */
public class MainLayout extends VBox {

    private final DataItemSection dataItemSection;
    private final DataSourceSection dataSourceSection;
    private final CompareSection compareSection;

    public MainLayout() {
        super(10);
        setPadding(new Insets(10));

        // 初始化各个区域
        dataItemSection = new DataItemSection();
        dataSourceSection = new DataSourceSection();
        compareSection = new CompareSection();

        // 设置对比区域自动填充剩余空间
        VBox.setVgrow(compareSection, Priority.ALWAYS);

        // 添加所有区域到主布局
        getChildren().addAll(
                dataItemSection,
                dataSourceSection,
                compareSection);
    }
}