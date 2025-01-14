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
    private final ResultSection resultSection;

    public MainLayout() {
        super(10); // 设置垂直间距为10
        setPadding(new Insets(10));

        // 初始化各个区域
        dataItemSection = new DataItemSection();
        dataSourceSection = new DataSourceSection();
        compareSection = new CompareSection();
        resultSection = new ResultSection();

        // 设置结果区域自动填充剩余空间
        VBox.setVgrow(resultSection, Priority.ALWAYS);

        // 添加所有区域到主布局
        getChildren().addAll(
                dataItemSection,
                dataSourceSection,
                compareSection,
                resultSection);
    }
}