package com.oneblue3.unicasetest.logic;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * API客户端
 * 演示文档中网络依赖逻辑测试：HTTP客户端封装，使用MockWebServer进行测试
 * 适用场景：API调用、网络重试、文件上传下载等
 */
public class ApiClient {
    
    private final OkHttpClient httpClient;
    private final String baseUrl;
    private final int maxRetries;
    
    public ApiClient(String baseUrl) {
        this(baseUrl, 3);
    }
    
    public ApiClient(String baseUrl, int maxRetries) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.maxRetries = maxRetries;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     * @throws IOException 网络异常
     */
    public UserInfo getUserInfo(String userId) throws IOException {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        String url = baseUrl + "/users/" + userId;
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }
            
            String responseBody = response.body().string();
            return parseUserInfo(responseBody);
        }
    }
    
    /**
     * 创建用户
     * @param userRequest 用户创建请求
     * @return 创建结果
     * @throws IOException 网络异常
     */
    public CreateUserResult createUser(CreateUserRequest userRequest) throws IOException {
        if (userRequest == null) {
            throw new IllegalArgumentException("用户请求不能为空");
        }
        
        String url = baseUrl + "/users";
        String jsonBody = userRequestToJson(userRequest);
        
        RequestBody body = RequestBody.create(
            jsonBody, MediaType.get("application/json; charset=utf-8"));
        
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleErrorResponse(response);
            }
            
            String responseBody = response.body().string();
            return parseCreateUserResult(responseBody);
        }
    }
    
    /**
     * 带重试的文件上传
     * @param file 文件对象
     * @param callback 进度回调
     * @return 上传结果
     */
    public UploadResult uploadFileWithRetry(FileData file, UploadProgressCallback callback) {
        if (file == null) {
            return new UploadResult(false, null, "文件不能为空");
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (callback != null) {
                    callback.onProgress(0);
                }
                
                String url = baseUrl + "/files/upload";
                RequestBody body = RequestBody.create(
                    file.getData(), MediaType.get("application/octet-stream"));
                
                Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Content-Type", "application/octet-stream")
                    .addHeader("X-File-Name", file.getName())
                    .addHeader("X-File-Size", String.valueOf(file.getSize()))
                    .build();
                
                try (Response response = httpClient.newCall(request).execute()) {
                    if (callback != null) {
                        callback.onProgress(100);
                    }
                    
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        UploadResult result = parseUploadResult(responseBody);
                        
                        if (callback != null) {
                            callback.onSuccess(result.getFileId());
                        }
                        
                        return result;
                    } else {
                        throw new IOException("上传失败: " + response.code());
                    }
                }
                
            } catch (IOException e) {
                lastException = e;
                
                if (callback != null) {
                    callback.onRetry(attempt, e.getMessage());
                }
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000 * attempt); // 指数退避
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        if (callback != null) {
            callback.onError(lastException);
        }
        
        return new UploadResult(false, null, 
            "上传失败，已重试 " + maxRetries + " 次: " + lastException.getMessage());
    }
    
    /**
     * 异步获取用户列表
     * @param page 页码
     * @param pageSize 页大小
     * @param callback 回调
     */
    public void getUserListAsync(int page, int pageSize, ApiCallback<UserListResult> callback) {
        if (page < 1 || pageSize < 1) {
            callback.onError(new IllegalArgumentException("页码和页大小必须大于0"));
            return;
        }
        
        String url = String.format("%s/users?page=%d&pageSize=%d", baseUrl, page, pageSize);
        Request request = new Request.Builder()
            .url(url)
            .get()
            .addHeader("Accept", "application/json")
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
                    UserListResult result = parseUserListResult(responseBody);
                    callback.onSuccess(result);
                    
                } catch (Exception e) {
                    callback.onError(e);
                } finally {
                    response.close();
                }
            }
        });
    }
    
    /**
     * 检查API健康状态
     * @return 是否健康
     */
    public boolean isApiHealthy() {
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
     * 处理错误响应
     */
    private void handleErrorResponse(Response response) throws IOException {
        String errorMessage = "请求失败: " + response.code();
        
        if (response.body() != null) {
            try {
                String errorBody = response.body().string();
                if (!errorBody.isEmpty()) {
                    errorMessage += " - " + errorBody;
                }
            } catch (IOException e) {
                // 忽略解析错误
            }
        }
        
        throw new IOException(errorMessage);
    }
    
    /**
     * 解析用户信息JSON
     */
    private UserInfo parseUserInfo(String json) {
        try {
            String id = extractStringFromJson(json, "id");
            String name = extractStringFromJson(json, "name");
            String email = extractStringFromJson(json, "email");
            String status = extractStringFromJson(json, "status");
            
            return new UserInfo(id, name, email, status);
        } catch (Exception e) {
            throw new RuntimeException("解析用户信息失败", e);
        }
    }
    
    /**
     * 用户请求转JSON
     */
    private String userRequestToJson(CreateUserRequest request) {
        return String.format(
            "{\"name\":\"%s\",\"email\":\"%s\",\"phone\":\"%s\"}",
            request.getName(), request.getEmail(), request.getPhone()
        );
    }
    
    /**
     * 解析创建用户结果
     */
    private CreateUserResult parseCreateUserResult(String json) {
        try {
            String userId = extractStringFromJson(json, "userId");
            String status = extractStringFromJson(json, "status");
            
            return new CreateUserResult("success".equals(status), userId, 
                "success".equals(status) ? "用户创建成功" : "用户创建失败");
        } catch (Exception e) {
            return new CreateUserResult(false, null, "解析响应失败");
        }
    }
    
    /**
     * 解析上传结果
     */
    private UploadResult parseUploadResult(String json) {
        try {
            String fileId = extractStringFromJson(json, "fileId");
            String status = extractStringFromJson(json, "status");
            
            return new UploadResult("uploaded".equals(status), fileId, 
                "uploaded".equals(status) ? "上传成功" : "上传失败");
        } catch (Exception e) {
            return new UploadResult(false, null, "解析响应失败");
        }
    }
    
    /**
     * 解析用户列表结果
     */
    private UserListResult parseUserListResult(String json) {
        // 简化实现，实际项目中应使用JSON库
        try {
            String totalCountStr = extractStringFromJson(json, "totalCount");
            int totalCount = Integer.parseInt(totalCountStr);
            
            return new UserListResult(totalCount, "解析成功");
        } catch (Exception e) {
            return new UserListResult(0, "解析失败");
        }
    }
    
    /**
     * 从JSON中提取字符串值（简化实现）
     */
    private String extractStringFromJson(String json, String key) {
        String pattern = "\"" + key + "\":\\s*\"([^\"]+)\"";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        throw new RuntimeException("未找到键: " + key);
    }
    
    // 数据类定义
    public static class UserInfo {
        private final String id;
        private final String name;
        private final String email;
        private final String status;
        
        public UserInfo(String id, String name, String email, String status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.status = status;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
    }
    
    public static class CreateUserRequest {
        private final String name;
        private final String email;
        private final String phone;
        
        public CreateUserRequest(String name, String email, String phone) {
            this.name = name;
            this.email = email;
            this.phone = phone;
        }
        
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }
    
    public static class CreateUserResult {
        private final boolean success;
        private final String userId;
        private final String message;
        
        public CreateUserResult(boolean success, String userId, String message) {
            this.success = success;
            this.userId = userId;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getUserId() { return userId; }
        public String getMessage() { return message; }
    }
    
    public static class FileData {
        private final String name;
        private final byte[] data;
        private final long size;
        
        public FileData(String name, byte[] data) {
            this.name = name;
            this.data = data;
            this.size = data != null ? data.length : 0;
        }
        
        public String getName() { return name; }
        public byte[] getData() { return data; }
        public long getSize() { return size; }
    }
    
    public static class UploadResult {
        private final boolean success;
        private final String fileId;
        private final String message;
        
        public UploadResult(boolean success, String fileId, String message) {
            this.success = success;
            this.fileId = fileId;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public String getFileId() { return fileId; }
        public String getMessage() { return message; }
    }
    
    public static class UserListResult {
        private final int totalCount;
        private final String message;
        
        public UserListResult(int totalCount, String message) {
            this.totalCount = totalCount;
            this.message = message;
        }
        
        public int getTotalCount() { return totalCount; }
        public String getMessage() { return message; }
    }
    
    public interface UploadProgressCallback {
        void onProgress(int progress);
        void onSuccess(String fileId);
        void onError(Exception error);
        void onRetry(int attempt, String reason);
    }
    
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(Exception error);
    }
}