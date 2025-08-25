package com.oneblue3.unicasetest.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * 缓存管理器
 * 演示文档中有Android依赖的工具测试：需要Context，使用Robolectric测试
 * 适用场景：文件缓存、配置管理、临时数据存储等
 */
public class CacheManager {
    
    private final Context context;
    private final SharedPreferences preferences;
    private static final String PREF_NAME = "app_cache_prefs";
    private static final String CACHE_DIR = "app_cache";
    
    public CacheManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * 保存用户配置
     * @param config 用户配置对象
     * @return 是否保存成功
     */
    public boolean saveUserConfig(UserConfig config) {
        if (config == null) {
            return false;
        }
        
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("theme", config.getTheme());
            editor.putString("language", config.getLanguage());
            editor.putBoolean("notification_enabled", config.isNotificationEnabled());
            editor.putInt("font_size", config.getFontSize());
            editor.putString("user_name", config.getUserName());
            
            return editor.commit();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取用户配置
     * @return 用户配置对象
     */
    public UserConfig getUserConfig() {
        return new UserConfig.Builder()
            .setTheme(preferences.getString("theme", "light"))
            .setLanguage(preferences.getString("language", "zh-CN"))
            .setNotificationEnabled(preferences.getBoolean("notification_enabled", true))
            .setFontSize(preferences.getInt("font_size", 14))
            .setUserName(preferences.getString("user_name", ""))
            .build();
    }
    
    /**
     * 缓存文件到内部存储
     * @param key 缓存键
     * @param data 数据
     * @return 是否成功
     */
    public boolean putFileCache(String key, byte[] data) {
        if (key == null || key.trim().isEmpty() || data == null) {
            return false;
        }
        
        try {
            File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
            if (!cacheDir.exists() && !cacheDir.mkdirs()) {
                return false;
            }
            
            File cacheFile = new File(cacheDir, key);
            try (FileOutputStream fos = new FileOutputStream(cacheFile)) {
                fos.write(data);
                fos.flush();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 从缓存获取文件数据
     * @param key 缓存键
     * @return 文件数据，不存在返回null
     */
    public byte[] getFileCache(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        try {
            File cacheFile = new File(context.getCacheDir(), CACHE_DIR + "/" + key);
            if (!cacheFile.exists()) {
                return null;
            }
            
            try (FileInputStream fis = new FileInputStream(cacheFile)) {
                byte[] data = new byte[(int) cacheFile.length()];
                fis.read(data);
                return data;
            }
        } catch (IOException e) {
            return null;
        }
    }
    
    /**
     * 带过期时间的缓存
     * @param key 缓存键
     * @param data 数据
     * @param expireTimeMs 过期时间（毫秒）
     * @return 是否成功
     */
    public boolean putWithExpiry(String key, String data, long expireTimeMs) {
        if (key == null || data == null) {
            return false;
        }
        
        long expireTime = System.currentTimeMillis() + expireTimeMs;
        
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cache_" + key, data);
        editor.putLong("expire_" + key, expireTime);
        
        return editor.commit();
    }
    
    /**
     * 获取缓存数据，检查过期时间
     * @param key 缓存键
     * @return 数据，过期或不存在返回null
     */
    public String get(String key) {
        if (key == null) {
            return null;
        }
        
        long expireTime = preferences.getLong("expire_" + key, 0);
        if (expireTime > 0 && System.currentTimeMillis() > expireTime) {
            // 已过期，清除数据
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove("cache_" + key);
            editor.remove("expire_" + key);
            editor.apply();
            return null;
        }
        
        return preferences.getString("cache_" + key, null);
    }
    
    /**
     * 清除过期缓存
     * @return 清除的缓存项数量
     */
    public int clearExpiredCache() {
        int removedCount = 0;
        Map<String, ?> allPrefs = preferences.getAll();
        SharedPreferences.Editor editor = preferences.edit();
        
        long currentTime = System.currentTimeMillis();
        
        for (String key : allPrefs.keySet()) {
            if (key.startsWith("expire_")) {
                String cacheKey = key.substring(7); // 移除"expire_"前缀
                long expireTime = preferences.getLong(key, 0);
                
                if (expireTime > 0 && currentTime > expireTime) {
                    editor.remove("cache_" + cacheKey);
                    editor.remove(key);
                    removedCount++;
                }
            }
        }
        
        editor.apply();
        return removedCount;
    }
    
    /**
     * 获取缓存大小
     * @return 缓存大小（字节）
     */
    public long getCacheSize() {
        File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
        return calculateDirectorySize(cacheDir);
    }
    
    /**
     * 清空所有缓存
     * @return 是否成功
     */
    public boolean clearAllCache() {
        try {
            // 清空SharedPreferences缓存
            preferences.edit().clear().apply();
            
            // 清空文件缓存
            File cacheDir = new File(context.getCacheDir(), CACHE_DIR);
            deleteDirectory(cacheDir);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return 0;
        }
        
        long size = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    size += calculateDirectorySize(file);
                } else {
                    size += file.length();
                }
            }
        }
        return size;
    }
    
    /**
     * 删除目录及其内容
     */
    private boolean deleteDirectory(File directory) {
        if (!directory.exists()) {
            return true;
        }
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        
        return directory.delete();
    }
    
    /**
     * 用户配置类
     */
    public static class UserConfig {
        private final String theme;
        private final String language;
        private final boolean notificationEnabled;
        private final int fontSize;
        private final String userName;
        
        private UserConfig(Builder builder) {
            this.theme = builder.theme;
            this.language = builder.language;
            this.notificationEnabled = builder.notificationEnabled;
            this.fontSize = builder.fontSize;
            this.userName = builder.userName;
        }
        
        public String getTheme() { return theme; }
        public String getLanguage() { return language; }
        public boolean isNotificationEnabled() { return notificationEnabled; }
        public int getFontSize() { return fontSize; }
        public String getUserName() { return userName; }
        
        public static class Builder {
            private String theme = "light";
            private String language = "zh-CN";
            private boolean notificationEnabled = true;
            private int fontSize = 14;
            private String userName = "";
            
            public Builder setTheme(String theme) {
                this.theme = theme;
                return this;
            }
            
            public Builder setLanguage(String language) {
                this.language = language;
                return this;
            }
            
            public Builder setNotificationEnabled(boolean enabled) {
                this.notificationEnabled = enabled;
                return this;
            }
            
            public Builder setFontSize(int fontSize) {
                this.fontSize = fontSize;
                return this;
            }
            
            public Builder setUserName(String userName) {
                this.userName = userName;
                return this;
            }
            
            public UserConfig build() {
                return new UserConfig(this);
            }
        }
    }
}