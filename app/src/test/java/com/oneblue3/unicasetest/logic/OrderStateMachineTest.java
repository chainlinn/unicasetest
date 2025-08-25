package com.oneblue3.unicasetest.logic;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.oneblue3.unicasetest.logic.OrderStateMachine.MemberLevel;
import com.oneblue3.unicasetest.logic.OrderStateMachine.Order;
import com.oneblue3.unicasetest.logic.OrderStateMachine.OrderItem;
import com.oneblue3.unicasetest.logic.OrderStateMachine.OrderStatus;
import com.oneblue3.unicasetest.logic.OrderStateMachine.OrderValidationResult;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

/**
 * 订单状态机测试类
 * 演示文档中纯业务逻辑测试：状态流转、业务规则验证
 */
public class OrderStateMachineTest {
    
    private OrderStateMachine stateMachine;
    
    @Before
    public void setUp() {
        stateMachine = new OrderStateMachine();
    }
    
    @Test
    public void processPayment_pendingOrder_shouldTransitToPaid() {
        Order order = createValidOrder("ORDER_001");
        order.setStatus(OrderStatus.PENDING);
        
        boolean result = stateMachine.processPayment(order);
        
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getPaidTime()).isNotNull();
    }
    
    @Test
    public void processPayment_cancelledOrder_shouldFail() {
        Order order = createValidOrder("ORDER_002");
        order.setStatus(OrderStatus.CANCELLED);
        
        boolean result = stateMachine.processPayment(order);
        
        assertThat(result).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getPaidTime()).isNull();
    }
    
    @Test
    public void processPayment_nullOrder_shouldReturnFalse() {
        boolean result = stateMachine.processPayment(null);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void processPayment_zeroAmount_shouldFail() {
        Order order = createValidOrder("ORDER_003");
        order.setTotalAmount(BigDecimal.ZERO);
        
        boolean result = stateMachine.processPayment(order);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void processPayment_negativeAmount_shouldFail() {
        Order order = createValidOrder("ORDER_004");
        order.setTotalAmount(new BigDecimal("-10.00"));
        
        boolean result = stateMachine.processPayment(order);
        
        assertThat(result).isFalse();
    }
    
    @Test
    public void processShipment_paidOrder_shouldTransitToShipped() {
        Order order = createValidOrder("ORDER_005");
        order.setStatus(OrderStatus.PAID);
        
        boolean result = stateMachine.processShipment(order);
        
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getShippedTime()).isNotNull();
    }
    
    @Test
    public void processShipment_pendingOrder_shouldFail() {
        Order order = createValidOrder("ORDER_006");
        order.setStatus(OrderStatus.PENDING);
        
        boolean result = stateMachine.processShipment(order);
        
        assertThat(result).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
    
    @Test
    public void confirmDelivery_shippedOrder_shouldTransitToDelivered() {
        Order order = createValidOrder("ORDER_007");
        order.setStatus(OrderStatus.SHIPPED);
        
        boolean result = stateMachine.confirmDelivery(order);
        
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getDeliveredTime()).isNotNull();
    }
    
    @Test
    public void confirmDelivery_paidOrder_shouldFail() {
        Order order = createValidOrder("ORDER_008");
        order.setStatus(OrderStatus.PAID);
        
        boolean result = stateMachine.confirmDelivery(order);
        
        assertThat(result).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }
    
    @Test
    public void orderStateTransition_normalFlow_shouldTransitCorrectly() {
        Order order = createValidOrder("ORDER_009");
        
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
    public void cancelOrder_pendingOrder_shouldCancelSuccessfully() {
        Order order = createValidOrder("ORDER_010");
        order.setStatus(OrderStatus.PENDING);
        
        boolean result = stateMachine.cancelOrder(order, "用户取消");
        
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo("用户取消");
        assertThat(order.getCancelledTime()).isNotNull();
    }
    
    @Test
    public void cancelOrder_paidOrder_shouldCancelSuccessfully() {
        Order order = createValidOrder("ORDER_011");
        order.setStatus(OrderStatus.PAID);
        
        boolean result = stateMachine.cancelOrder(order, "库存不足");
        
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancelReason()).isEqualTo("库存不足");
    }
    
    @Test
    public void cancelOrder_shippedOrder_shouldFail() {
        Order order = createValidOrder("ORDER_012");
        order.setStatus(OrderStatus.SHIPPED);
        
        boolean result = stateMachine.cancelOrder(order, "用户要求");
        
        assertThat(result).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
        assertThat(order.getCancelReason()).isNull();
    }
    
    @Test
    public void cancelOrder_deliveredOrder_shouldFail() {
        Order order = createValidOrder("ORDER_013");
        order.setStatus(OrderStatus.DELIVERED);
        
        boolean result = stateMachine.cancelOrder(order, "用户要求");
        
        assertThat(result).isFalse();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }
    
    @Test
    public void calculateMemberDiscount_bronzeLevel_shouldReturnBasicDiscount() {
        double amount = 100.0;
        
        double discount = stateMachine.calculateMemberDiscount(amount, MemberLevel.BRONZE);
        
        assertThat(discount).isWithin(0.001).of(5.0); // 5%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_silverLevel_shouldReturnMediumDiscount() {
        double amount = 100.0;
        
        double discount = stateMachine.calculateMemberDiscount(amount, MemberLevel.SILVER);
        
        assertThat(discount).isWithin(0.001).of(10.0); // 10%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_goldLevel_shouldReturnHighDiscount() {
        double amount = 100.0;
        
        double discount = stateMachine.calculateMemberDiscount(amount, MemberLevel.GOLD);
        
        assertThat(discount).isWithin(0.001).of(15.0); // 15%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_largeAmountBronze_shouldReturnExtraDiscount() {
        double amount = 500.0; // 大额消费
        
        double discount = stateMachine.calculateMemberDiscount(amount, MemberLevel.BRONZE);
        
        assertThat(discount).isWithin(0.001).of(40.0); // 5%+3%额外折扣
    }
    
    @Test
    public void calculateMemberDiscount_largeAmountGold_shouldReturnMaxDiscount() {
        double amount = 500.0; // 大额消费
        
        double discount = stateMachine.calculateMemberDiscount(amount, MemberLevel.GOLD);
        
        assertThat(discount).isWithin(0.001).of(90.0); // 15%+3%额外折扣 = 18%
    }
    
    @Test
    public void calculateMemberDiscount_negativeAmount_shouldThrowException() {
        try {
            stateMachine.calculateMemberDiscount(-100.0, MemberLevel.BRONZE);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无效参数");
        }
    }
    
    @Test
    public void calculateMemberDiscount_nullLevel_shouldThrowException() {
        try {
            stateMachine.calculateMemberDiscount(100.0, null);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无效参数");
        }
    }
    
    @Test
    public void validateOrder_validOrder_shouldReturnSuccess() {
        Order order = createValidOrder("ORDER_014");
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMessage()).isEqualTo("订单验证通过");
    }
    
    @Test
    public void validateOrder_nullOrder_shouldReturnError() {
        OrderValidationResult result = stateMachine.validateOrder(null);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("订单不能为空");
    }
    
    @Test
    public void validateOrder_emptyOrderId_shouldReturnError() {
        Order order = createValidOrder("");
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("订单ID不能为空");
    }
    
    @Test
    public void validateOrder_emptyCustomerId_shouldReturnError() {
        Order order = createValidOrder("ORDER_015");
        order.setCustomerId("");
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("客户ID不能为空");
    }
    
    @Test
    public void validateOrder_zeroAmount_shouldReturnError() {
        Order order = createValidOrder("ORDER_016");
        order.setTotalAmount(BigDecimal.ZERO);
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("订单金额必须大于0");
    }
    
    @Test
    public void validateOrder_emptyItems_shouldReturnError() {
        Order order = createValidOrder("ORDER_017");
        order.setItems(Collections.emptyList());
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("订单必须包含商品");
    }
    
    @Test
    public void validateOrder_invalidItem_shouldReturnError() {
        Order order = createValidOrder("ORDER_018");
        OrderItem invalidItem = new OrderItem("", "商品", new BigDecimal("10.00"), 1);
        order.setItems(Arrays.asList(invalidItem));
        
        OrderValidationResult result = stateMachine.validateOrder(order);
        
        assertThat(result.isValid()).isFalse();
        assertThat(result.getMessage()).contains("商品ID不能为空");
    }
    
    @Test
    public void calculateOrderTotal_validItems_shouldReturnCorrectTotal() {
        OrderItem item1 = new OrderItem("PROD_001", "商品1", new BigDecimal("10.50"), 2);
        OrderItem item2 = new OrderItem("PROD_002", "商品2", new BigDecimal("25.00"), 1);
        
        BigDecimal total = stateMachine.calculateOrderTotal(Arrays.asList(item1, item2));
        
        assertThat(total).isEqualTo(new BigDecimal("46.00")); // 10.50*2 + 25.00*1
    }
    
    @Test
    public void calculateOrderTotal_emptyItems_shouldReturnZero() {
        BigDecimal total = stateMachine.calculateOrderTotal(Collections.emptyList());
        
        assertThat(total).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    public void calculateOrderTotal_nullItems_shouldReturnZero() {
        BigDecimal total = stateMachine.calculateOrderTotal(null);
        
        assertThat(total).isEqualTo(BigDecimal.ZERO);
    }
    
    @Test
    public void calculateOrderTotal_itemsWithZeroQuantity_shouldIgnore() {
        OrderItem validItem = new OrderItem("PROD_001", "商品1", new BigDecimal("10.00"), 2);
        OrderItem zeroQuantityItem = new OrderItem("PROD_002", "商品2", new BigDecimal("20.00"), 0);
        
        BigDecimal total = stateMachine.calculateOrderTotal(Arrays.asList(validItem, zeroQuantityItem));
        
        assertThat(total).isEqualTo(new BigDecimal("20.00")); // 只计算有效商品
    }
    
    private Order createValidOrder(String orderId) {
        Order order = new Order(orderId);
        order.setCustomerId("CUST_123");
        order.setTotalAmount(new BigDecimal("99.99"));
        
        OrderItem item = new OrderItem("PROD_001", "测试商品", new BigDecimal("99.99"), 1);
        order.setItems(Arrays.asList(item));
        
        return order;
    }
}