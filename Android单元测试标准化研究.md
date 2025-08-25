# Android测试框架选择指南

## 🛠️ 一、工具层测试 - 基础设施验证

### 核心职责
验证**独立工具函数**和**基础设施组件**，这类代码通常是项目的基石，需要极高的稳定性和性能。

### 📌 无依赖工具测试（JUnit 5 + Truth）

**使用场景**：
- 纯算法实现（排序、搜索、加密）
- 数据转换工具（JSON解析、格式化、编解码）
- 字符串处理工具（验证、清理、格式化）
- 数学计算工具（价格计算、统计函数）

**依赖配置**：
```kotlin
testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
testImplementation("com.google.truth:truth:1.1.5")
testImplementation("org.hamcrest:hamcrest:2.2")
```

**测试示例**：
```java
class PriceCalculatorTest {
    private PriceCalculator calculator;
    
    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }
    
    @Test
    @DisplayName("计算商品折扣价格")
    void calculateDiscountPrice_normalCase_shouldReturnCorrect() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discount = new BigDecimal("0.15"); // 15%折扣
        
        BigDecimal result = calculator.calculateDiscountPrice(originalPrice, discount);
        
        assertThat(result).isEqualTo(new BigDecimal("85.00"));
    }
    
    @ParameterizedTest(name = "汇率转换 {0} {1} → {2}")
    @CsvSource({
        "100.0, USD_TO_CNY, 728.50",
        "50.0, EUR_TO_CNY, 392.75",
        "1000.0, CNY_TO_USD, 137.36"
    })
    void convertCurrency_variousRates_shouldCalculateCorrectly(
            double amount, String rateType, double expected) {
        
        double result = calculator.convertCurrency(amount, rateType);
        
        assertThat(result).isWithin(0.01).of(expected);
    }
}
```

**特点**：毫秒级执行，100%可预测，零环境依赖

### 📌 Android框架依赖工具测试（+ Robolectric）

**使用场景**：
- 文件系统工具（缓存管理、配置读写）
- 系统服务封装（网络状态、设备信息获取）
- 资源访问工具（字符串、图片资源处理）
- SharedPreferences封装工具

**新增依赖**：
```kotlin
testImplementation("org.robolectric:robolectric:4.15")
testImplementation("androidx.test:core:1.5.0")
```

**测试示例**：
```java
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
    
    @Test
    public void saveUserConfig_validData_shouldPersistCorrectly() {
        UserConfig config = new UserConfig.Builder()
            .setTheme("dark")
            .setLanguage("zh-CN")
            .setNotificationEnabled(true)
            .build();
        
        boolean result = cacheManager.saveUserConfig(config);
        
        assertTrue("配置保存应该成功", result);
        
        UserConfig retrieved = cacheManager.getUserConfig();
        assertThat(retrieved.getTheme()).isEqualTo("dark");
        assertThat(retrieved.getLanguage()).isEqualTo("zh-CN");
        assertThat(retrieved.isNotificationEnabled()).isTrue();
    }
    
    @Test
    public void clearExpiredCache_oldData_shouldRemoveCorrectly() {
        // 模拟过期数据
        cacheManager.putWithExpiry("expired_key", "old_data", 1); // 1ms过期
        cacheManager.putWithExpiry("valid_key", "new_data", 300000); // 5分钟过期
        
        // 等待过期
        SystemClock.sleep(10);
        
        int removedCount = cacheManager.clearExpiredCache();
        
        assertThat(removedCount).isEqualTo(1);
        assertThat(cacheManager.get("expired_key")).isNull();
        assertThat(cacheManager.get("valid_key")).isEqualTo("new_data");
    }
}
```

**特点**：JVM环境模拟Android，快速但需要框架支持

### 📌 网络依赖工具测试（+ MockWebServer）

**使用场景**：
- HTTP客户端封装
- API调用工具类
- 网络重试和缓存机制
- 文件上传下载工具

**新增依赖**：
```kotlin
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
```

**测试示例**：
```java
class ApiClientTest {
    
    private MockWebServer mockServer;
    private ApiClient apiClient;
    
    @BeforeEach
    void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
        apiClient = new ApiClient(mockServer.url("/").toString());
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockServer.shutdown();
    }
    
    @Test
    void uploadFile_withRetry_shouldSucceedAfterFailure() {
        // 第一次请求失败
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        // 重试成功
        mockServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"fileId\":\"FILE_123\",\"status\":\"uploaded\"}"));
        
        File testFile = createTestFile("test.jpg", 1024);
        UploadProgressCallback callback = mock(UploadProgressCallback.class);
        
        UploadResult result = apiClient.uploadFileWithRetry(testFile, callback);
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getFileId()).isEqualTo("FILE_123");
        
        // 验证进度回调
        verify(callback).onProgress(0);
        verify(callback).onProgress(100);
        verify(callback).onSuccess("FILE_123");
        
        // 验证重试机制
        assertThat(mockServer.getRequestCount()).isEqualTo(2);
    }
}
```

**特点**：可控网络环境，支持各种网络场景模拟

---

## 🧠 二、逻辑层测试 - 业务规则验证

### 核心职责
验证**业务规则**和**数据处理流程**，这是应用的核心价值所在，决定了功能的正确性。

### 📌 纯业务逻辑测试（JUnit 5 + Truth）

**使用场景**：
- 业务规则引擎（价格计算、折扣逻辑、权限判断）
- 数据验证器（表单验证、业务规则检查）
- 状态机逻辑（订单状态流转、游戏状态管理）
- 领域对象行为（实体类的业务方法）

**测试示例**：
```java
class OrderStateMachineTest {
    
    private OrderStateMachine stateMachine;
    
    @BeforeEach
    void setUp() {
        stateMachine = new OrderStateMachine();
    }
    
    @Test
    @DisplayName("订单状态正常流转")
    void orderStateTransition_normalFlow_shouldTransitCorrectly() {
        Order order = new Order("ORDER_001");
        
        // 初始状态
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        
        // 支付确认
        boolean paymentResult = stateMachine.processPayment(order);
        assertThat(paymentResult).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        
        // 发货处理
        boolean shipResult = stateMachine.processShipment(order);
        assertThat(shipResult).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        
        // 确认收货
        boolean deliveryResult = stateMachine.confirmDelivery(order);
        assertThat(deliveryResult).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }
    
    @Test
    @DisplayName("无效状态转换应该失败")
    void orderStateTransition_invalidTransition_shouldFail() {
        Order cancelledOrder = new Order("ORDER_002");
        cancelledOrder.setStatus(OrderStatus.CANCELLED);
        
        // 尝试对已取消订单进行支付
        boolean result = stateMachine.processPayment(cancelledOrder);
        
        assertThat(result).isFalse();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
    
    @ParameterizedTest(name = "折扣规则：{0}元商品，{1}级会员，期望折扣{2}")
    @CsvSource({
        "100.0, BRONZE, 0.05",
        "100.0, SILVER, 0.10", 
        "100.0, GOLD, 0.15",
        "500.0, BRONZE, 0.08",
        "500.0, GOLD, 0.20"
    })
    void calculateMemberDiscount_variousLevels_shouldApplyCorrectRates(
            double amount, MemberLevel level, double expectedDiscount) {
        
        double discount = stateMachine.calculateMemberDiscount(amount, level);
        
        assertThat(discount).isWithin(0.001).of(expectedDiscount);
    }
}
```

**优势**：逻辑清晰，边界明确，测试即文档

### 📌 复杂业务逻辑测试（+ Mockito）

**使用场景**：
- 服务层业务逻辑（多个依赖协作的复杂场景）
- 工作流处理器（多步骤业务流程）
- 事件处理器（监听器、回调处理逻辑）
- 数据同步逻辑（本地与远程数据协调）

**新增依赖**：
```kotlin
testImplementation("org.mockito:mockito-core:5.5.0")
testImplementation("org.mockito:mockito-junit-jupiter:5.5.0")
```

**测试示例**：
```java
@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {
    
    @Mock private PaymentService paymentService;
    @Mock private InventoryService inventoryService;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogger auditLogger;
    
    @InjectMocks private OrderProcessor orderProcessor;
    
    @Test
    void processOrder_completeWorkflow_shouldExecuteAllSteps() {
        // 准备测试数据
        Order order = OrderTestDataBuilder.create()
            .withId("ORDER_001")
            .withProductId("PHONE_001")
            .withQuantity(2)
            .withCustomerId("CUST_123")
            .withAmount(new BigDecimal("1999.98"))
            .build();
        
        // 配置Mock行为 - 库存检查通过
        when(inventoryService.checkAvailability("PHONE_001", 2))
            .thenReturn(InventoryCheckResult.available());
        when(inventoryService.reserveItems("PHONE_001", 2))
            .thenReturn(ReservationResult.success("RES_456"));
        
        // 配置Mock行为 - 支付成功
        PaymentRequest expectedPaymentRequest = new PaymentRequest(
            "CUST_123", new BigDecimal("1999.98"), "ORDER_001");
        when(paymentService.processPayment(any(PaymentRequest.class)))
            .thenReturn(PaymentResult.success("PAY_789"));
        
        // 执行业务逻辑
        OrderProcessResult result = orderProcessor.processOrder(order);
        
        // 验证处理结果
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrderId()).isEqualTo("ORDER_001");
        assertThat(result.getPaymentId()).isEqualTo("PAY_789");
        assertThat(result.getReservationId()).isEqualTo("RES_456");
        
        // 验证依赖交互的顺序和参数
        InOrder inOrder = inOrder(inventoryService, paymentService, notificationService, auditLogger);
        
        inOrder.verify(inventoryService).checkAvailability("PHONE_001", 2);
        inOrder.verify(inventoryService).reserveItems("PHONE_001", 2);
        inOrder.verify(paymentService).processPayment(argThat(payment -> 
            payment.getCustomerId().equals("CUST_123") &&
            payment.getAmount().equals(new BigDecimal("1999.98")) &&
            payment.getOrderId().equals("ORDER_001")));
        inOrder.verify(notificationService).sendOrderConfirmation("CUST_123", "ORDER_001");
        inOrder.verify(auditLogger).logOrderProcessed(order, result);
    }
    
    @Test
    void processOrder_inventoryInsufficient_shouldHandleGracefully() {
        Order order = OrderTestDataBuilder.create()
            .withProductId("PHONE_001")
            .withQuantity(100) // 超出库存
            .build();
        
        // 模拟库存不足
        when(inventoryService.checkAvailability("PHONE_001", 100))
            .thenReturn(InventoryCheckResult.insufficient(5)); // 只有5个库存
        
        OrderProcessResult result = orderProcessor.processOrder(order);
        
        // 验证失败处理
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("INSUFFICIENT_INVENTORY");
        assertThat(result.getErrorMessage()).contains("库存不足，当前库存：5");
        
        // 验证不应该执行的操作
        verify(inventoryService, never()).reserveItems(anyString(), anyInt());
        verify(paymentService, never()).processPayment(any());
        verify(notificationService, never()).sendOrderConfirmation(any(), any());
        
        // 验证错误日志记录
        verify(auditLogger).logOrderFailed(order, "INSUFFICIENT_INVENTORY", "库存不足，当前库存：5");
    }
}
```

**特点**：通过Mock隔离依赖，专注核心逻辑验证

---

## 🖥️ 三、UI层测试 - 用户体验验证

### 核心职责
验证**完整用户路径**和**关键交互流程**，确保用户能够顺利完成核心任务。

### 📌 应用内UI交互测试（Espresso）

**使用场景**：
- 关键业务流程（注册登录、购买支付、数据提交）
- 多页面导航验证（Tab切换、页面跳转、数据传递）
- 表单交互验证（输入验证、提交反馈、错误处理）
- 状态响应验证（加载状态、成功失败反馈）

**依赖配置**：
```kotlin
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.test:rules:1.5.0")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
```

**测试示例**：
```java
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UserRegistrationFlowTest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);
    
    @Test
    public void userRegistration_completeFlow_shouldRegisterSuccessfully() {
        // 进入注册页面
        onView(withId(R.id.btn_register))
            .perform(click());
        
        // 填写注册信息
        onView(withId(R.id.edit_username))
            .perform(typeText("testuser2024"), closeSoftKeyboard());
        
        onView(withId(R.id.edit_email))
            .perform(typeText("testuser@example.com"), closeSoftKeyboard());
        
        onView(withId(R.id.edit_password))
            .perform(typeText("SecurePass123!"), closeSoftKeyboard());
        
        onView(withId(R.id.edit_confirm_password))
            .perform(typeText("SecurePass123!"), closeSoftKeyboard());
        
        // 选择用户协议
        onView(withId(R.id.checkbox_agree_terms))
            .perform(click());
        
        // 提交注册
        onView(withId(R.id.btn_submit_registration))
            .perform(click());
        
        // 验证加载状态
        onView(withId(R.id.progress_registration))
            .check(matches(isDisplayed()));
        
        // 等待并验证成功状态
        onView(withText("注册成功"))
            .check(matches(isDisplayed()));
        
        onView(withText("激活邮件已发送至 testuser@example.com"))
            .check(matches(isDisplayed()));
        
        // 验证跳转到登录页面
        onView(withId(R.id.btn_go_to_login))
            .perform(click());
        
        onView(withId(R.id.edit_login_email))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void userRegistration_invalidData_shouldShowValidationErrors() {
        onView(withId(R.id.btn_register)).perform(click());
        
        // 输入无效数据
        onView(withId(R.id.edit_username))
            .perform(typeText("ab"), closeSoftKeyboard()); // 用户名太短
        
        onView(withId(R.id.edit_email))
            .perform(typeText("invalid.email"), closeSoftKeyboard()); // 无效邮箱
        
        onView(withId(R.id.edit_password))
            .perform(typeText("123"), closeSoftKeyboard()); // 密码太弱
        
        onView(withId(R.id.edit_confirm_password))
            .perform(typeText("456"), closeSoftKeyboard()); // 密码不匹配
        
        // 提交表单
        onView(withId(R.id.btn_submit_registration))
            .perform(click());
        
        // 验证各种验证错误
        onView(withId(R.id.edit_username))
            .check(matches(hasErrorText("用户名至少需要3个字符")));
        
        onView(withId(R.id.edit_email))
            .check(matches(hasErrorText("请输入有效的邮箱地址")));
        
        onView(withId(R.id.edit_password))
            .check(matches(hasErrorText("密码强度不足，请包含字母和数字")));
        
        onView(withId(R.id.edit_confirm_password))
            .check(matches(hasErrorText("两次输入的密码不一致")));
        
        // 验证用户协议提示
        onView(withText("请同意用户协议"))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void navigationTabs_switchBetweenSections_shouldMaintainState() {
        // 在注册页面填写部分信息
        onView(withId(R.id.btn_register)).perform(click());
        onView(withId(R.id.edit_username))
            .perform(typeText("testuser"), closeSoftKeyboard());
        
        // 切换到登录页面
        onView(withId(R.id.btn_login_tab)).perform(click());
        onView(withId(R.id.edit_login_email))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        // 切换回注册页面，验证输入保持
        onView(withId(R.id.btn_register_tab)).perform(click());
        onView(withId(R.id.edit_username))
            .check(matches(withText("testuser")));
        
        // 切换回登录页面，验证输入保持
        onView(withId(R.id.btn_login_tab)).perform(click());
        onView(withId(R.id.edit_login_email))
            .check(matches(withText("test@example.com")));
    }
}
```

**特点**：真实设备运行，完整用户体验，但执行较慢

### 📌 Intent交互测试（+ Espresso-Intents）

**使用场景**：
- Activity间跳转验证
- 外部应用调用（相机、地图、分享）
- 深度链接处理
- 应用间数据传递

**新增依赖**：
```kotlin
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
```

**测试示例**：
```java
@RunWith(AndroidJUnit4.class)
public class ExternalIntentTest {
    
    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule = 
        new ActivityScenarioRule<>(ProfileActivity.class);
    
    @Before
    public void setUp() {
        Intents.init();
    }
    
    @After
    public void tearDown() {
        Intents.release();
    }
    
    @Test
    public void shareProfile_clickShare_shouldLaunchShareIntent() {
        // 点击分享按钮
        onView(withId(R.id.btn_share_profile))
            .perform(click());
        
        // 验证分享Intent被正确发送
        intended(allOf(
            hasAction(Intent.ACTION_SEND),
            hasType("text/plain"),
            hasExtra(Intent.EXTRA_TEXT, containsString("查看我的个人资料")),
            hasExtra(Intent.EXTRA_SUBJECT, "个人资料分享")
        ));
    }
    
    @Test
    public void changeAvatar_clickCamera_shouldLaunchCameraIntent() {
        // 点击头像编辑
        onView(withId(R.id.img_avatar)).perform(click());
        onView(withText("拍照")).perform(click());
        
        // 验证相机Intent
        intended(allOf(
            hasAction(MediaStore.ACTION_IMAGE_CAPTURE),
            hasExtra(equalTo(MediaStore.EXTRA_OUTPUT), any(Uri.class))
        ));
    }
    
    @Test
    public void openSettings_clickSettings_shouldNavigateCorrectly() {
        // 打开设置菜单
        onView(withId(R.id.menu_settings)).perform(click());
        
        // 验证设置页面Intent
        intended(hasComponent(SettingsActivity.class.getName()));
        
        // 验证传递的数据
        intended(hasExtra("user_id", "USER_123"));
        intended(hasExtra("source", "profile"));
    }
}
```

**特点**：专门处理Intent相关的复杂交互场景

---

## 🎯 选择决策流程

### 第一步：确定测试层级
```
工具层 → 可复用的独立功能
逻辑层 → 业务规则和数据处理  
UI层 → 用户交互和体验
```

### 第二步：评估依赖复杂度
```
无依赖 → 基础测试框架即可
Android依赖 → 添加Robolectric
网络依赖 → 添加MockWebServer  
复杂依赖 → 添加Mockito
真实UI → 使用Espresso
```

### 第三步：选择合适工具
- **工具层**：优先选择最轻量的测试方案
- **逻辑层**：重点保证业务逻辑的准确性
- **UI层**：只测试最关键的用户路径

---

## 📊 最佳实践总结

### 分布比例指导
- **工具层测试**：30% - 基础稳定，高频使用
- **逻辑层测试**：50% - 业务核心，重点覆盖
- **UI层测试**：20% - 关键路径，精选场景

### 执行效率优化
- 工具层：毫秒级反馈，支持TDD开发
- 逻辑层：秒级执行，快速业务验证
- UI层：分钟级运行，关键场景保障