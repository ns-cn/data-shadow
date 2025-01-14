package com.tangyujun.datashadow.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tangyujun.datashadow.datasource.DataSourceMemory;

class ShadowSchemaManagerTest {

    @TempDir
    Path tempDir;

    private ShadowSchemaManager manager;
    private String testFilePath;

    @BeforeEach
    void setUp() {
        manager = new ShadowSchemaManager();
        testFilePath = tempDir.resolve("test_schema.dat").toString();

        // 准备测试数据
        List<DataItem> items = new ArrayList<>();
        DataItem item = new DataItem();
        item.setCode("test_code");
        items.add(item);

        DataSourceMemory primarySource = new DataSourceMemory();

        DataSourceMemory shadowSource = new DataSourceMemory();

        manager.setDataItems(items);
        manager.setPrimarySource(primarySource);
        manager.setShadowSource(shadowSource);
    }

    @AfterEach
    void tearDown() {
        // 清理测试文件
        new File(testFilePath).delete();
    }

    @Test
    void testExportAndImport() throws IOException, ClassNotFoundException {
        // 测试导出
        manager.export(testFilePath);
        assertTrue(Files.exists(Path.of(testFilePath)), "导出的文件应该存在");

        // 测试导入
        ShadowSchemaManager importedManager = new ShadowSchemaManager();
        importedManager.importFrom(testFilePath);

        // 验证导入的数据
        assertNotNull(importedManager.getDataItems(), "数据项不应为空");
        assertEquals(1, importedManager.getDataItems().size(), "应有1个数据项");
        assertEquals("test_code", importedManager.getDataItems().get(0).getCode(), "数据项代码应匹配");

        assertTrue(importedManager.getPrimarySource() instanceof DataSourceMemory, "主数据源类型应匹配");
        DataSourceMemory expectedPrimary = new DataSourceMemory();
        assertEquals(expectedPrimary, importedManager.getPrimarySource(), "主数据源应匹配");

        assertTrue(importedManager.getShadowSource() instanceof DataSourceMemory, "影子数据源类型应匹配");
        DataSourceMemory expectedShadow = new DataSourceMemory();
        assertEquals(expectedShadow, importedManager.getShadowSource(), "影子数据源应匹配");
    }

    /**
     * 测试导出到无效路径
     */
    @Test
    void testExportToInvalidPath() {
        String invalidPath = "/invalid/path/test.dat";
        try {
            manager.export(invalidPath);
            fail("导出到无效路径应抛出IOException");
        } catch (IOException e) {
            // 预期的异常,测试通过
        }
    }

    /**
     * 测试从不存在的文件导入
     */
    @Test
    void testImportFromNonExistentFile() {
        String nonExistentFile = tempDir.resolve("non_existent.dat").toString();
        try {
            manager.importFrom(nonExistentFile);
            fail("从不存在的文件导入应抛出IOException");
        } catch (IOException | ClassNotFoundException e) {
            // 预期的异常,测试通过
        }
    }
}