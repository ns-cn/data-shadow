package com.tangyujun.datashadow.ui;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.core.ComparisonScheme;
import com.tangyujun.datashadow.core.DataFactory;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.datasource.DataSourceGenerator;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * 对比方案管理器
 * 用于处理对比方案的导入导出
 */
public class ComparisonSchemeManager {
    /**
     * 导出对比方案
     * 
     * @param window 父窗口
     * @return 是否导出成功
     */
    public static boolean exportScheme(Window window) {
        try {
            // 获取当前配置
            DataFactory factory = DataFactory.getInstance();

            // 创建导出数据结构
            ComparisonScheme schemeData = new ComparisonScheme();
            schemeData.setDataItems(factory.getDataItems());
            schemeData.setPrimaryDataSourceName(factory.getPrimaryDataSourceName());
            schemeData.setShadowDataSourceName(factory.getShadowDataSourceName());

            // 获取数据源配置
            if (factory.getPrimaryDataSource() != null) {
                schemeData.setPrimaryDataSource(factory.getPrimaryDataSource().exportSource());
            }
            if (factory.getShadowDataSource() != null) {
                schemeData.setShadowDataSource(factory.getShadowDataSource().exportSource());
            }

            // 转换为JSON并Base64编码
            String jsonStr = JSON.toJSONString(schemeData);
            String base64Str = Base64.getEncoder().encodeToString(jsonStr.getBytes());

            // 选择保存位置
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("保存对比方案");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Shadow方案文件", "*.shadow"));
            File file = fileChooser.showSaveDialog(window);

            if (file != null) {
                // 确保文件扩展名正确
                String path = file.getPath();
                if (!path.endsWith(".shadow")) {
                    path += ".shadow";
                    file = new File(path);
                }

                // 写入文件
                Files.write(file.toPath(), base64Str.getBytes());
                return true;
            }

        } catch (IOException e) {
            showError("导出失败", "导出对比方案时发生错误：" + e.getMessage());
        }
        return false;
    }

    /**
     * 导入对比方案
     * 
     * @param window 父窗口
     * @return 是否导入成功
     */
    public static boolean importScheme(Window window) {
        try {
            // 选择文件
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导入对比方案");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Shadow方案文件", "*.shadow"));
            File file = fileChooser.showOpenDialog(window);

            if (file != null) {
                // 读取并解码文件内容
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64Str = new String(bytes);
                String jsonStr = new String(Base64.getDecoder().decode(base64Str));

                // 解析JSON
                ComparisonScheme schemeData = JSON.parseObject(jsonStr, ComparisonScheme.class);

                // 获取工厂实例
                DataFactory factory = DataFactory.getInstance();

                // 清空当前配置
                factory.clearDataItems();

                // 导入数据项
                if (schemeData.getDataItems() != null) {
                    factory.addDataItems(schemeData.getDataItems());
                }

                // 导入数据源
                try {
                    if (schemeData.getPrimaryDataSourceName() != null && schemeData.getPrimaryDataSource() != null) {
                        DataSourceGenerator dataSourceGenerator = factory.getDataSources()
                                .get(schemeData.getPrimaryDataSourceName());
                        if (dataSourceGenerator == null) {
                            showError("导入主数据源失败", "找不到对应的数据源类型：" + schemeData.getPrimaryDataSourceName() +
                                    "\n请确保已安装该数据源插件。");
                        } else {
                            DataSource primaryDataSource = dataSourceGenerator.generate();
                            primaryDataSource.importSource(schemeData.getPrimaryDataSource());
                            factory.setPrimaryDataSource(schemeData.getPrimaryDataSourceName(), primaryDataSource);
                        }
                    }
                } catch (Exception exception) {
                    showError("导入主数据源失败", "导入主数据源时发生错误：" + exception.getMessage());
                }
                try {
                    if (schemeData.getShadowDataSourceName() != null && schemeData.getShadowDataSource() != null) {
                        DataSourceGenerator dataSourceGenerator = factory.getDataSources()
                                .get(schemeData.getShadowDataSourceName());
                        if (dataSourceGenerator == null) {
                            showError("导入影子数据源失败", "找不到对应的数据源类型：" + schemeData.getShadowDataSourceName() +
                                    "\n请确保已安装该数据源插件。");
                        } else {
                            DataSource shadowDataSource = dataSourceGenerator.generate();
                            shadowDataSource.importSource(schemeData.getShadowDataSource());
                            factory.setShadowDataSource(schemeData.getShadowDataSourceName(), shadowDataSource);
                        }
                    }
                } catch (Exception exception) {
                    showError("导入影子数据源失败", "导入影子数据源时发生错误：" + exception.getMessage());
                }
                return true;
            }
        } catch (IOException e) {
            showError("导入失败", "导入对比方案时发生错误：" + e.getMessage());
        }
        return false;
    }

    /**
     * 显示错误对话框
     */
    private static void showError(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}