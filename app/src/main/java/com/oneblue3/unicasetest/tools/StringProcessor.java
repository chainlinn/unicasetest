package com.oneblue3.unicasetest.tools;

import java.util.regex.Pattern;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * 字符串处理工具类
 * 演示文档中无依赖工具测试：字符串处理、验证、清理、格式化
 * 适用场景：数据验证、格式转换、编解码、文本处理等
 */
public class StringProcessor {
    
    // 常用正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );
    
    private static final Pattern CHINESE_PATTERN = Pattern.compile(
        "[\\u4e00-\\u9fff]+"
    );
    
    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @return 是否有效
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * 验证手机号格式（中国大陆）
     * @param phone 手机号
     * @return 是否有效
     */
    public boolean isValidChinesePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }
    
    /**
     * 清理HTML标签
     * @param html HTML字符串
     * @return 纯文本
     */
    public String stripHtmlTags(String html) {
        if (html == null) {
            return null;
        }
        // 移除HTML标签，保留内容
        return html.replaceAll("<[^>]+>", "").trim();
    }
    
    /**
     * 格式化手机号（添加分隔符）
     * @param phone 原始手机号
     * @return 格式化后手机号 (例: 138-1234-5678)
     */
    public String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return phone;
        }
        
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        if (!isValidChinesePhone(cleanPhone)) {
            throw new IllegalArgumentException("无效的手机号格式");
        }
        
        return cleanPhone.substring(0, 3) + "-" + 
               cleanPhone.substring(3, 7) + "-" + 
               cleanPhone.substring(7);
    }
    
    /**
     * 字符串Base64编码
     * @param input 输入字符串
     * @return Base64编码结果
     */
    public String encodeBase64(String input) {
        if (input == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 字符串Base64解码
     * @param encoded Base64编码字符串
     * @return 解码结果
     */
    public String decodeBase64(String encoded) {
        if (encoded == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(encoded);
            return new String(decoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的Base64编码", e);
        }
    }
    
    /**
     * 检查是否包含中文字符
     * @param text 待检查文本
     * @return 是否包含中文
     */
    public boolean containsChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return CHINESE_PATTERN.matcher(text).find();
    }
    
    /**
     * 首字母大写
     * @param text 输入文本
     * @return 首字母大写的文本
     */
    public String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    
    /**
     * 驼峰命名转下划线
     * @param camelCase 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public String camelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    /**
     * 下划线命名转驼峰
     * @param snakeCase 下划线命名字符串
     * @return 驼峰命名字符串
     */
    public String snakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        String[] parts = snakeCase.split("_");
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (i == 0) {
                result.append(part.toLowerCase());
            } else {
                result.append(capitalize(part));
            }
        }
        
        return result.toString();
    }
    
    /**
     * 截断文本并添加省略号
     * @param text 原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    public String truncate(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (maxLength < 0) {
            throw new IllegalArgumentException("最大长度不能为负数");
        }
        if (text.length() <= maxLength) {
            return text;
        }
        
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        
        return text.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * 隐藏敏感信息（如手机号中间4位）
     * @param sensitive 敏感信息
     * @param showPrefix 显示前缀长度
     * @param showSuffix 显示后缀长度
     * @return 隐藏后的字符串
     */
    public String maskSensitiveInfo(String sensitive, int showPrefix, int showSuffix) {
        if (sensitive == null || sensitive.isEmpty()) {
            return sensitive;
        }
        if (showPrefix < 0 || showSuffix < 0) {
            throw new IllegalArgumentException("显示长度不能为负数");
        }
        if (showPrefix + showSuffix >= sensitive.length()) {
            return sensitive;
        }
        
        String prefix = sensitive.substring(0, showPrefix);
        String suffix = sensitive.substring(sensitive.length() - showSuffix);
        int maskLength = sensitive.length() - showPrefix - showSuffix;
        String mask = "*".repeat(Math.max(0, maskLength));
        
        return prefix + mask + suffix;
    }
}