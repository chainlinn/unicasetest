package com.oneblue3.unicasetest.network;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.oneblue3.unicasetest.network.NetworkUtil.NetworkCallback;
import com.oneblue3.unicasetest.network.NetworkUtil.UploadResult;
import com.oneblue3.unicasetest.network.NetworkUtil.UserData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;

/**
 * 网络工具类MockWebServer测试
 * 演示文档中组件测试的网络部分：
 * - 使用MockWebServer模拟HTTP服务
 * - 测试网络请求、响应处理、错误处理
 * - 验证请求参数和行为
 * - 异步操作的测试方法
 */
public class NetworkUtilTest {
    
    private MockWebServer mockServer;
    private NetworkUtil networkUtil;
    
    @Before
    public void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        // 使用基础URL，不包含末尾斜杠以避免双斜杠问题
        String baseUrl = mockServer.url("").toString();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        networkUtil = new NetworkUtil(baseUrl);
    }
    
    @After
    public void tearDown() throws IOException {
        mockServer.shutdown();
    }
    
    @Test
    public void fetchUserData_successResponse_shouldParseCorrectly() throws IOException, InterruptedException {
        // 模拟成功响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":1,\"name\":\"张三\",\"status\":\"active\"}"));
        
        UserData result = networkUtil.fetchUserData(1);
        
        // 验证解析结果
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("张三");
        assertThat(result.getStatus()).isEqualTo("active");
        
        // 验证请求参数
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/users/1");
        assertThat(request.getMethod()).isEqualTo("GET");
    }
    
    @Test
    public void fetchUserData_notFoundResponse_shouldThrowException() {
        // 模拟404响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(404)
            .setBody("User not found"));
        
        try {
            networkUtil.fetchUserData(999);
            fail("应该抛出IOException");
        } catch (IOException e) {
            assertThat(e.getMessage()).contains("请求失败: 404");
        }
    }
    
    @Test
    public void fetchUserData_serverError_shouldThrowException() {
        // 模拟500服务器错误
        mockServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));
        
        try {
            networkUtil.fetchUserData(1);
            fail("应该抛出IOException");
        } catch (IOException e) {
            assertThat(e.getMessage()).contains("请求失败: 500");
        }
    }
    
    @Test
    public void fetchUserData_networkTimeout_shouldThrowException() {
        // 模拟网络超时
        mockServer.enqueue(new MockResponse()
            .setSocketPolicy(SocketPolicy.NO_RESPONSE));
        
        try {
            networkUtil.fetchUserData(1);
            fail("应该抛出IOException");
        } catch (IOException e) {
            // 验证抛出了异常
        }
    }
    
    @Test
    public void uploadUserData_validData_shouldSucceed() throws IOException, InterruptedException {
        // 模拟成功上传响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"status\":\"success\",\"message\":\"上传成功\"}"));
        
        UserData userData = new UserData(2, "李四", "pending");
        UploadResult result = networkUtil.uploadUserData(userData);
        
        // 验证结果
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessage()).isEqualTo("上传成功");
        
        // 验证请求内容
        RecordedRequest request = mockServer.takeRequest();
        assertThat(request.getPath()).isEqualTo("/users");
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getHeader("Content-Type")).contains("application/json");
        
        String requestBody = request.getBody().readUtf8();
        assertThat(requestBody).contains("\"id\":2");
        assertThat(requestBody).contains("\"name\":\"李四\"");
        assertThat(requestBody).contains("\"status\":\"pending\"");
    }
    
    @Test
    public void uploadUserData_serverReject_shouldReturnFailure() {
        // 模拟上传失败响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(400)
            .setBody("Bad Request"));
        
        UserData userData = new UserData(3, "王五", "invalid");
        
        try {
            networkUtil.uploadUserData(userData);
            fail("应该抛出IOException");
        } catch (IOException e) {
            assertThat(e.getMessage()).contains("上传失败: 400");
        }
    }
    
    @Test
    public void fetchUserDataAsync_successResponse_shouldCallbackWithData() throws InterruptedException {
        // 模拟成功响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":4,\"name\":\"赵六\",\"status\":\"active\"}"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UserData> resultData = new AtomicReference<>();
        AtomicReference<Exception> errorData = new AtomicReference<>();
        
        networkUtil.fetchUserDataAsync(4, new NetworkCallback<UserData>() {
            @Override
            public void onSuccess(UserData data) {
                resultData.set(data);
                latch.countDown();
            }
            
            @Override
            public void onError(Exception e) {
                errorData.set(e);
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        // 验证结果
        assertThat(errorData.get()).isNull();
        assertThat(resultData.get()).isNotNull();
        assertThat(resultData.get().getId()).isEqualTo(4);
        assertThat(resultData.get().getName()).isEqualTo("赵六");
        assertThat(resultData.get().getStatus()).isEqualTo("active");
    }
    
    @Test
    public void fetchUserDataAsync_errorResponse_shouldCallbackWithError() throws InterruptedException {
        // 模拟错误响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Internal Server Error"));
        
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<UserData> resultData = new AtomicReference<>();
        AtomicReference<Exception> errorData = new AtomicReference<>();
        
        networkUtil.fetchUserDataAsync(5, new NetworkCallback<UserData>() {
            @Override
            public void onSuccess(UserData data) {
                resultData.set(data);
                latch.countDown();
            }
            
            @Override
            public void onError(Exception e) {
                errorData.set(e);
                latch.countDown();
            }
        });
        
        // 等待异步操作完成
        boolean completed = latch.await(5, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        
        // 验证结果
        assertThat(resultData.get()).isNull();
        assertThat(errorData.get()).isNotNull();
        assertThat(errorData.get().getMessage()).contains("请求失败: 500");
    }
    
    @Test
    public void checkConnection_healthEndpointAvailable_shouldReturnTrue() {
        // 模拟健康检查成功
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("OK"));
        
        boolean result = networkUtil.checkConnection();
        
        assertThat(result).isTrue();
    }
    
    @Test
    public void checkConnection_healthEndpointFailed_shouldReturnFalse() {
        // 模拟健康检查失败
        mockServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("Service Unavailable"));
        
        boolean result = networkUtil.checkConnection();
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void multipleRequests_shouldHandleCorrectly() throws IOException, InterruptedException {
        // 模拟多个请求的响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":1,\"name\":\"用户1\",\"status\":\"active\"}"));
        
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("{\"id\":2,\"name\":\"用户2\",\"status\":\"inactive\"}"));
        
        // 执行多个请求
        UserData user1 = networkUtil.fetchUserData(1);
        UserData user2 = networkUtil.fetchUserData(2);
        
        // 验证结果
        assertThat(user1.getId()).isEqualTo(1);
        assertThat(user1.getName()).isEqualTo("用户1");
        assertThat(user2.getId()).isEqualTo(2);
        assertThat(user2.getName()).isEqualTo("用户2");
        
        // 验证请求路径
        RecordedRequest request1 = mockServer.takeRequest();
        RecordedRequest request2 = mockServer.takeRequest();
        assertThat(request1.getPath()).isEqualTo("/users/1");
        assertThat(request2.getPath()).isEqualTo("/users/2");
    }
    
    @Test
    public void jsonParsingError_shouldThrowException() {
        // 模拟无效JSON响应
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody("invalid json"));
        
        try {
            networkUtil.fetchUserData(1);
            fail("应该抛出RuntimeException");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).contains("JSON解析失败");
        } catch (IOException e) {
            // 也可能抛出IOException，这也是可接受的
        }
    }
}