package com.oneblue3.unicasetest.logic;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单处理器
 * 演示文档中复杂业务逻辑测试：多个依赖协作的复杂场景，使用Mockito进行依赖隔离
 * 适用场景：服务层业务逻辑、工作流处理器、事件处理器等
 */
public class OrderProcessor {
    
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;
    private final AuditLogger auditLogger;
    private final OrderStateMachine stateMachine;
    
    public OrderProcessor(PaymentService paymentService, 
                         InventoryService inventoryService,
                         NotificationService notificationService,
                         AuditLogger auditLogger) {
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
        this.auditLogger = auditLogger;
        this.stateMachine = new OrderStateMachine();
    }
    
    /**
     * 处理订单的完整流程
     * @param order 订单对象
     * @return 处理结果
     */
    public OrderProcessResult processOrder(OrderStateMachine.Order order) {
        try {
            // 1. 验证订单
            OrderStateMachine.OrderValidationResult validation = stateMachine.validateOrder(order);
            if (!validation.isValid()) {
                auditLogger.logOrderFailed(order, "VALIDATION_FAILED", validation.getMessage());
                return new OrderProcessResult(false, order.getOrderId(), null, null, 
                    "VALIDATION_FAILED", validation.getMessage());
            }
            
            // 2. 检查库存
            for (OrderStateMachine.OrderItem item : order.getItems()) {
                InventoryCheckResult inventoryCheck = inventoryService.checkAvailability(
                    item.getProductId(), item.getQuantity());
                
                if (!inventoryCheck.isAvailable()) {
                    String errorMsg = String.format("库存不足，当前库存：%d", inventoryCheck.getAvailableQuantity());
                    auditLogger.logOrderFailed(order, "INSUFFICIENT_INVENTORY", errorMsg);
                    return new OrderProcessResult(false, order.getOrderId(), null, null,
                        "INSUFFICIENT_INVENTORY", errorMsg);
                }
            }
            
            // 3. 预留库存
            String reservationId = null;
            for (OrderStateMachine.OrderItem item : order.getItems()) {
                ReservationResult reservation = inventoryService.reserveItems(
                    item.getProductId(), item.getQuantity());
                
                if (!reservation.isSuccess()) {
                    auditLogger.logOrderFailed(order, "RESERVATION_FAILED", reservation.getErrorMessage());
                    return new OrderProcessResult(false, order.getOrderId(), null, null,
                        "RESERVATION_FAILED", reservation.getErrorMessage());
                }
                
                if (reservationId == null) {
                    reservationId = reservation.getReservationId();
                }
            }
            
            // 4. 处理支付
            PaymentRequest paymentRequest = new PaymentRequest(
                order.getCustomerId(), order.getTotalAmount(), order.getOrderId());
            
            PaymentResult paymentResult = paymentService.processPayment(paymentRequest);
            
            if (!paymentResult.isSuccess()) {
                // 支付失败，释放库存
                inventoryService.releaseReservation(reservationId);
                auditLogger.logOrderFailed(order, "PAYMENT_FAILED", paymentResult.getErrorMessage());
                return new OrderProcessResult(false, order.getOrderId(), null, reservationId,
                    "PAYMENT_FAILED", paymentResult.getErrorMessage());
            }
            
            // 5. 更新订单状态
            boolean paymentProcessed = stateMachine.processPayment(order);
            if (!paymentProcessed) {
                auditLogger.logOrderFailed(order, "STATUS_UPDATE_FAILED", "无法更新订单状态为已支付");
                return new OrderProcessResult(false, order.getOrderId(), paymentResult.getPaymentId(), reservationId,
                    "STATUS_UPDATE_FAILED", "无法更新订单状态为已支付");
            }
            
            // 6. 发送确认通知
            notificationService.sendOrderConfirmation(order.getCustomerId(), order.getOrderId());
            
            // 7. 记录审计日志
            OrderProcessResult result = new OrderProcessResult(true, order.getOrderId(), 
                paymentResult.getPaymentId(), reservationId, null, "订单处理成功");
            auditLogger.logOrderProcessed(order, result);
            
            return result;
            
        } catch (Exception e) {
            auditLogger.logOrderFailed(order, "SYSTEM_ERROR", e.getMessage());
            return new OrderProcessResult(false, order.getOrderId(), null, null,
                "SYSTEM_ERROR", "系统异常：" + e.getMessage());
        }
    }
    
    /**
     * 批量处理订单
     * @param orders 订单列表
     * @return 批处理结果
     */
    public BatchProcessResult batchProcessOrders(List<OrderStateMachine.Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return new BatchProcessResult(0, 0, "没有订单需要处理");
        }
        
        int successCount = 0;
        int failedCount = 0;
        StringBuilder errorMessages = new StringBuilder();
        
        for (OrderStateMachine.Order order : orders) {
            OrderProcessResult result = processOrder(order);
            if (result.isSuccess()) {
                successCount++;
            } else {
                failedCount++;
                errorMessages.append(String.format("订单 %s 处理失败: %s; ", 
                    order.getOrderId(), result.getErrorMessage()));
            }
        }
        
        String message = String.format("批处理完成，成功：%d，失败：%d", successCount, failedCount);
        if (failedCount > 0) {
            message += "，失败详情：" + errorMessages.toString();
        }
        
        return new BatchProcessResult(successCount, failedCount, message);
    }
    
    /**
     * 订单处理结果类
     */
    public static class OrderProcessResult {
        private final boolean success;
        private final String orderId;
        private final String paymentId;
        private final String reservationId;
        private final String errorCode;
        private final String errorMessage;
        
        public OrderProcessResult(boolean success, String orderId, String paymentId, 
                                String reservationId, String errorCode, String errorMessage) {
            this.success = success;
            this.orderId = orderId;
            this.paymentId = paymentId;
            this.reservationId = reservationId;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
        
        public boolean isSuccess() { return success; }
        public String getOrderId() { return orderId; }
        public String getPaymentId() { return paymentId; }
        public String getReservationId() { return reservationId; }
        public String getErrorCode() { return errorCode; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 批处理结果类
     */
    public static class BatchProcessResult {
        private final int successCount;
        private final int failedCount;
        private final String message;
        
        public BatchProcessResult(int successCount, int failedCount, String message) {
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.message = message;
        }
        
        public int getSuccessCount() { return successCount; }
        public int getFailedCount() { return failedCount; }
        public String getMessage() { return message; }
        public boolean isAllSuccess() { return failedCount == 0; }
    }
    
    /**
     * 支付服务接口
     */
    public interface PaymentService {
        PaymentResult processPayment(PaymentRequest request);
    }
    
    /**
     * 库存服务接口
     */
    public interface InventoryService {
        InventoryCheckResult checkAvailability(String productId, int quantity);
        ReservationResult reserveItems(String productId, int quantity);
        boolean releaseReservation(String reservationId);
    }
    
    /**
     * 通知服务接口
     */
    public interface NotificationService {
        void sendOrderConfirmation(String customerId, String orderId);
        void sendPaymentFailureNotification(String customerId, String orderId, String reason);
    }
    
    /**
     * 审计日志接口
     */
    public interface AuditLogger {
        void logOrderProcessed(OrderStateMachine.Order order, OrderProcessResult result);
        void logOrderFailed(OrderStateMachine.Order order, String errorCode, String errorMessage);
    }
    
    /**
     * 支付请求类
     */
    public static class PaymentRequest {
        private final String customerId;
        private final BigDecimal amount;
        private final String orderId;
        
        public PaymentRequest(String customerId, BigDecimal amount, String orderId) {
            this.customerId = customerId;
            this.amount = amount;
            this.orderId = orderId;
        }
        
        public String getCustomerId() { return customerId; }
        public BigDecimal getAmount() { return amount; }
        public String getOrderId() { return orderId; }
    }
    
    /**
     * 支付结果类
     */
    public static class PaymentResult {
        private final boolean success;
        private final String paymentId;
        private final String errorMessage;
        
        private PaymentResult(boolean success, String paymentId, String errorMessage) {
            this.success = success;
            this.paymentId = paymentId;
            this.errorMessage = errorMessage;
        }
        
        public static PaymentResult success(String paymentId) {
            return new PaymentResult(true, paymentId, null);
        }
        
        public static PaymentResult failure(String errorMessage) {
            return new PaymentResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getPaymentId() { return paymentId; }
        public String getErrorMessage() { return errorMessage; }
    }
    
    /**
     * 库存检查结果类
     */
    public static class InventoryCheckResult {
        private final boolean available;
        private final int availableQuantity;
        
        private InventoryCheckResult(boolean available, int availableQuantity) {
            this.available = available;
            this.availableQuantity = availableQuantity;
        }
        
        public static InventoryCheckResult available() {
            return new InventoryCheckResult(true, -1);
        }
        
        public static InventoryCheckResult insufficient(int availableQuantity) {
            return new InventoryCheckResult(false, availableQuantity);
        }
        
        public boolean isAvailable() { return available; }
        public int getAvailableQuantity() { return availableQuantity; }
    }
    
    /**
     * 预留结果类
     */
    public static class ReservationResult {
        private final boolean success;
        private final String reservationId;
        private final String errorMessage;
        
        private ReservationResult(boolean success, String reservationId, String errorMessage) {
            this.success = success;
            this.reservationId = reservationId;
            this.errorMessage = errorMessage;
        }
        
        public static ReservationResult success(String reservationId) {
            return new ReservationResult(true, reservationId, null);
        }
        
        public static ReservationResult failure(String errorMessage) {
            return new ReservationResult(false, null, errorMessage);
        }
        
        public boolean isSuccess() { return success; }
        public String getReservationId() { return reservationId; }
        public String getErrorMessage() { return errorMessage; }
    }
}