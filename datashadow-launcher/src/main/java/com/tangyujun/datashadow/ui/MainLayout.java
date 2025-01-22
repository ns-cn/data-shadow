package com.tangyujun.datashadow.ui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

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

        // 设置主窗口最小尺寸
        setMinWidth(1366);
        setMinHeight(900);

        // 初始化菜单栏
        menuBarSection = new MenuBarSection();

        // 创建内容区域容器
        VBox contentBox = new VBox(10);
        contentBox.setPadding(new Insets(10));
        VBox.setVgrow(contentBox, Priority.ALWAYS); // 让contentBox填充剩余空间

        // 初始化各个区域
        dataItemSection = new DataItemSection();
        dataSourceSection = new DataSourceSection();
        compareSection = new CompareSection();

        // 设置数据项区域的默认高度和最小高度
        dataItemSection.setPrefHeight(200);
        dataItemSection.setMinHeight(100);
        dataItemSection.setMaxHeight(Region.USE_PREF_SIZE); // 防止过度拉伸

        // 设置数据源区域固定高度
        dataSourceSection.setPrefHeight(120); // 设置固定的首选高度
        dataSourceSection.setMinHeight(120); // 最小高度与首选高度相同
        dataSourceSection.setMaxHeight(120); // 最大高度与首选高度相同，确保高度固定

        // 让对比区域填充剩余空间，但确保有最小高度
        compareSection.setMinHeight(200);
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