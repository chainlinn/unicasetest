package com.oneblue3.unicasetest.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 用户验证器
 * 演示纯逻辑测试：字符串处理、数据验证、业务规则
 * 无任何外部依赖，完全独立的验证逻辑
 */
public class UserValidator {
    
    // 邮箱验证正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    // 手机号验证正则表达式（中国大陆）
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
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
     * 验证手机号格式
     * @param phone 手机号
     * @return 是否有效
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * 检查密码强度
     * @param password 密码
     * @return 密码强度对象
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null) {
            return new PasswordStrength(false, 0, List.of("密码不能为空"));
        }
        
        List<String> missingRequirements = new ArrayList<>();
        int score = 0;
        
        // 检查长度
        if (password.length() < 8) {
            missingRequirements.add("至少8个字符");
        } else {
            score += 2;
        }
        
        // 检查大写字母
        if (!password.matches(".*[A-Z].*")) {
            missingRequirements.add("包含大写字母");
        } else {
            score += 2;
        }
        
        // 检查小写字母
        if (!password.matches(".*[a-z].*")) {
            missingRequirements.add("包含小写字母");
        } else {
            score += 2;
        }
        
        // 检查数字
        if (!password.matches(".*\\d.*")) {
            missingRequirements.add("包含数字");
        } else {
            score += 2;
        }
        
        // 检查特殊字符
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            missingRequirements.add("包含特殊字符");
        } else {
            score += 2;
        }
        
        // 额外加分项
        if (password.length() >= 12) {
            score += 1; // 长度奖励
        }
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            score += 1; // 多个特殊字符奖励
        }
        
        boolean isValid = missingRequirements.isEmpty() && score >= 8;
        return new PasswordStrength(isValid, score, missingRequirements);
    }
    
    /**
     * 验证用户名格式
     * @param username 用户名
     * @return 是否有效
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        
        // 长度检查
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return false;
        }
        
        // 只允许字母、数字、下划线
        return trimmed.matches("^[a-zA-Z0-9_]+$");
    }
    
    /**
     * 验证年龄
     * @param age 年龄
     * @return 是否有效（18-120岁）
     */
    public boolean isValidAge(int age) {
        return age >= 18 && age <= 120;
    }
    
    /**
     * 密码强度结果类
     */
    public static class PasswordStrength {
        private final boolean isValid;
        private final int score;
        private final List<String> missingRequirements;
        
        public PasswordStrength(boolean isValid, int score, List<String> missingRequirements) {
            this.isValid = isValid;
            this.score = score;
            this.missingRequirements = new ArrayList<>(missingRequirements);
        }
        
        public boolean isValid() {
            return isValid;
        }
        
        public int getScore() {
            return score;
        }
        
        public List<String> getMissingRequirements() {
            return new ArrayList<>(missingRequirements);
        }
        
        public String getStrengthLevel() {
            if (score >= 10) return "非常强";
            if (score >= 8) return "强";
            if (score >= 6) return "中等";
            if (score >= 4) return "弱";
            return "非常弱";
        }
    }
}