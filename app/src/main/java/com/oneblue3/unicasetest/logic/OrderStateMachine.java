package com.oneblue3.unicasetest.logic;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 订单状态机
 * 演示文档中纯业务逻辑测试：状态流转、业务规则验证
 * 适用场景：订单管理、工作流处理、状态转换等
 */
public class OrderStateMachine {
    
    /**
     * 处理支付
     * @param order 订单对象
     * @return 是否处理成功
     */
    public boolean processPayment(Order order) {
        if (order == null) {
            return false;
        }
        
        // 检查订单状态是否允许支付
        if (!canTransitionTo(order.getStatus(), OrderStatus.PAID)) {
            return false;
        }
        
        // 验证订单金额
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // 模拟支付处理
        order.setStatus(OrderStatus.PAID);
        order.setPaidTime(LocalDateTime.now());
        
        return true;
    }
    
    /**
     * 处理发货
     * @param order 订单对象
     * @return 是否处理成功
     */
    public boolean processShipment(Order order) {
        if (order == null) {
            return false;
        }
        
        if (!canTransitionTo(order.getStatus(), OrderStatus.SHIPPED)) {
            return false;
        }
        
        order.setStatus(OrderStatus.SHIPPED);
        order.setShippedTime(LocalDateTime.now());
        
        return true;
    }
    
    /**
     * 确认收货
     * @param order 订单对象
     * @return 是否处理成功
     */
    public boolean confirmDelivery(Order order) {
        if (order == null) {
            return false;
        }
        
        if (!canTransitionTo(order.getStatus(), OrderStatus.DELIVERED)) {
            return false;
        }
        
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredTime(LocalDateTime.now());
        
        return true;
    }
    
    /**
     * 取消订单
     * @param order 订单对象
     * @param reason 取消原因
     * @return 是否取消成功
     */
    public boolean cancelOrder(Order order, String reason) {
        if (order == null) {
            return false;
        }
        
        // 已发货和已完成的订单不能取消
        if (order.getStatus() == OrderStatus.SHIPPED || 
            order.getStatus() == OrderStatus.DELIVERED ||
            order.getStatus() == OrderStatus.CANCELLED) {
            return false;
        }
        
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledTime(LocalDateTime.now());
        
        return true;
    }
    
    /**
     * 计算会员折扣
     * @param amount 商品金额
     * @param level 会员等级
     * @return 折扣金额
     */
    public double calculateMemberDiscount(double amount, MemberLevel level) {
        if (amount < 0 || level == null) {
            throw new IllegalArgumentException("无效参数");
        }
        
        double discountRate = getBaseDiscountRate(level);
        
        // 大额消费额外折扣
        if (amount >= 500.0) {
            discountRate += 0.03;
        }
        
        // 最大折扣限制
        discountRate = Math.min(discountRate, 0.20);
        
        return amount * discountRate;
    }
    
    /**
     * 验证订单有效性
     * @param order 订单对象
     * @return 验证结果
     */
    public OrderValidationResult validateOrder(Order order) {
        if (order == null) {
            return new OrderValidationResult(false, "订单不能为空");
        }
        
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            return new OrderValidationResult(false, "订单ID不能为空");
        }
        
        if (order.getCustomerId() == null || order.getCustomerId().trim().isEmpty()) {
            return new OrderValidationResult(false, "客户ID不能为空");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return new OrderValidationResult(false, "订单金额必须大于0");
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return new OrderValidationResult(false, "订单必须包含商品");
        }
        
        // 验证商品项
        for (OrderItem item : order.getItems()) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                return new OrderValidationResult(false, "商品ID不能为空");
            }
            if (item.getQuantity() <= 0) {
                return new OrderValidationResult(false, "商品数量必须大于0");
            }
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return new OrderValidationResult(false, "商品单价必须大于0");
            }
        }
        
        return new OrderValidationResult(true, "订单验证通过");
    }
    
    /**
     * 计算订单总金额
     * @param items 订单项列表
     * @return 总金额
     */
    public BigDecimal calculateOrderTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getUnitPrice() != null && item.getQuantity() > 0) {
                BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }
        
        return total;
    }
    
    /**
     * 检查状态转换是否有效
     */
    private boolean canTransitionTo(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) {
            return false;
        }
        
        switch (from) {
            case PENDING:
                return to == OrderStatus.PAID || to == OrderStatus.CANCELLED;
            case PAID:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
            case CANCELLED:
                return false; // 终态，不能再转换
            default:
                return false;
        }
    }
    
    /**
     * 获取基础折扣率
     */
    private double getBaseDiscountRate(MemberLevel level) {
        return switch (level) {
            case BRONZE -> 0.05;
            case SILVER -> 0.10;
            case GOLD -> 0.15;
        };
    }
    
    /**
     * 订单状态枚举
     */
    public enum OrderStatus {
        PENDING,    // 待支付
        PAID,       // 已支付
        SHIPPED,    // 已发货
        DELIVERED,  // 已完成
        CANCELLED   // 已取消
    }
    
    /**
     * 会员等级枚举
     */
    public enum MemberLevel {
        BRONZE, SILVER, GOLD
    }
    
    /**
     * 订单类
     */
    public static class Order {
        private String orderId;
        private String customerId;
        private OrderStatus status;
        private BigDecimal totalAmount;
        private List<OrderItem> items;
        private LocalDateTime createTime;
        private LocalDateTime paidTime;
        private LocalDateTime shippedTime;
        private LocalDateTime deliveredTime;
        private LocalDateTime cancelledTime;
        private String cancelReason;
        
        public Order(String orderId) {
            this.orderId = orderId;
            this.status = OrderStatus.PENDING;
            this.createTime = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        public OrderStatus getStatus() { return status; }
        public void setStatus(OrderStatus status) { this.status = status; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
        
        public LocalDateTime getCreateTime() { return createTime; }
        public LocalDateTime getPaidTime() { return paidTime; }
        public void setPaidTime(LocalDateTime paidTime) { this.paidTime = paidTime; }
        
        public LocalDateTime getShippedTime() { return shippedTime; }
        public void setShippedTime(LocalDateTime shippedTime) { this.shippedTime = shippedTime; }
        
        public LocalDateTime getDeliveredTime() { return deliveredTime; }
        public void setDeliveredTime(LocalDateTime deliveredTime) { this.deliveredTime = deliveredTime; }
        
        public LocalDateTime getCancelledTime() { return cancelledTime; }
        public void setCancelledTime(LocalDateTime cancelledTime) { this.cancelledTime = cancelledTime; }
        
        public String getCancelReason() { return cancelReason; }
        public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    }
    
    /**
     * 订单项类
     */
    public static class OrderItem {
        private String productId;
        private String productName;
        private BigDecimal unitPrice;
        private int quantity;
        
        public OrderItem(String productId, String productName, BigDecimal unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
        }
        
        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
    }
    
    /**
     * 订单验证结果类
     */
    public static class OrderValidationResult {
        private final boolean valid;
        private final String message;
        
        public OrderValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}