package com.oneblue3.unicasetest.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.oneblue3.unicasetest.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * MainActivity UI测试类
 * 演示文档中界面层测试：关键业务流程的端到端验证
 * 测试覆盖：用户交互、数据输入验证、业务流程完整性
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityUITest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);
    
    @Test
    public void priceCalculation_validInputs_shouldDisplayCorrectResult() {
        // 输入原价
        onView(withId(R.id.edit_original_price))
            .perform(typeText("100.00"), closeSoftKeyboard());
        
        // 输入折扣率
        onView(withId(R.id.edit_discount_rate))
            .perform(typeText("0.15"), closeSoftKeyboard());
        
        // 点击计算按钮
        onView(withId(R.id.btn_calculate_discount))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_discount_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("原价：100.00"))))
            .check(matches(withText(containsString("折扣率：15.0%"))))
            .check(matches(withText(containsString("折后价：85.00"))));
    }
    
    @Test
    public void priceCalculation_emptyInputs_shouldShowError() {
        // 不输入任何数据，直接点击计算
        onView(withId(R.id.btn_calculate_discount))
            .perform(click());
        
        // Toast消息无法直接测试，但可以验证结果区域不显示
        // 这里我们验证结果区域保持隐藏状态
        onView(withId(R.id.text_discount_result))
            .check(matches(withText(""))); // 应该为空
    }
    
    @Test
    public void priceCalculation_invalidDiscount_shouldShowError() {
        // 输入原价
        onView(withId(R.id.edit_original_price))
            .perform(typeText("100.00"), closeSoftKeyboard());
        
        // 输入无效折扣率（大于1）
        onView(withId(R.id.edit_discount_rate))
            .perform(typeText("1.5"), closeSoftKeyboard());
        
        // 点击计算按钮
        onView(withId(R.id.btn_calculate_discount))
            .perform(click());
        
        // 验证错误情况下结果区域不显示有效结果
        onView(withId(R.id.text_discount_result))
            .check(matches(withText(""))); // 应该为空，因为出错了
    }
    
    @Test
    public void userInfoValidation_validEmail_shouldShowSuccess() {
        // 输入有效邮箱
        onView(withId(R.id.edit_user_email))
            .perform(typeText("test@example.com"), closeSoftKeyboard());
        
        // 点击验证按钮
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("邮箱：✓ 有效"))));
    }
    
    @Test
    public void userInfoValidation_validPhone_shouldShowFormattedResult() {
        // 输入有效手机号
        onView(withId(R.id.edit_user_phone))
            .perform(typeText("13812345678"), closeSoftKeyboard());
        
        // 点击验证按钮
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("手机号：✓ 有效"))))
            .check(matches(withText(containsString("格式化：138-1234-5678"))))
            .check(matches(withText(containsString("脱敏显示：138****5678"))));
    }
    
    @Test
    public void userInfoValidation_invalidEmail_shouldShowError() {
        // 输入无效邮箱
        onView(withId(R.id.edit_user_email))
            .perform(typeText("invalid.email"), closeSoftKeyboard());
        
        // 点击验证按钮
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("邮箱：✗ 无效"))));
    }
    
    @Test
    public void userInfoValidation_invalidPhone_shouldShowError() {
        // 输入无效手机号
        onView(withId(R.id.edit_user_phone))
            .perform(typeText("12345678901"), closeSoftKeyboard());
        
        // 点击验证按钮
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("手机号：✗ 无效"))));
    }
    
    @Test
    public void userInfoValidation_bothEmailAndPhone_shouldValidateBoth() {
        // 输入邮箱和手机号
        onView(withId(R.id.edit_user_email))
            .perform(typeText("user@test.com"), closeSoftKeyboard());
        
        onView(withId(R.id.edit_user_phone))
            .perform(typeText("15987654321"), closeSoftKeyboard());
        
        // 点击验证按钮
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        // 验证结果显示
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("邮箱：✓ 有效"))))
            .check(matches(withText(containsString("手机号：✓ 有效"))));
    }
    
    @Test
    public void orderCreation_validAmount_shouldShowOrderFlow() {
        // 输入订单金额
        onView(withId(R.id.edit_order_amount))
            .perform(typeText("199.99"), closeSoftKeyboard());
        
        // 点击创建订单按钮
        onView(withId(R.id.btn_create_order))
            .perform(click());
        
        // 验证订单状态显示
        onView(withId(R.id.text_order_status))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("订单创建成功"))))
            .check(matches(withText(containsString("✓ 支付成功"))))
            .check(matches(withText(containsString("✓ 发货成功"))))
            .check(matches(withText(containsString("✓ 订单完成"))))
            .check(matches(withText(containsString("最终状态：DELIVERED"))));
    }
    
    @Test
    public void orderCreation_emptyAmount_shouldShowError() {
        // 不输入金额，直接点击创建订单
        onView(withId(R.id.btn_create_order))
            .perform(click());
        
        // 验证订单状态区域不显示结果
        onView(withId(R.id.text_order_status))
            .check(matches(withText(""))); // 应该为空
    }
    
    @Test
    public void orderCreation_invalidAmount_shouldShowError() {
        // 输入无效金额
        onView(withId(R.id.edit_order_amount))
            .perform(typeText("invalid"), closeSoftKeyboard());
        
        // 点击创建订单按钮
        onView(withId(R.id.btn_create_order))
            .perform(click());
        
        // 验证订单状态区域不显示结果
        onView(withId(R.id.text_order_status))
            .check(matches(withText(""))); // 应该为空，因为出错了
    }
    
    @Test
    public void completeUserFlow_allFunctions_shouldWorkCorrectly() {
        // 测试完整用户流程：价格计算 -> 用户验证 -> 订单创建
        
        // 1. 价格计算
        onView(withId(R.id.edit_original_price))
            .perform(typeText("299.99"), closeSoftKeyboard());
        onView(withId(R.id.edit_discount_rate))
            .perform(typeText("0.20"), closeSoftKeyboard());
        onView(withId(R.id.btn_calculate_discount))
            .perform(click());
        
        onView(withId(R.id.text_discount_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("折后价：239.99"))));
        
        // 2. 用户信息验证
        onView(withId(R.id.edit_user_email))
            .perform(typeText("customer@shop.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_user_phone))
            .perform(typeText("19876543210"), closeSoftKeyboard());
        onView(withId(R.id.btn_validate_info))
            .perform(click());
        
        onView(withId(R.id.text_validation_result))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("邮箱：✓ 有效"))))
            .check(matches(withText(containsString("手机号：✓ 有效"))));
        
        // 3. 订单创建
        onView(withId(R.id.edit_order_amount))
            .perform(typeText("239.99"), closeSoftKeyboard());
        onView(withId(R.id.btn_create_order))
            .perform(click());
        
        onView(withId(R.id.text_order_status))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString("订单创建成功"))))
            .check(matches(withText(containsString("最终状态：DELIVERED"))));
    }
    
    @Test
    public void appTitle_shouldBeDisplayed() {
        // 验证应用标题显示
        onView(withText("Android单元测试三层架构演示"))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void allSections_shouldBeDisplayed() {
        // 验证所有功能区域的标题都显示
        onView(withText("工具层演示 - 价格计算器"))
            .check(matches(isDisplayed()));
        
        onView(withText("工具层演示 - 字符串处理器"))
            .check(matches(isDisplayed()));
        
        onView(withText("逻辑层演示 - 订单状态机"))
            .check(matches(isDisplayed()));
    }
}