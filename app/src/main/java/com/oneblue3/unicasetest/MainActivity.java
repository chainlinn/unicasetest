package com.oneblue3.unicasetest;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.oneblue3.unicasetest.ui.CalculatorFragment;
import com.oneblue3.unicasetest.ui.DataListFragment;
import com.oneblue3.unicasetest.ui.UserRegistrationFragment;

/**
 * 主界面Activity - 管理不同功能模块的Fragment切换
 * 包含用户注册、计算器、数据展示等复杂UI组件
 */
public class MainActivity extends AppCompatActivity {

    private Button btnUserRegistration;
    private Button btnCalculator;
    private Button btnDataList;
    private FragmentManager fragmentManager;
    
    // Fragment实例
    private UserRegistrationFragment userRegistrationFragment;
    private CalculatorFragment calculatorFragment;
    private DataListFragment dataListFragment;
    
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        initializeViews();
        setupFragmentManager();
        setupClickListeners();
        
        // 默认显示用户注册界面
        showUserRegistrationFragment();
    }
    
    /**
     * 初始化视图组件
     */
    private void initializeViews() {
        btnUserRegistration = findViewById(R.id.btn_user_registration);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnDataList = findViewById(R.id.btn_data_list);
    }
    
    /**
     * 设置Fragment管理器
     */
    private void setupFragmentManager() {
        fragmentManager = getSupportFragmentManager();
        
        // 初始化Fragment实例
        userRegistrationFragment = new UserRegistrationFragment();
        calculatorFragment = new CalculatorFragment();
        dataListFragment = new DataListFragment();
    }
    
    /**
     * 设置按钮点击监听器
     */
    private void setupClickListeners() {
        btnUserRegistration.setOnClickListener(v -> showUserRegistrationFragment());
        btnCalculator.setOnClickListener(v -> showCalculatorFragment());
        btnDataList.setOnClickListener(v -> showDataListFragment());
    }
    
    /**
     * 显示用户注册Fragment
     */
    public void showUserRegistrationFragment() {
        showFragment(userRegistrationFragment, "UserRegistration");
        updateButtonStates(btnUserRegistration);
    }
    
    /**
     * 显示计算器Fragment
     */
    public void showCalculatorFragment() {
        showFragment(calculatorFragment, "Calculator");
        updateButtonStates(btnCalculator);
    }
    
    /**
     * 显示数据列表Fragment
     */
    public void showDataListFragment() {
        showFragment(dataListFragment, "DataList");
        updateButtonStates(btnDataList);
    }
    
    /**
     * 显示指定的Fragment
     */
    private void showFragment(Fragment fragment, String tag) {
        if (currentFragment == fragment) {
            return; // 如果是当前Fragment，不需要切换
        }
        
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // 隐藏当前Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        
        // 检查Fragment是否已添加
        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.fragment_container, fragment, tag);
        }
        
        transaction.commit();
        currentFragment = fragment;
    }
    
    /**
     * 更新按钮状态
     */
    private void updateButtonStates(Button activeButton) {
        // 重置所有按钮状态
        btnUserRegistration.setSelected(false);
        btnCalculator.setSelected(false);
        btnDataList.setSelected(false);
        
        // 设置活动按钮状态
        activeButton.setSelected(true);
    }
    
    /**
     * 获取当前显示的Fragment
     */
    public Fragment getCurrentFragment() {
        return currentFragment;
    }
    
    /**
     * 显示Toast消息
     */
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 显示长Toast消息
     */
    public void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理资源
        fragmentManager = null;
        currentFragment = null;
    }
}