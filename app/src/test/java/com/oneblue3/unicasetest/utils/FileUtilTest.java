package com.oneblue3.unicasetest.utils;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oneblue3.unicasetest.utils.FileUtil.AppInfo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * 文件工具类Robolectric测试
 * 演示文档中组件测试的实践：
 * - 使用Robolectric在JVM中模拟Android环境
 * - 测试依赖Android Context的组件
 * - 快速执行（1-3秒），保持较高的测试效率
 * - 验证文件操作、资源访问等Android相关功能
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {29})
public class FileUtilTest {
    
    private Context context;
    private FileUtil fileUtil;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        fileUtil = new FileUtil(context);
        
        // 清理测试环境
        cleanUpTestFiles();
    }
    
    @After
    public void tearDown() {
        // 清理测试产生的文件
        cleanUpTestFiles();
    }
    
    @Test
    public void writeToInternalStorage_validData_shouldSuccess() {
        // 测试文件写入功能
        String filename = "./test_config.json";
        String content = "{\"version\":\"1.0\",\"debug\":true}";
        
        boolean result = fileUtil.writeToInternalStorage(filename, content);
        
        assertTrue("文件写入应该成功", result);
        assertTrue("文件应该存在", fileUtil.fileExists(filename));
        assertEquals("内容应该匹配", content, 
            fileUtil.readFromInternalStorage(filename));
    }
    
    @Test
    public void writeToInternalStorage_emptyContent_shouldSuccess() {
        // 测试空内容写入
        String filename = "empty_file.txt";
        String content = "";
        
        boolean result = fileUtil.writeToInternalStorage(filename, content);
        
        assertThat(result).isTrue();
        assertThat(fileUtil.fileExists(filename)).isTrue();
        assertThat(fileUtil.readFromInternalStorage(filename)).isEqualTo(content);
    }
    
    @Test
    public void readFromInternalStorage_nonExistentFile_shouldReturnNull() {
        // 测试读取不存在的文件
        String content = fileUtil.readFromInternalStorage("non_existent_file.txt");
        
        assertThat(content).isNull();
    }
    
    @Test
    public void getFileSize_existingFile_shouldReturnCorrectSize() {
        // 测试获取文件大小
        String filename = "size_test.txt";
        String content = "Hello, World!";
        
        fileUtil.writeToInternalStorage(filename, content);
        long size = fileUtil.getFileSize(filename);
        
        assertThat(size).isEqualTo(content.getBytes().length);
    }
    
    @Test
    public void getFileSize_nonExistentFile_shouldReturnMinusOne() {
        // 测试边界条件
        long size = fileUtil.getFileSize("non_existent.txt");
        assertThat(size).isEqualTo(-1);
    }
    
    @Test
    public void deleteFile_existingFile_shouldReturnTrue() {
        // 测试文件删除
        String filename = "delete_test.txt";
        String content = "To be deleted";
        
        fileUtil.writeToInternalStorage(filename, content);
        assertThat(fileUtil.fileExists(filename)).isTrue();
        
        boolean deleted = fileUtil.deleteFile(filename);
        
        assertThat(deleted).isTrue();
        assertThat(fileUtil.fileExists(filename)).isFalse();
    }
    
    @Test
    public void deleteFile_nonExistentFile_shouldReturnFalse() {
        // 测试删除不存在的文件
        boolean deleted = fileUtil.deleteFile("non_existent.txt");
        
        assertThat(deleted).isFalse();
    }
    
    @Test
    public void getApplicationInfo_shouldReturnValidInfo() {
        // 测试Android资源访问 - 这是Robolectric测试的核心价值
        AppInfo info = fileUtil.getApplicationInfo();
        
        assertNotNull("应用信息不应为空", info);
        assertThat(info.getPackageName()).isEqualTo("com.oneblue3.unicasetest");
        assertThat(info.getVersionName()).isNotEmpty();
        assertThat(info.getTargetSdk()).isAtLeast(29);
        assertThat(info.getVersionCode()).isAtLeast(1);
    }
    
    @Test
    public void getCacheSize_shouldReturnNonNegativeValue() {
        // 测试缓存大小获取
        long cacheSize = fileUtil.getCacheSize();
        
        assertThat(cacheSize).isAtLeast(0L);
    }
    
    @Test
    public void clearCache_shouldSucceed() {
        // 测试缓存清理
        boolean result = fileUtil.clearCache();
        
        assertThat(result).isTrue();
    }
    
    @Test
    public void fileOperations_multipleFiles_shouldWorkCorrectly() {
        // 测试多文件操作场景
        String[] filenames = {"file1.txt", "file2.json", "file3.xml"};
        String[] contents = {"Content 1", "{\"key\":\"value\"}", "<root></root>"};
        
        // 写入多个文件
        for (int i = 0; i < filenames.length; i++) {
            boolean written = fileUtil.writeToInternalStorage(filenames[i], contents[i]);
            assertThat(written).isTrue();
        }
        
        // 验证所有文件都存在
        for (String filename : filenames) {
            assertThat(fileUtil.fileExists(filename)).isTrue();
        }
        
        // 验证文件内容
        for (int i = 0; i < filenames.length; i++) {
            String readContent = fileUtil.readFromInternalStorage(filenames[i]);
            assertThat(readContent).isEqualTo(contents[i]);
        }
        
        // 验证文件大小
        for (int i = 0; i < filenames.length; i++) {
            long size = fileUtil.getFileSize(filenames[i]);
            assertThat(size).isEqualTo(contents[i].getBytes().length);
        }
    }
    
    @Test
    public void writeAndReadLargeContent_shouldWorkCorrectly() {
        // 测试大文件处理
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeContent.append("这是第").append(i).append("行内容\n");
        }
        
        String filename = "large_file.txt";
        String content = largeContent.toString();
        
        boolean written = fileUtil.writeToInternalStorage(filename, content);
        assertThat(written).isTrue();
        
        String readContent = fileUtil.readFromInternalStorage(filename);
        assertThat(readContent).isEqualTo(content);
        
        long size = fileUtil.getFileSize(filename);
        assertThat(size).isEqualTo(content.getBytes().length);
    }
    
    /**
     * 清理测试文件
     */
    private void cleanUpTestFiles() {
        String[] testFiles = {
            "test_config.json",
            "empty_file.txt", 
            "size_test.txt",
            "delete_test.txt",
            "file1.txt",
            "file2.json", 
            "file3.xml",
            "large_file.txt"
        };
        
        for (String filename : testFiles) {
            fileUtil.deleteFile(filename);
        }
    }
}