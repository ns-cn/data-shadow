package com.tangyujun.datashadow.scheme;

import com.alibaba.fastjson2.JSON;
import com.tangyujun.datashadow.core.DataFactory;

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
            // 创建导出数据结构
            ComparisonScheme schemeData = ComparisonScheme.snapshot();

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
                // 检查数据源是否存在
                StringBuilder errorMsg = new StringBuilder();
                if (schemeData.getPrimaryDataSource() != null) {
                    String primarySourceGroup = schemeData.getPrimaryDataSource().getGroup();
                    String primarySourceName = schemeData.getPrimaryDataSource().getSourceName();
                    if (!DataFactory.getInstance().getDataSources().containsKey(primarySourceGroup)
                            || !DataFactory.getInstance().getDataSources().get(primarySourceGroup)
                                    .containsKey(primarySourceName)) {
                        errorMsg.append("主数据源 '").append(primarySourceGroup).append("/").append(primarySourceName)
                                .append("' 不存在\n");
                    }
                }
                if (schemeData.getShadowDataSource() != null) {
                    String shadowSourceGroup = schemeData.getShadowDataSource().getGroup();
                    String shadowSourceName = schemeData.getShadowDataSource().getSourceName();
                    if (!DataFactory.getInstance().getDataSources().containsKey(shadowSourceGroup)
                            || !DataFactory.getInstance().getDataSources().get(shadowSourceGroup)
                                    .containsKey(shadowSourceName)) {
                        errorMsg.append("影子数据源 '").append(shadowSourceGroup).append("/").append(shadowSourceName)
                                .append("' 不存在\n");
                    }
                }
                if (errorMsg.length() > 0) {
                    showError("导入异常", "以下数据源不存在，请注意检查数据源插件:\n" + errorMsg);
                }
                // 应用对比方案
                schemeData.applyToFactory();
                return true;
            }
        } catch (IOException e) {
            showError("导入异常", "导入对比方案时发生错误：" + e.getMessage());
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