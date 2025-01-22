package com.tangyujun.datashadow.ui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import com.tangyujun.datashadow.ui.compare.CompareSection;
import com.tangyujun.datashadow.ui.dataitem.DataItemSection;
import com.tangyujun.datashadow.ui.datasource.DataSourceSection;
import com.tangyujun.datashadow.ui.menu.MenuBarSection;

import javafx.geometry.Insets;

/**
 * 主界面布局类
 */
public class MainLayout extends VBox {

    private final MenuBarSection menuBarSection;
    private final DataItemSection dataItemSection;
    private final DataSourceSection dataSourceSection;
    private final CompareSection compareSection;

    public MainLayout() {
        super(0); // 将间距改为0，因为菜单栏不需要间距

        // 初始化菜单栏
        menuBarSection = new MenuBarSection();

        // 创建内容区域容器
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));

        // 初始化各个区域
        dataItemSection = new DataItemSection();
        dataSourceSection = new DataSourceSection();
        compareSection = new CompareSection();

        // 设置对比区域自动填充剩余空间
        VBox.setVgrow(compareSection, Priority.ALWAYS);

        // 将功能区域添加到内容容器
        contentBox.getChildren().addAll(
                dataItemSection,
                dataSourceSection,
                compareSection);

        // 添加所有区域到主布局
        getChildren().addAll(menuBarSection, contentBox);
    }
}