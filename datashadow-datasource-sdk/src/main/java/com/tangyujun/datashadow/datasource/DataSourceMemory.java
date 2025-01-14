package com.tangyujun.datashadow.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.tangyujun.datashadow.exception.DataAccessException;
import com.tangyujun.datashadow.exception.DataSourceValidException;

import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * 内存数据源
 * 用于存储和管理内存中的数据集
 * 主要用于临时数据的存储和处理
 */
public class DataSourceMemory extends DataSource {

    /**
     * 数据集
     * 使用List<Map<String, Object>>存储数据
     * 其中List中的每个Map代表一行数据
     * Map的key为字段名，value为字段值
     */
    private List<Map<String, Object>> data;

    /**
     * 验证数据源是否有效
     * 由于是内存数据源，不需要特殊的验证逻辑
     * 
     * @throws DataSourceValidException 当数据源验证失败时抛出此异常
     */
    @Override
    public void valid() throws DataSourceValidException {
    }

    /**
     * 获取数据集
     * 直接返回内存中存储的数据集
     * 
     * @return 返回内存中存储的数据集
     * @throws DataAccessException 当数据访问出错时抛出此异常
     */
    @Override
    public List<Map<String, Object>> getValues() throws DataAccessException {
        return data;
    }

    /**
     * 获取数据集的列名
     * 
     * @return 列名列表
     */
    @Override
    public List<String> getColumns() {
        if (data == null || data.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(data.get(0).keySet());
    }

    /**
     * 获取已配置数据源的简单描述
     * 用于在界面上显示数据源的基本信息
     * 例如: mysql:127.0.0.1:3306/test 或 D:/test.xlsx
     * 
     * @return 数据源的简单描述字符串
     */
    @Override
    public String getDescription() {
        return "内存数据源";
    }

    /**
     * 配置数据源的具体行为，例如打开对话框、选择文件、配置数据库链接信息等
     * 
     * @param primaryStage 主窗口
     */
    @Override
    public void configure(Stage primaryStage) {
        // 创建新的对话框窗口
        Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL); // 设置为模态窗口
        dialog.initOwner(primaryStage); // 设置主窗口为父窗口

        BorderPane dialogRoot = new BorderPane();
        Scene dialogScene = new Scene(dialogRoot, 400, 300);

        dialog.setTitle("内存数据源配置");
        dialog.setScene(dialogScene);
        dialog.show();
    }
}
