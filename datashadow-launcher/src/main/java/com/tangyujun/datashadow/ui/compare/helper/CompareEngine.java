package com.tangyujun.datashadow.ui.compare.helper;

import com.tangyujun.datashadow.dataitem.DataItem;
import com.tangyujun.datashadow.dataresult.CellResult;
import com.tangyujun.datashadow.dataresult.CompareResult;
import com.tangyujun.datashadow.datasource.DataSource;
import com.tangyujun.datashadow.exception.DataAccessException;

import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据对比引擎
 * 处理数据对比相关的核心逻辑，包括：
 * 1. 数据对比前置条件验证
 * 2. 数据获取和映射
 * 3. 数据对比处理
 * 4. 结果生成
 * 
 * 主要功能:
 * 1. 执行主数据源和影子数据源的数据对比
 * 2. 支持多字段联合主键
 * 3. 处理数据源字段映射
 * 4. 生成详细的对比结果
 * 
 * 对比流程:
 * 1. 获取主键字段列表
 * 2. 从两个数据源获取数据和字段映射
 * 3. 构建影子数据源查找索引
 * 4. 遍历主数据源进行对比
 * 5. 处理仅在影子数据源存在的数据
 * 
 * 数据处理:
 * 1. 支持字段映射转换
 * 2. 处理空值情况
 * 3. 生成规范化的对比结果
 * 
 * 注意事项:
 * 1. 确保主键字段在两个数据源中都存在
 * 2. 字段映射需要正确配置
 * 3. 数据量大时注意性能影响
 */
public class CompareEngine {

    /**
     * 执行数据对比
     * 对主数据源和影子数据源的数据进行全面对比
     *
     * @param primary   主数据源对象
     * @param shadow    影子数据源对象
     * @param dataItems 数据项定义列表
     * @param results   存储对比结果的列表
     * @throws DataAccessException 数据访问异常
     */
    public static void compare(DataSource primary, DataSource shadow, List<DataItem> dataItems,
            ObservableList<CompareResult> results) throws DataAccessException {
        List<DataItem> uniqueItems = getUniqueItems(dataItems);
        CompareData compareData = getCompareData(primary, shadow);

        List<Map<String, Object>> shadowList = buildShadowList(
                compareData.shadowData(),
                compareData.shadowMapping(),
                dataItems);

        processPrimaryData(
                compareData.primaryData(),
                compareData.primaryMapping(),
                shadowList,
                dataItems,
                uniqueItems,
                results);

        processShadowOnlyData(shadowList, dataItems, results);
    }

    /**
     * 获取主键数据项
     * 从数据项列表中筛选出标记为唯一键的字段
     *
     * @param dataItems 数据项定义列表
     * @return 主键数据项列表
     */
    private static List<DataItem> getUniqueItems(List<DataItem> dataItems) {
        return dataItems.stream()
                .filter(DataItem::isUnique)
                .toList();
    }

    /**
     * 数据源数据和映射的封装类
     * 用于封装数据源的原始数据和字段映射关系
     *
     * @param primaryData    主数据源数据
     * @param primaryMapping 主数据源字段映射
     * @param shadowData     影子数据源数据
     * @param shadowMapping  影子数据源字段映射
     */
    private record CompareData(
            List<Map<String, Object>> primaryData,
            Map<String, String> primaryMapping,
            List<Map<String, Object>> shadowData,
            Map<String, String> shadowMapping) {
    }

    /**
     * 获取数据源数据和映射
     * 从两个数据源获取数据和字段映射信息
     *
     * @param primary 主数据源
     * @param shadow  影子数据源
     * @return 包含两个数据源数据和映射的封装对象
     * @throws DataAccessException 数据访问异常
     */
    private static CompareData getCompareData(DataSource primary, DataSource shadow) throws DataAccessException {
        return new CompareData(
                primary.acquireValues(),
                primary.getMappings(),
                shadow.acquireValues(),
                shadow.getMappings());
    }

    /**
     * 构建影子数据Map
     * 将影子数据源数据构建成以主键为索引的Map结构
     */
    private static List<Map<String, Object>> buildShadowList(
            List<Map<String, Object>> shadowData,
            Map<String, String> shadowMapping,
            List<DataItem> dataItems) {
        return shadowData.stream()
                .map(shadowRow -> mapDataSourceRow(shadowRow, shadowMapping, dataItems))
                .collect(Collectors.toList());
    }

    /**
     * 在影子数据中查找匹配的数据行
     * 
     * @param primaryObject 主数据行
     * @param shadowList    影子数据列表
     * @param uniqueItems   主键数据项列表
     * @return 匹配的影子数据行，如果未找到返回null
     */
    private static Map<String, Object> findMatchingShadowRow(
            Map<String, Object> primaryObject,
            List<Map<String, Object>> shadowList,
            List<DataItem> uniqueItems) {
        return shadowList.stream()
                .filter(shadowRow -> isUniqueKeysMatch(primaryObject, shadowRow, uniqueItems))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断两行数据的主键是否匹配
     * 
     * @param primaryRow  主数据行
     * @param shadowRow   影子数据行
     * @param uniqueItems 主键数据项列表
     * @return 如果所有主键值都匹配返回true，否则返回false
     */
    private static boolean isUniqueKeysMatch(
            Map<String, Object> primaryRow,
            Map<String, Object> shadowRow,
            List<DataItem> uniqueItems) {
        return uniqueItems.stream().allMatch(item -> {
            Object primaryValue = primaryRow.get(item.getCode());
            Object shadowValue = shadowRow.get(item.getCode());
            // 如果比较器为空,则使用Objects.equals进行比较
            return item.getComparator() == null ? false
                    : item.getComparator().equals(primaryValue, shadowValue);
        });
    }

    /**
     * 处理主数据源数据
     */
    private static void processPrimaryData(
            List<Map<String, Object>> primaryData,
            Map<String, String> primaryMapping,
            List<Map<String, Object>> shadowList,
            List<DataItem> dataItems,
            List<DataItem> uniqueItems,
            ObservableList<CompareResult> results) {

        for (Map<String, Object> primaryRow : primaryData) {
            Map<String, Object> primaryObject = mapDataSourceRow(primaryRow, primaryMapping, dataItems);
            Map<String, Object> shadowObject = findMatchingShadowRow(primaryObject, shadowList, uniqueItems);

            if (shadowObject != null) {
                shadowList.remove(shadowObject);
            }

            CompareResult result = compareDataRows(primaryObject, shadowObject, dataItems);
            results.add(result);
        }
    }

    /**
     * 映射数据源行数据
     * 将数据源原始字段映射到标准字段
     *
     * @param sourceRow 数据源行数据
     * @param mapping   字段映射关系
     * @param dataItems 数据项定义列表
     * @return 映射后的数据行
     */
    private static Map<String, Object> mapDataSourceRow(
            Map<String, Object> sourceRow,
            Map<String, String> mapping,
            List<DataItem> dataItems) {
        Map<String, Object> mappedRow = new HashMap<>();
        for (DataItem item : dataItems) {
            String mappedField = mapping.get(item.getCode());
            if (mappedField != null) {
                mappedRow.put(item.getCode(), sourceRow.get(mappedField));
            }
        }
        return mappedRow;
    }

    /**
     * 比较数据行
     * 对两个数据源的行数据进行字段级别的对比
     *
     * @param primaryObject 主数据源行数据
     * @param shadowObject  影子数据源行数据
     * @param dataItems     数据项定义列表
     * @return 行级别的对比结果
     */
    private static CompareResult compareDataRows(
            Map<String, Object> primaryObject,
            Map<String, Object> shadowObject,
            List<DataItem> dataItems) {
        CompareResult result = new CompareResult();

        for (DataItem item : dataItems) {
            CellResult cellResult = CellResult.create(
                    primaryObject.get(item.getCode()),
                    shadowObject != null ? shadowObject.get(item.getCode()) : null,
                    item,
                    false, // primaryObject 不可能为 null
                    shadowObject == null);
            result.putCellResult(item.getCode(), cellResult);
        }

        return result;
    }

    /**
     * 处理仅在影子数据源中存在的数据
     */
    private static void processShadowOnlyData(
            List<Map<String, Object>> shadowList,
            List<DataItem> dataItems,
            ObservableList<CompareResult> results) {
        for (Map<String, Object> shadowRow : shadowList) {
            CompareResult result = new CompareResult();
            for (DataItem item : dataItems) {
                Object shadowValue = shadowRow.get(item.getCode());
                CellResult cellResult = CellResult.create(null, shadowValue, item, true, false);
                result.putCellResult(item.getCode(), cellResult);
            }
            results.add(result);
        }
    }
}