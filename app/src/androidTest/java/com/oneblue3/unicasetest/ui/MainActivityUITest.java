package com.oneblue3.unicasetest.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.oneblue3.unicasetest.MainActivity;
import com.oneblue3.unicasetest.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 主活动界面测试
 * 演示文档中界面测试的最佳实践：
 * - 专注核心用户流程，不测试所有UI细节
 * - 验证端到端的用户交互场景
 * - 使用Espresso进行应用内UI自动化测试
 * - 保持测试的稳定性和可维护性
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityUITest {
    
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = 
        new ActivityScenarioRule<>(MainActivity.class);
    
    @Test
    public void app_launchesSuccessfully() {
        // 验证应用启动后的初始状态
        // 这是最基础的界面测试 - 确保应用能正常启动
        onView(withId(R.id.btn_user_registration))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.btn_calculator))
            .check(matches(isDisplayed()));
        
        onView(withId(R.id.btn_data_list))
            .check(matches(isDisplayed()));
    }
    
    @Test
    public void navigationTabs_clickDifferentTabs_shouldSwitchCorrectly() {
        // 测试核心导航流程 - 这是用户最重要的交互路径
        
        // 验证初始状态 - 用户注册页面被选中
        onView(withId(R.id.btn_user_registration))
            .check(matches(isDisplayed()))
            .check(matches(isSelected()));
        
        // 切换到计算器页面
        onView(withId(R.id.btn_calculator))
            .perform(click());
        
        // 验证状态变化
        onView(withId(R.id.btn_calculator))
            .check(matches(isSelected()));
        onView(withId(R.id.btn_user_registration))
            .check(matches(not(isSelected())));
        
        // 切换到数据列表页面
        onView(withId(R.id.btn_data_list))
            .perform(click());
        
        onView(withId(R.id.btn_data_list))
            .check(matches(isSelected()));
        onView(withId(R.id.btn_calculator))
            .check(matches(not(isSelected())));
        
        // 回到用户注册页面
        onView(withId(R.id.btn_user_registration))
            .perform(click());
        
        onView(withId(R.id.btn_user_registration))
            .check(matches(isSelected()));
        onView(withId(R.id.btn_data_list))
            .check(matches(not(isSelected())));
    }
    
    @Test
    public void userFlow_navigateAllTabs_shouldMaintainState() {
        // 端到端用户流程测试
        // 模拟用户依次访问所有功能模块的完整场景
        
        // 第一步：确认在用户注册页面
        onView(withId(R.id.btn_user_registration))
            .check(matches(isSelected()));
        
        // 第二步：访问计算器功能
        onView(withId(R.id.btn_calculator))
            .perform(click());
        onView(withId(R.id.btn_calculator))
            .check(matches(isSelected()));
        
        // 第三步：访问数据列表功能
        onView(withId(R.id.btn_data_list))
            .perform(click());
        onView(withId(R.id.btn_data_list))
            .check(matches(isSelected()));
        
        // 第四步：返回用户注册页面，验证应用状态完整性
        onView(withId(R.id.btn_user_registration))
            .perform(click());
        onView(withId(R.id.btn_user_registration))
            .check(matches(isSelected()));
        
        // 第五步：快速切换验证响应性
        onView(withId(R.id.btn_calculator))
            .perform(click());
        onView(withId(R.id.btn_calculator))
            .check(matches(isSelected()));
        
        onView(withId(R.id.btn_data_list))
            .perform(click());
        onView(withId(R.id.btn_data_list))
            .check(matches(isSelected()));
    }
    
    @Test
    public void multipleClicks_sameTab_shouldRemainStable() {
        // 测试重复点击同一按钮的稳定性
        // 这是防御性测试，确保UI在异常操作下仍然稳定
        
        // 多次点击同一个按钮
        onView(withId(R.id.btn_calculator))
            .perform(click())
            .perform(click())
            .perform(click());
        
        // 验证状态仍然正确
        onView(withId(R.id.btn_calculator))
            .check(matches(isSelected()));
        onView(withId(R.id.btn_user_registration))
            .check(matches(not(isSelected())));
        onView(withId(R.id.btn_data_list))
            .check(matches(not(isSelected())));
        
        // 切换到其他页面验证功能正常
        onView(withId(R.id.btn_data_list))
            .perform(click());
        onView(withId(R.id.btn_data_list))
            .check(matches(isSelected()));
    }
    
    @Test
    public void fragmentContainer_shouldBeVisible() {
        // 验证Fragment容器存在且可见
        // 这确保了Fragment能够正确显示内容
        onView(withId(R.id.fragment_container))
            .check(matches(isDisplayed()));
    }
}