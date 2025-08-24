package com.oneblue3.unicasetest.network;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 网络工具类
 * 演示组件测试：需要HTTP请求，使用MockWebServer进行测试
 * 包含GET、POST请求及错误处理逻辑
 */
public class NetworkUtil {
    
    private final OkHttpClient httpClient;
    private final String baseUrl;
    
    public NetworkUtil(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * 获取用户数据
     * @param userId 用户ID
     * @return 用户数据
     * @throws IOException 网络异常
     */
    public UserData fetchUserData(int userId) throws IOException {
        String url = baseUrl + "/users/" + userId;
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return parseUserData(responseBody);
        }
    }
    
    /**
     * 上传用户数据
     * @param userData 用户数据
     * @return 上传结果
     * @throws IOException 网络异常
     */
    public UploadResult uploadUserData(UserData userData) throws IOException {
        String url = baseUrl + "/users";
        String json = userDataToJson(userData);
        
        RequestBody body = RequestBody.create(
            json, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("上传失败: " + response.code());
            }
            
            String responseBody = response.body().string();
            return parseUploadResult(responseBody);
        }
    }
    
    /**
     * 异步获取用户数据
     * @param userId 用户ID
     * @param callback 回调接口
     */
    public void fetchUserDataAsync(int userId, NetworkCallback<UserData> callback) {
        String url = baseUrl + "/users/" + userId;
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        callback.onError(new IOException("请求失败: " + response.code()));
                        return;
                    }
                    
                    String responseBody = response.body().string();
                    UserData userData = parseUserData(responseBody);
                    callback.onSuccess(userData);
                } catch (Exception e) {
                    callback.onError(e);
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * 检查网络连接
     * @return 是否连接成功
     */
    public boolean checkConnection() {
        try {
            String url = baseUrl + "/health";
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 解析用户数据JSON
     */
    private UserData parseUserData(String json) {
        // 简化的JSON解析（实际项目中应使用Gson或类似库）
        // 示例JSON: {"id":1,"name":"张三","status":"active"}
        try {
            int id = extractIntFromJson(json, "id");
            String name = extractStringFromJson(json, "name");
            String status = extractStringFromJson(json, "status");
            return new UserData(id, name, status);
        } catch (Exception e) {
            throw new RuntimeException("JSON解析失败", e);
        }
    }
    
    /**
     * 用户数据转JSON
     */
    private String userDataToJson(UserData userData) {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"status\":\"%s\"}",
            userData.getId(),
            userData.getName(),
            userData.getStatus()
        );
    }
    
    /**
     * 解析上传结果
     */
    private UploadResult parseUploadResult(String json) {
        try {
            String status = extractStringFromJson(json, "status");
            String message = extractStringFromJson(json, "message");
            return new UploadResult("success".equals(status), message);
        } catch (Exception e) {
            return new UploadResult(false, "解析响应失败");
        }
    }
    
    /**
     * 从JSON中提取整数值（简化实现）
     */
    private int extractIntFromJson(String json, String key) {
        String pattern = "\"" + key + "\":\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new RuntimeException("未找到键: " + key);
    }
    
    /**
     * 从JSON中提取字符串值（简化实现）
     */
    private String extractStringFromJson(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        throw new RuntimeException("未找到键: " + key);
    }
    
    /**
     * 用户数据类
     */
    public static class UserData {
        private final int id;
        private final String name;
        private final String status;
        
        public UserData(int id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
    }
    
    /**
     * 上传结果类
     */
    public static class UploadResult {
        private final boolean success;
        private final String message;
        
        public UploadResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    /**
     * 网络回调接口
     */
    public interface NetworkCallback<T> {
        void onSuccess(T data);
        void onError(Exception e);
    }
}