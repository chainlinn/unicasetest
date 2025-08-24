package com.oneblue3.unicasetest.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件工具类
 * 演示组件测试：依赖Android Context，需要Robolectric模拟环境
 * 包含文件操作、应用信息获取等Android框架相关功能
 */
public class FileUtil {
    
    private final Context context;
    
    public FileUtil(Context context) {
        this.context = context;
    }
    
    /**
     * 写入数据到内部存储
     * @param filename 文件名
     * @param content 文件内容
     * @return 是否成功
     */
    public boolean writeToInternalStorage(String filename, String content) {
        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 从内部存储读取数据
     * @param filename 文件名
     * @return 文件内容，失败时返回null
     */
    public String readFromInternalStorage(String filename) {
        // 先检查文件是否存在，避免FileNotFoundException
        if (!fileExists(filename)) {
            return null;
        }
        
        try (FileInputStream fis = context.openFileInput(filename)) {
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            return new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 检查文件是否存在
     * @param filename 文件名
     * @return 是否存在
     */
    public boolean fileExists(String filename) {
        File file = new File(context.getFilesDir(), filename);
        return file.exists();
    }
    
    /**
     * 获取文件大小
     * @param filename 文件名
     * @return 文件大小，文件不存在时返回-1
     */
    public long getFileSize(String filename) {
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            return file.length();
        }
        return -1;
    }
    
    /**
     * 删除文件
     * @param filename 文件名
     * @return 是否成功删除
     */
    public boolean deleteFile(String filename) {
        return context.deleteFile(filename);
    }
    
    /**
     * 获取应用信息
     * @return 应用信息对象
     */
    public AppInfo getApplicationInfo() {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            String versionName = pm.getPackageInfo(context.getPackageName(), 0).versionName;
            int versionCode = pm.getPackageInfo(context.getPackageName(), 0).versionCode;
            
            return new AppInfo(
                context.getPackageName(),
                versionName,
                versionCode,
                appInfo.targetSdkVersion,
                appInfo.minSdkVersion
            );
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获取缓存目录大小
     * @return 缓存目录大小（字节）
     */
    public long getCacheSize() {
        return calculateDirectorySize(context.getCacheDir());
    }
    
    /**
     * 清理缓存
     * @return 是否成功
     */
    public boolean clearCache() {
        return deleteDirectory(context.getCacheDir());
    }
    
    /**
     * 计算目录大小
     */
    private long calculateDirectorySize(File directory) {
        long size = 0;
        if (directory != null && directory.exists()) {
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
        }
        return size;
    }
    
    /**
     * 递归删除目录
     */
    private boolean deleteDirectory(File directory) {
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * 应用信息数据类
     */
    public static class AppInfo {
        private final String packageName;
        private final String versionName;
        private final int versionCode;
        private final int targetSdk;
        private final int minSdk;
        
        public AppInfo(String packageName, String versionName, int versionCode, int targetSdk, int minSdk) {
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
            this.targetSdk = targetSdk;
            this.minSdk = minSdk;
        }
        
        public String getPackageName() { return packageName; }
        public String getVersionName() { return versionName; }
        public int getVersionCode() { return versionCode; }
        public int getTargetSdk() { return targetSdk; }
        public int getMinSdk() { return minSdk; }
    }
}