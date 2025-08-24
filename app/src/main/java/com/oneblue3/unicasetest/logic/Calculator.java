package com.oneblue3.unicasetest.logic;

/**
 * 计算器业务逻辑类
 * 演示纯逻辑测试：无外部依赖，只包含数学计算逻辑
 * 这是纯逻辑测试的理想目标 - 快速、稳定、精确
 */
public class Calculator {
    
    /**
     * 加法运算
     * @param a 第一个数
     * @param b 第二个数
     * @return 计算结果
     */
    public int add(int a, int b) {
        return a + b;
    }
    
    /**
     * 减法运算
     * @param a 被减数
     * @param b 减数
     * @return 计算结果
     */
    public int subtract(int a, int b) {
        return a - b;
    }
    
    /**
     * 乘法运算
     * @param a 第一个数
     * @param b 第二个数
     * @return 计算结果
     */
    public long multiply(int a, int b) {
        return (long) a * b;
    }
    
    /**
     * 除法运算
     * @param dividend 被除数
     * @param divisor 除数
     * @return 计算结果
     * @throws ArithmeticException 当除数为0时抛出异常
     */
    public double divide(int dividend, int divisor) {
        if (divisor == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return (double) dividend / divisor;
    }
    
    /**
     * 计算平方根
     * @param number 输入数字
     * @return 平方根结果
     * @throws IllegalArgumentException 当输入负数时抛出异常
     */
    public double sqrt(double number) {
        if (number < 0) {
            throw new IllegalArgumentException("不能计算负数的平方根");
        }
        return Math.sqrt(number);
    }
    
    /**
     * 计算阶乘
     * @param n 输入数字
     * @return 阶乘结果
     * @throws IllegalArgumentException 当输入负数时抛出异常
     */
    public long factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("不能计算负数的阶乘");
        }
        if (n == 0 || n == 1) {
            return 1;
        }
        
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
    
    /**
     * 判断是否为素数
     * @param number 待检查的数字
     * @return 是否为素数
     */
    public boolean isPrime(int number) {
        if (number <= 1) {
            return false;
        }
        if (number <= 3) {
            return true;
        }
        if (number % 2 == 0 || number % 3 == 0) {
            return false;
        }
        
        for (int i = 5; i * i <= number; i += 6) {
            if (number % i == 0 || number % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}