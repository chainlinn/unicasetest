package com.oneblue3.unicasetest.tools;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.oneblue3.unicasetest.tools.CacheManager.UserConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * 缓存管理器测试类
 * 演示文档中Android依赖工具测试：使用Robolectric模拟Android环境
 * 测试覆盖：文件缓存、配置管理、过期处理等
 */
@RunWith(AndroidJUnit4.class)
@Config(sdk = {29})
public class CacheManagerTest {
    
    private Context context;
    private CacheManager cacheManager;
    
    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        cacheManager = new CacheManager(context);
    }
    
    @After
    public void tearDown() {
        // 清理测试数据
        cacheManager.clearAllCache();
    }
    
    @Test
    public void saveUserConfig_validConfig_shouldPersistCorrectly() {
        UserConfig config = new UserConfig.Builder()
            .setTheme("dark")
            .setLanguage("zh-CN")
            .setNotificationEnabled(true)
            .setFontSize(16)
            .setUserName("测试用户")
            .build();
        
        boolean result = cacheManager.saveUserConfig(config);
        
        assertThat(result).isTrue();
        
        UserConfig retrieved = cacheManager.getUserConfig();
        assertThat(retrieved.getTheme()).isEqualTo("dark");
        assertThat(retrieved.getLanguage()).isEqualTo("zh-CN");
        assertThat(retrieved.isNotificationEnabled()).isTrue();
        assertThat(retrieved.getFontSize()).isEqualTo(16);
        assertThat(retrieved.getUserName()).isEqualTo("测试用户");
    }
    
    @Test
    public void saveUserConfig_nullConfig_shouldReturnFalse() {
        boolean result = cacheManager.saveUserConfig(null);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void getUserConfig_noSavedConfig_shouldReturnDefaults() {
        UserConfig config = cacheManager.getUserConfig();
        
        assertThat(config.getTheme()).isEqualTo("light");
        assertThat(config.getLanguage()).isEqualTo("zh-CN");
        assertThat(config.isNotificationEnabled()).isTrue();
        assertThat(config.getFontSize()).isEqualTo(14);
        assertThat(config.getUserName()).isEqualTo("");
    }
    
    @Test
    public void putFileCache_validData_shouldCacheSuccessfully() {
        String key = "test_file";
        byte[] data = "Test file content".getBytes();
        
        boolean result = cacheManager.putFileCache(key, data);
        
        assertThat(result).isTrue();
        
        byte[] retrieved = cacheManager.getFileCache(key);
        assertThat(retrieved).isEqualTo(data);
        assertThat(new String(retrieved)).isEqualTo("Test file content");
    }
    
    @Test
    public void putFileCache_nullKey_shouldReturnFalse() {
        byte[] data = "Test data".getBytes();
        
        boolean result = cacheManager.putFileCache(null, data);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void putFileCache_emptyKey_shouldReturnFalse() {
        byte[] data = "Test data".getBytes();
        
        boolean result = cacheManager.putFileCache("", data);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void putFileCache_nullData_shouldReturnFalse() {
        boolean result = cacheManager.putFileCache("test_key", null);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void getFileCache_nonExistentKey_shouldReturnNull() {
        byte[] result = cacheManager.getFileCache("non_existent_key");
        
        assertThat(result).isNull();
    }
    
    @Test
    public void getFileCache_nullKey_shouldReturnNull() {
        byte[] result = cacheManager.getFileCache(null);
        
        assertThat(result).isNull();
    }
    
    @Test
    public void putWithExpiry_validData_shouldCacheWithExpiry() {
        String key = "expiry_test";
        String data = "Test data with expiry";
        long expireTime = 5000; // 5秒过期
        
        boolean result = cacheManager.putWithExpiry(key, data, expireTime);
        
        assertThat(result).isTrue();
        
        String retrieved = cacheManager.get(key);
        assertThat(retrieved).isEqualTo(data);
    }
    
    @Test
    public void putWithExpiry_nullKey_shouldReturnFalse() {
        boolean result = cacheManager.putWithExpiry(null, "data", 1000);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void putWithExpiry_nullData_shouldReturnFalse() {
        boolean result = cacheManager.putWithExpiry("key", null, 1000);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void get_expiredData_shouldReturnNull() throws InterruptedException {
        String key = "expired_test";
        String data = "Expired data";
        long expireTime = 10; // 10毫秒过期
        
        cacheManager.putWithExpiry(key, data, expireTime);
        
        // 等待过期
        Thread.sleep(20);
        
        String retrieved = cacheManager.get(key);
        assertThat(retrieved).isNull();
    }
    
    @Test
    public void get_nonExpiredData_shouldReturnData() {
        String key = "valid_test";
        String data = "Valid data";
        long expireTime = 30000; // 30秒过期
        
        cacheManager.putWithExpiry(key, data, expireTime);
        
        String retrieved = cacheManager.get(key);
        assertThat(retrieved).isEqualTo(data);
    }
    
    @Test
    public void get_nonExistentKey_shouldReturnNull() {
        String result = cacheManager.get("non_existent");
        
        assertThat(result).isNull();
    }
    
    @Test
    public void clearExpiredCache_mixedData_shouldRemoveOnlyExpired() throws InterruptedException {
        // 添加即将过期的数据
        cacheManager.putWithExpiry("expired1", "data1", 10);
        cacheManager.putWithExpiry("expired2", "data2", 10);
        
        // 添加不过期的数据
        cacheManager.putWithExpiry("valid1", "data3", 30000);
        cacheManager.putWithExpiry("valid2", "data4", 30000);
        
        // 等待部分数据过期
        Thread.sleep(20);
        
        int removedCount = cacheManager.clearExpiredCache();
        
        assertThat(removedCount).isEqualTo(2);
        
        // 验证过期数据被清除
        assertThat(cacheManager.get("expired1")).isNull();
        assertThat(cacheManager.get("expired2")).isNull();
        
        // 验证有效数据仍存在
        assertThat(cacheManager.get("valid1")).isEqualTo("data3");
        assertThat(cacheManager.get("valid2")).isEqualTo("data4");
    }
    
    @Test
    public void clearExpiredCache_noExpiredData_shouldReturnZero() {
        cacheManager.putWithExpiry("valid1", "data1", 30000);
        cacheManager.putWithExpiry("valid2", "data2", 30000);
        
        int removedCount = cacheManager.clearExpiredCache();
        
        assertThat(removedCount).isEqualTo(0);
    }
    
    @Test
    public void getCacheSize_emptyCache_shouldReturnZero() {
        long size = cacheManager.getCacheSize();
        
        assertThat(size).isEqualTo(0);
    }
    
    @Test
    public void getCacheSize_withFiles_shouldReturnPositiveSize() {
        byte[] data = "Test cache size data".getBytes();
        cacheManager.putFileCache("size_test", data);
        
        long size = cacheManager.getCacheSize();
        
        assertThat(size).isGreaterThan(0);
        assertThat(size).isAtLeast(data.length);
    }
    
    @Test
    public void clearAllCache_mixedCaches_shouldClearEverything() {
        // 添加文件缓存
        cacheManager.putFileCache("file1", "File data 1".getBytes());
        cacheManager.putFileCache("file2", "File data 2".getBytes());
        
        // 添加配置缓存
        UserConfig config = new UserConfig.Builder()
            .setTheme("dark")
            .setUserName("Test User")
            .build();
        cacheManager.saveUserConfig(config);
        
        // 添加过期缓存
        cacheManager.putWithExpiry("temp1", "Temp data 1", 30000);
        cacheManager.putWithExpiry("temp2", "Temp data 2", 30000);
        
        boolean result = cacheManager.clearAllCache();
        
        assertThat(result).isTrue();
        
        // 验证所有缓存都被清空
        assertThat(cacheManager.getFileCache("file1")).isNull();
        assertThat(cacheManager.getFileCache("file2")).isNull();
        assertThat(cacheManager.get("temp1")).isNull();
        assertThat(cacheManager.get("temp2")).isNull();
        assertThat(cacheManager.getCacheSize()).isEqualTo(0);
        
        // 验证配置被重置为默认值
        UserConfig defaultConfig = cacheManager.getUserConfig();
        assertThat(defaultConfig.getTheme()).isEqualTo("light");
        assertThat(defaultConfig.getUserName()).isEqualTo("");
    }
    
    @Test
    public void userConfigBuilder_allFields_shouldBuildCorrectly() {
        UserConfig config = new UserConfig.Builder()
            .setTheme("custom_theme")
            .setLanguage("en-US")
            .setNotificationEnabled(false)
            .setFontSize(18)
            .setUserName("Builder Test User")
            .build();
        
        assertThat(config.getTheme()).isEqualTo("custom_theme");
        assertThat(config.getLanguage()).isEqualTo("en-US");
        assertThat(config.isNotificationEnabled()).isFalse();
        assertThat(config.getFontSize()).isEqualTo(18);
        assertThat(config.getUserName()).isEqualTo("Builder Test User");
    }
    
    @Test
    public void userConfigBuilder_defaults_shouldReturnDefaultValues() {
        UserConfig config = new UserConfig.Builder().build();
        
        assertThat(config.getTheme()).isEqualTo("light");
        assertThat(config.getLanguage()).isEqualTo("zh-CN");
        assertThat(config.isNotificationEnabled()).isTrue();
        assertThat(config.getFontSize()).isEqualTo(14);
        assertThat(config.getUserName()).isEqualTo("");
    }
}