package com.oneblue3.unicasetest.tools;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.oneblue3.unicasetest.tools.PriceCalculator.MemberLevel;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * 价格计算器测试类
 * 演示文档中无依赖工具测试：JUnit 4 + Truth，毫秒级执行，100%可预测
 * 测试覆盖：价格计算、折扣逻辑、汇率转换、会员优惠等
 */
public class PriceCalculatorTest {
    
    private PriceCalculator calculator;
    
    @Before
    public void setUp() {
        calculator = new PriceCalculator();
    }
    
    @Test
    public void calculateDiscountPrice_normalCase_shouldReturnCorrect() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discount = new BigDecimal("0.15"); // 15%折扣
        
        BigDecimal result = calculator.calculateDiscountPrice(originalPrice, discount);
        
        assertThat(result).isEqualTo(new BigDecimal("85.00"));
    }
    
    @Test
    public void calculateDiscountPrice_zeroDiscount_shouldReturnOriginal() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discount = new BigDecimal("0.00");
        
        BigDecimal result = calculator.calculateDiscountPrice(originalPrice, discount);
        
        assertThat(result).isEqualTo(originalPrice);
    }
    
    @Test
    public void calculateDiscountPrice_fullDiscount_shouldReturnZero() {
        BigDecimal originalPrice = new BigDecimal("100.00");
        BigDecimal discount = new BigDecimal("1.00"); // 100%折扣
        
        BigDecimal result = calculator.calculateDiscountPrice(originalPrice, discount);
        
        assertThat(result).isEqualTo(new BigDecimal("0.00"));
    }
    
    @Test
    public void calculateDiscountPrice_nullPrice_shouldThrowException() {
        BigDecimal discount = new BigDecimal("0.15");
        
        try {
            calculator.calculateDiscountPrice(null, discount);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("价格和折扣率不能为空");
        }
    }
    
    @Test
    public void calculateDiscountPrice_negativePrice_shouldThrowException() {
        BigDecimal negativePrice = new BigDecimal("-10.00");
        BigDecimal discount = new BigDecimal("0.15");
        
        try {
            calculator.calculateDiscountPrice(negativePrice, discount);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("价格不能为负数");
        }
    }
    
    @Test
    public void calculateDiscountPrice_invalidDiscountRate_shouldThrowException() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal invalidDiscount = new BigDecimal("1.5"); // 150%折扣，无效
        
        try {
            calculator.calculateDiscountPrice(price, invalidDiscount);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("折扣率必须在0-1之间");
        }
    }
    
    @Test
    public void calculatePriceWithTax_normalCase_shouldReturnCorrect() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("0.13"); // 13%税率
        
        BigDecimal result = calculator.calculatePriceWithTax(price, taxRate);
        
        assertThat(result).isEqualTo(new BigDecimal("113.00"));
    }
    
    @Test
    public void calculatePriceWithTax_zeroTax_shouldReturnOriginal() {
        BigDecimal price = new BigDecimal("100.00");
        BigDecimal taxRate = new BigDecimal("0.00");
        
        BigDecimal result = calculator.calculatePriceWithTax(price, taxRate);
        
        assertThat(result).isEqualTo(price);
    }
    
    @Test
    public void convertCurrency_usdToCny_shouldCalculateCorrectly() {
        double usdAmount = 100.0;
        
        double result = calculator.convertCurrency(usdAmount, "USD_TO_CNY");
        
        assertThat(result).isWithin(0.01).of(728.50);
    }
    
    @Test
    public void convertCurrency_eurToCny_shouldCalculateCorrectly() {
        double eurAmount = 50.0;
        
        double result = calculator.convertCurrency(eurAmount, "EUR_TO_CNY");
        
        assertThat(result).isWithin(0.01).of(392.75);
    }
    
    @Test
    public void convertCurrency_cnyToUsd_shouldCalculateCorrectly() {
        double cnyAmount = 1000.0;
        
        double result = calculator.convertCurrency(cnyAmount, "CNY_TO_USD");
        
        assertThat(result).isWithin(0.01).of(137.40);
    }
    
    @Test
    public void convertCurrency_invalidRateType_shouldThrowException() {
        try {
            calculator.convertCurrency(100.0, "INVALID_RATE");
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("不支持的汇率类型");
        }
    }
    
    @Test
    public void convertCurrency_negativeAmount_shouldThrowException() {
        try {
            calculator.convertCurrency(-100.0, "USD_TO_CNY");
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("金额不能为负数");
        }
    }
    
    @Test
    public void calculateMemberDiscount_bronzeLevel_shouldReturnBasicDiscount() {
        double amount = 100.0;
        
        double discount = calculator.calculateMemberDiscount(amount, MemberLevel.BRONZE);
        
        assertThat(discount).isWithin(0.001).of(5.0); // 5%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_silverLevel_shouldReturnMediumDiscount() {
        double amount = 100.0;
        
        double discount = calculator.calculateMemberDiscount(amount, MemberLevel.SILVER);
        
        assertThat(discount).isWithin(0.001).of(10.0); // 10%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_goldLevel_shouldReturnHighDiscount() {
        double amount = 100.0;
        
        double discount = calculator.calculateMemberDiscount(amount, MemberLevel.GOLD);
        
        assertThat(discount).isWithin(0.001).of(15.0); // 15%基础折扣
    }
    
    @Test
    public void calculateMemberDiscount_largeAmountBronze_shouldReturnExtraDiscount() {
        double amount = 500.0; // 大额消费
        
        double discount = calculator.calculateMemberDiscount(amount, MemberLevel.BRONZE);
        
        assertThat(discount).isWithin(0.001).of(40.0); // 5%+3%额外折扣
    }
    
    @Test
    public void calculateMemberDiscount_largeAmountGold_shouldReturnMaxDiscount() {
        double amount = 500.0; // 大额消费
        
        double discount = calculator.calculateMemberDiscount(amount, MemberLevel.GOLD);
        
        assertThat(discount).isWithin(0.001).of(90.0); // 15%+3%额外折扣 = 18%
    }
    
    @Test
    public void calculateMemberDiscount_nullLevel_shouldThrowException() {
        try {
            calculator.calculateMemberDiscount(100.0, null);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("会员等级不能为空");
        }
    }
    
    @Test
    public void calculateTotalPrice_normalCase_shouldReturnCorrect() {
        BigDecimal unitPrice = new BigDecimal("19.99");
        int quantity = 3;
        
        BigDecimal result = calculator.calculateTotalPrice(unitPrice, quantity);
        
        assertThat(result).isEqualTo(new BigDecimal("59.97"));
    }
    
    @Test
    public void calculateTotalPrice_zeroQuantity_shouldReturnZero() {
        BigDecimal unitPrice = new BigDecimal("19.99");
        int quantity = 0;
        
        BigDecimal result = calculator.calculateTotalPrice(unitPrice, quantity);
        
        assertThat(result).isEqualTo(new BigDecimal("0.00"));
    }
    
    @Test
    public void calculateTotalPrice_nullUnitPrice_shouldThrowException() {
        try {
            calculator.calculateTotalPrice(null, 3);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("单价不能为空");
        }
    }
    
    @Test
    public void calculateTotalPrice_negativeQuantity_shouldThrowException() {
        BigDecimal unitPrice = new BigDecimal("19.99");
        
        try {
            calculator.calculateTotalPrice(unitPrice, -1);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("数量不能为负数");
        }
    }
}