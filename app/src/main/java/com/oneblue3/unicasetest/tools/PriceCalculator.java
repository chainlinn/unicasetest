package com.oneblue3.unicasetest.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 价格计算工具类
 * 演示文档中无依赖工具测试：纯算法实现，无外部依赖
 * 适用场景：价格计算、折扣处理、税费计算等
 */
public class PriceCalculator {
    
    // 汇率常量（简化示例）
    private static final BigDecimal USD_TO_CNY = new BigDecimal("7.2850");
    private static final BigDecimal EUR_TO_CNY = new BigDecimal("7.8550");
    private static final BigDecimal CNY_TO_USD = new BigDecimal("0.1374");
    
    /**
     * 计算商品折扣价格
     * @param originalPrice 原价
     * @param discountRate 折扣率 (0.0-1.0)
     * @return 折扣后价格
     */
    public BigDecimal calculateDiscountPrice(BigDecimal originalPrice, BigDecimal discountRate) {
        if (originalPrice == null || discountRate == null) {
            throw new IllegalArgumentException("价格和折扣率不能为空");
        }
        if (originalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格不能为负数");
        }
        if (discountRate.compareTo(BigDecimal.ZERO) < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("折扣率必须在0-1之间");
        }
        
        BigDecimal discountAmount = originalPrice.multiply(discountRate);
        return originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算含税价格
     * @param price 不含税价格
     * @param taxRate 税率
     * @return 含税价格
     */
    public BigDecimal calculatePriceWithTax(BigDecimal price, BigDecimal taxRate) {
        if (price == null || taxRate == null) {
            throw new IllegalArgumentException("价格和税率不能为空");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格不能为负数");
        }
        if (taxRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("税率不能为负数");
        }
        
        BigDecimal taxAmount = price.multiply(taxRate);
        return price.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 汇率转换
     * @param amount 金额
     * @param rateType 汇率类型
     * @return 转换后金额
     */
    public double convertCurrency(double amount, String rateType) {
        if (amount < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        if (rateType == null || rateType.trim().isEmpty()) {
            throw new IllegalArgumentException("汇率类型不能为空");
        }
        
        BigDecimal amountBd = new BigDecimal(String.valueOf(amount));
        BigDecimal result;
        
        switch (rateType) {
            case "USD_TO_CNY":
                result = amountBd.multiply(USD_TO_CNY);
                break;
            case "EUR_TO_CNY":
                result = amountBd.multiply(EUR_TO_CNY);
                break;
            case "CNY_TO_USD":
                result = amountBd.multiply(CNY_TO_USD);
                break;
            default:
                throw new IllegalArgumentException("不支持的汇率类型: " + rateType);
        }
        
        return result.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    
    /**
     * 计算会员折扣
     * @param amount 商品金额
     * @param memberLevel 会员等级
     * @return 折扣金额
     */
    public double calculateMemberDiscount(double amount, MemberLevel memberLevel) {
        if (amount < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
        if (memberLevel == null) {
            throw new IllegalArgumentException("会员等级不能为空");
        }
        
        double discountRate = getMemberDiscountRate(amount, memberLevel);
        return amount * discountRate;
    }
    
    /**
     * 获取会员折扣率
     */
    private double getMemberDiscountRate(double amount, MemberLevel level) {
        // 基础折扣率
        double baseRate = switch (level) {
            case BRONZE -> 0.05;
            case SILVER -> 0.10;
            case GOLD -> 0.15;
        };
        
        // 大额消费额外折扣
        if (amount >= 500.0) {
            baseRate += 0.03;
        }
        
        return Math.min(baseRate, 0.20); // 最大折扣率20%
    }
    
    /**
     * 会员等级枚举
     */
    public enum MemberLevel {
        BRONZE, SILVER, GOLD
    }
    
    /**
     * 计算商品总价
     * @param unitPrice 单价
     * @param quantity 数量
     * @return 总价
     */
    public BigDecimal calculateTotalPrice(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null) {
            throw new IllegalArgumentException("单价不能为空");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("单价不能为负数");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("数量不能为负数");
        }
        
        return unitPrice.multiply(new BigDecimal(quantity)).setScale(2, RoundingMode.HALF_UP);
    }
}