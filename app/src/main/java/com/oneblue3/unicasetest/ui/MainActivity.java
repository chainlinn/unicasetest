package com.oneblue3.unicasetest.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.oneblue3.unicasetest.R;
import com.oneblue3.unicasetest.logic.OrderStateMachine;
import com.oneblue3.unicasetest.tools.PriceCalculator;
import com.oneblue3.unicasetest.tools.StringProcessor;

import java.math.BigDecimal;

/**
 * 主Activity
 * 演示文档中UI层测试：关键业务流程的界面交互测试
 * 整合工具层和逻辑层功能，提供完整的用户体验
 */
public class MainActivity extends AppCompatActivity {
    
    // UI组件
    private EditText editOriginalPrice;
    private EditText editDiscountRate;
    private Button btnCalculateDiscount;
    private TextView textDiscountResult;
    
    private EditText editUserEmail;
    private EditText editUserPhone;
    private Button btnValidateInfo;
    private TextView textValidationResult;
    
    private EditText editOrderAmount;
    private Button btnCreateOrder;
    private TextView textOrderStatus;
    
    // 业务逻辑组件
    private PriceCalculator priceCalculator;
    private StringProcessor stringProcessor;
    private OrderStateMachine orderStateMachine;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        initializeViews();
        setupEventListeners();
    }
    
    /**
     * 初始化业务逻辑组件
     */
    private void initializeComponents() {
        priceCalculator = new PriceCalculator();
        stringProcessor = new StringProcessor();
        orderStateMachine = new OrderStateMachine();
    }
    
    /**
     * 初始化UI视图
     */
    private void initializeViews() {
        // 价格计算相关
        editOriginalPrice = findViewById(R.id.edit_original_price);
        editDiscountRate = findViewById(R.id.edit_discount_rate);
        btnCalculateDiscount = findViewById(R.id.btn_calculate_discount);
        textDiscountResult = findViewById(R.id.text_discount_result);
        
        // 用户信息验证相关
        editUserEmail = findViewById(R.id.edit_user_email);
        editUserPhone = findViewById(R.id.edit_user_phone);
        btnValidateInfo = findViewById(R.id.btn_validate_info);
        textValidationResult = findViewById(R.id.text_validation_result);
        
        // 订单创建相关
        editOrderAmount = findViewById(R.id.edit_order_amount);
        btnCreateOrder = findViewById(R.id.btn_create_order);
        textOrderStatus = findViewById(R.id.text_order_status);
    }
    
    /**
     * 设置事件监听器
     */
    private void setupEventListeners() {
        btnCalculateDiscount.setOnClickListener(this::onCalculateDiscountClicked);
        btnValidateInfo.setOnClickListener(this::onValidateInfoClicked);
        btnCreateOrder.setOnClickListener(this::onCreateOrderClicked);
    }
    
    /**
     * 计算折扣按钮点击事件
     */
    private void onCalculateDiscountClicked(View view) {
        try {
            String priceText = editOriginalPrice.getText().toString().trim();
            String discountText = editDiscountRate.getText().toString().trim();
            
            if (priceText.isEmpty() || discountText.isEmpty()) {
                showError("请输入价格和折扣率");
                return;
            }
            
            BigDecimal originalPrice = new BigDecimal(priceText);
            BigDecimal discountRate = new BigDecimal(discountText);
            
            BigDecimal discountedPrice = priceCalculator.calculateDiscountPrice(originalPrice, discountRate);
            
            String resultText = String.format("原价：%.2f\n折扣率：%.1f%%\n折后价：%.2f", 
                originalPrice.doubleValue(), 
                discountRate.multiply(new BigDecimal("100")).doubleValue(),
                discountedPrice.doubleValue());
            
            textDiscountResult.setText(resultText);
            textDiscountResult.setVisibility(View.VISIBLE);
            
        } catch (NumberFormatException e) {
            showError("请输入有效的数字");
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }
    
    /**
     * 验证用户信息按钮点击事件
     */
    private void onValidateInfoClicked(View view) {
        String email = editUserEmail.getText().toString().trim();
        String phone = editUserPhone.getText().toString().trim();
        
        if (email.isEmpty() && phone.isEmpty()) {
            showError("请输入邮箱或手机号");
            return;
        }
        
        StringBuilder resultBuilder = new StringBuilder();
        
        if (!email.isEmpty()) {
            boolean isValidEmail = stringProcessor.isValidEmail(email);
            resultBuilder.append("邮箱：").append(isValidEmail ? "✓ 有效" : "✗ 无效").append("\n");
        }
        
        if (!phone.isEmpty()) {
            boolean isValidPhone = stringProcessor.isValidChinesePhone(phone);
            resultBuilder.append("手机号：").append(isValidPhone ? "✓ 有效" : "✗ 无效").append("\n");
            
            if (isValidPhone) {
                try {
                    String formattedPhone = stringProcessor.formatPhone(phone);
                    String maskedPhone = stringProcessor.maskSensitiveInfo(phone, 3, 4);
                    resultBuilder.append("格式化：").append(formattedPhone).append("\n");
                    resultBuilder.append("脱敏显示：").append(maskedPhone).append("\n");
                } catch (Exception e) {
                    resultBuilder.append("格式化失败：").append(e.getMessage()).append("\n");
                }
            }
        }
        
        textValidationResult.setText(resultBuilder.toString().trim());
        textValidationResult.setVisibility(View.VISIBLE);
    }
    
    /**
     * 创建订单按钮点击事件
     */
    private void onCreateOrderClicked(View view) {
        try {
            String amountText = editOrderAmount.getText().toString().trim();
            
            if (amountText.isEmpty()) {
                showError("请输入订单金额");
                return;
            }
            
            BigDecimal amount = new BigDecimal(amountText);
            
            // 创建简单订单进行状态测试
            OrderStateMachine.Order order = new OrderStateMachine.Order("ORDER_" + System.currentTimeMillis());
            order.setCustomerId("CUST_123");
            order.setTotalAmount(amount);
            
            // 模拟订单状态流转
            StringBuilder statusBuilder = new StringBuilder();
            statusBuilder.append("订单创建成功\n");
            statusBuilder.append("订单ID：").append(order.getOrderId()).append("\n");
            statusBuilder.append("初始状态：").append(order.getStatus()).append("\n");
            
            // 尝试支付
            boolean paymentSuccess = orderStateMachine.processPayment(order);
            if (paymentSuccess) {
                statusBuilder.append("✓ 支付成功\n");
                statusBuilder.append("当前状态：").append(order.getStatus()).append("\n");
                
                // 尝试发货
                boolean shipmentSuccess = orderStateMachine.processShipment(order);
                if (shipmentSuccess) {
                    statusBuilder.append("✓ 发货成功\n");
                    statusBuilder.append("当前状态：").append(order.getStatus()).append("\n");
                    
                    // 尝试确认收货
                    boolean deliverySuccess = orderStateMachine.confirmDelivery(order);
                    if (deliverySuccess) {
                        statusBuilder.append("✓ 订单完成\n");
                        statusBuilder.append("最终状态：").append(order.getStatus()).append("\n");
                    }
                }
            } else {
                statusBuilder.append("✗ 支付失败\n");
            }
            
            textOrderStatus.setText(statusBuilder.toString());
            textOrderStatus.setVisibility(View.VISIBLE);
            
            Toast.makeText(this, "订单流程演示完成", Toast.LENGTH_SHORT).show();
            
        } catch (NumberFormatException e) {
            showError("请输入有效的金额");
        } catch (Exception e) {
            showError("订单创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 显示错误信息
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    /**
     * 清空所有结果显示
     */
    public void clearAllResults() {
        textDiscountResult.setVisibility(View.GONE);
        textValidationResult.setVisibility(View.GONE);
        textOrderStatus.setVisibility(View.GONE);
        
        editOriginalPrice.setText("");
        editDiscountRate.setText("");
        editUserEmail.setText("");
        editUserPhone.setText("");
        editOrderAmount.setText("");
    }
    
    /**
     * 获取折扣计算结果（用于测试）
     */
    public String getDiscountResult() {
        return textDiscountResult.getText().toString();
    }
    
    /**
     * 获取验证结果（用于测试）
     */
    public String getValidationResult() {
        return textValidationResult.getText().toString();
    }
    
    /**
     * 获取订单状态（用于测试）
     */
    public String getOrderStatus() {
        return textOrderStatus.getText().toString();
    }
    
    /**
     * 设置价格输入（用于测试）
     */
    public void setPriceInputs(String price, String discount) {
        editOriginalPrice.setText(price);
        editDiscountRate.setText(discount);
    }
    
    /**
     * 设置用户信息输入（用于测试）
     */
    public void setUserInfoInputs(String email, String phone) {
        editUserEmail.setText(email);
        editUserPhone.setText(phone);
    }
    
    /**
     * 设置订单金额输入（用于测试）
     */
    public void setOrderAmountInput(String amount) {
        editOrderAmount.setText(amount);
    }
}