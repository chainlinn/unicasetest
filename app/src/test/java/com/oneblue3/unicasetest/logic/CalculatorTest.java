package com.oneblue3.unicasetest.logic;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * 计算器纯逻辑测试
 * 演示文档中纯逻辑测试的最佳实践：
 * - 执行速度极快（毫秒级）
 * - 无外部依赖，完全独立
 * - 覆盖边界条件和异常情况
 * - 使用JUnit 4 + Truth进行现代化测试
 */
public class CalculatorTest {
    
    private Calculator calculator;
    
    @Before
    public void setUp() {
        calculator = new Calculator();
    }
    
    @Test
    public void add_normalNumbers_shouldReturnCorrectSum() {
        // 测试基本加法
        int result = calculator.add(5, 3);
        
        // 使用Truth进行流式断言
        assertThat(result).isEqualTo(8);
    }
    
    @Test
    public void add_withZero_shouldHandleCorrectly() {
        assertThat(calculator.add(0, 5)).isEqualTo(5);
        assertThat(calculator.add(5, 0)).isEqualTo(5);
        assertThat(calculator.add(0, 0)).isEqualTo(0);
    }
    
    @Test
    public void add_negativeNumbers_shouldCalculateCorrectly() {
        assertThat(calculator.add(-5, 3)).isEqualTo(-2);
        assertThat(calculator.add(-5, -3)).isEqualTo(-8);
        assertThat(calculator.add(5, -3)).isEqualTo(2);
    }
    
    @Test
    public void divide_normalInputs_shouldReturnExpected() {
        assertThat(calculator.divide(10, 2)).isWithin(0.001).of(5.0);
        assertThat(calculator.divide(15, 3)).isWithin(0.001).of(5.0);
        assertThat(calculator.divide(7, 2)).isWithin(0.001).of(3.5);
        assertThat(calculator.divide(-10, 2)).isWithin(0.001).of(-5.0);
        assertThat(calculator.divide(0, 5)).isWithin(0.001).of(0.0);
    }
    
    @Test
    public void divide_byZero_shouldThrowException() {
        try {
            calculator.divide(10, 0);
            fail("应该抛出ArithmeticException");
        } catch (ArithmeticException e) {
            assertThat(e.getMessage()).contains("除数不能为零");
        }
    }
    
    @Test
    public void subtract_shouldCalculateCorrectly() {
        assertThat(calculator.subtract(10, 3)).isEqualTo(7);
        assertThat(calculator.subtract(5, 10)).isEqualTo(-5);
        assertThat(calculator.subtract(-5, -3)).isEqualTo(-2);
    }
    
    @Test
    public void multiply_shouldCalculateCorrectly() {
        assertThat(calculator.multiply(5, 3)).isEqualTo(15L);
        assertThat(calculator.multiply(-5, 3)).isEqualTo(-15L);
        assertThat(calculator.multiply(0, 100)).isEqualTo(0L);
    }
    
    @Test
    public void sqrt_validInputs_shouldReturnCorrectValue() {
        assertThat(calculator.sqrt(0)).isWithin(0.001).of(0.0);
        assertThat(calculator.sqrt(1)).isWithin(0.001).of(1.0);
        assertThat(calculator.sqrt(4)).isWithin(0.001).of(2.0);
        assertThat(calculator.sqrt(9)).isWithin(0.001).of(3.0);
        assertThat(calculator.sqrt(16)).isWithin(0.001).of(4.0);
        assertThat(calculator.sqrt(25)).isWithin(0.001).of(5.0);
    }
    
    @Test
    public void sqrt_negativeInput_shouldThrowException() {
        try {
            calculator.sqrt(-1);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("不能计算负数的平方根");
        }
    }
    
    @Test
    public void factorial_validInputs_shouldReturnCorrectValue() {
        assertThat(calculator.factorial(0)).isEqualTo(1L);
        assertThat(calculator.factorial(1)).isEqualTo(1L);
        assertThat(calculator.factorial(2)).isEqualTo(2L);
        assertThat(calculator.factorial(3)).isEqualTo(6L);
        assertThat(calculator.factorial(4)).isEqualTo(24L);
        assertThat(calculator.factorial(5)).isEqualTo(120L);
    }
    
    @Test
    public void factorial_negativeInput_shouldThrowException() {
        try {
            calculator.factorial(-1);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("不能计算负数的阶乘");
        }
    }
    
    @Test
    public void isPrime_variousNumbers_shouldReturnCorrectResult() {
        assertThat(calculator.isPrime(1)).isFalse();
        assertThat(calculator.isPrime(2)).isTrue();
        assertThat(calculator.isPrime(3)).isTrue();
        assertThat(calculator.isPrime(4)).isFalse();
        assertThat(calculator.isPrime(5)).isTrue();
        assertThat(calculator.isPrime(6)).isFalse();
        assertThat(calculator.isPrime(7)).isTrue();
        assertThat(calculator.isPrime(8)).isFalse();
        assertThat(calculator.isPrime(9)).isFalse();
        assertThat(calculator.isPrime(10)).isFalse();
        assertThat(calculator.isPrime(11)).isTrue();
        assertThat(calculator.isPrime(17)).isTrue();
        assertThat(calculator.isPrime(25)).isFalse();
        assertThat(calculator.isPrime(29)).isTrue();
    }
    
    @Test
    public void isPrime_nonPositiveNumbers_shouldReturnFalse() {
        assertThat(calculator.isPrime(0)).isFalse();
        assertThat(calculator.isPrime(-1)).isFalse();
        assertThat(calculator.isPrime(-5)).isFalse();
        assertThat(calculator.isPrime(-10)).isFalse();
    }
}