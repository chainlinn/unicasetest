package com.oneblue3.unicasetest.logic;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.oneblue3.unicasetest.logic.UserValidator.PasswordStrength;

import org.junit.Before;
import org.junit.Test;

/**
 * 用户验证器纯逻辑测试
 * 演示文档中字符串处理和验证逻辑的测试最佳实践：
 * - JUnit 4测试覆盖多种输入场景
 * - 边界条件和异常情况处理
 * - 复杂业务规则验证
 * - Truth断言库的流式API使用
 */
public class UserValidatorTest {
    
    private UserValidator validator;
    
    @Before
    public void setUp() {
        validator = new UserValidator();
    }
    
    @Test
    public void validateEmail_validFormats_shouldReturnTrue() {
        assertThat(validator.isValidEmail("valid@example.com")).isTrue();
        assertThat(validator.isValidEmail("user.name@company.org")).isTrue();
        assertThat(validator.isValidEmail("test123@gmail.com")).isTrue();
        assertThat(validator.isValidEmail("user+tag@example.co.uk")).isTrue();
    }
    
    @Test
    public void validateEmail_invalidFormats_shouldReturnFalse() {
        assertThat(validator.isValidEmail("invalid.email")).isFalse();
        assertThat(validator.isValidEmail("@example.com")).isFalse();
        assertThat(validator.isValidEmail("user@")).isFalse();
        assertThat(validator.isValidEmail("user@@example.com")).isFalse();
        assertThat(validator.isValidEmail("user@.com")).isFalse();
        assertThat(validator.isValidEmail("user@example.")).isFalse();
    }
    
    @Test
    public void validateEmail_nullAndEmpty_shouldReturnFalse() {
        assertThat(validator.isValidEmail(null)).isFalse();
        assertThat(validator.isValidEmail("")).isFalse();
        assertThat(validator.isValidEmail("   ")).isFalse();
        assertThat(validator.isValidEmail("\t")).isFalse();
        assertThat(validator.isValidEmail("\n")).isFalse();
    }
    
    @Test
    public void validatePhone_validFormats_shouldReturnTrue() {
        assertThat(validator.isValidPhone("13812345678")).isTrue();
        assertThat(validator.isValidPhone("15987654321")).isTrue();
        assertThat(validator.isValidPhone("18666888999")).isTrue();
        assertThat(validator.isValidPhone("19123456789")).isTrue();
    }
    
    @Test
    public void validatePhone_invalidFormats_shouldReturnFalse() {
        assertThat(validator.isValidPhone("12345678901")).isFalse(); // 以12开头
        assertThat(validator.isValidPhone("1381234567")).isFalse();  // 长度不够
        assertThat(validator.isValidPhone("138123456789")).isFalse(); // 长度过长
        assertThat(validator.isValidPhone("23812345678")).isFalse();  // 不以1开头
        assertThat(validator.isValidPhone("1081234567")).isFalse();   // 第二位是0
    }
    
    @Test
    public void checkPasswordStrength_strongPassword_shouldPass() {
        String strongPassword = "SecurePass123!";
        
        PasswordStrength strength = validator.checkPasswordStrength(strongPassword);
        
        assertThat(strength.isValid()).isTrue();
        assertThat(strength.getScore()).isAtLeast(8);
        assertThat(strength.getMissingRequirements()).isEmpty();
        assertThat(strength.getStrengthLevel()).isAnyOf("强", "非常强");
    }
    
    @Test
    public void checkPasswordStrength_weakPassword_shouldFail() {
        String weakPassword = "123";
        
        PasswordStrength strength = validator.checkPasswordStrength(weakPassword);
        
        assertThat(strength.isValid()).isFalse();
        assertThat(strength.getScore()).isLessThan(5);
        assertThat(strength.getMissingRequirements()).containsAtLeast(
            "至少8个字符", "包含大写字母", "包含小写字母", "包含特殊字符"
        );
        assertThat(strength.getStrengthLevel()).isAnyOf("弱", "非常弱");
    }
    
    @Test
    public void checkPasswordStrength_mediumPassword_shouldBePartiallyValid() {
        String mediumPassword = "Password123"; // 缺少特殊字符
        
        PasswordStrength strength = validator.checkPasswordStrength(mediumPassword);
        
        assertThat(strength.isValid()).isFalse();
        assertThat(strength.getScore()).isAtLeast(6);
        assertThat(strength.getMissingRequirements()).contains("包含特殊字符");
        assertThat(strength.getStrengthLevel()).isAnyOf("中等", "强");
    }
    
    @Test
    public void checkPasswordStrength_nullPassword_shouldReturnInvalid() {
        PasswordStrength strength = validator.checkPasswordStrength(null);
        
        assertThat(strength.isValid()).isFalse();
        assertThat(strength.getScore()).isEqualTo(0);
        assertThat(strength.getMissingRequirements()).contains("密码不能为空");
    }
    
    @Test
    public void checkPasswordStrength_veryStrongPassword_shouldGetHighScore() {
        String veryStrongPassword = "MyVerySecure@Password2024!#$";
        
        PasswordStrength strength = validator.checkPasswordStrength(veryStrongPassword);
        
        assertThat(strength.isValid()).isTrue();
        assertThat(strength.getScore()).isAtLeast(10);
        assertThat(strength.getMissingRequirements()).isEmpty();
        assertThat(strength.getStrengthLevel()).isEqualTo("非常强");
    }
    
    @Test
    public void validateUsername_validFormats_shouldReturnTrue() {
        assertThat(validator.isValidUsername("validuser")).isTrue();
        assertThat(validator.isValidUsername("user123")).isTrue();
        assertThat(validator.isValidUsername("test_user")).isTrue();
        assertThat(validator.isValidUsername("User_Name_123")).isTrue();
    }
    
    @Test
    public void validateUsername_invalidFormats_shouldReturnFalse() {
        assertThat(validator.isValidUsername("ab")).isFalse();           // 太短
        assertThat(validator.isValidUsername("a123456789012345678901")).isFalse(); // 太长
        assertThat(validator.isValidUsername("user-name")).isFalse();    // 包含连字符
        assertThat(validator.isValidUsername("user.name")).isFalse();    // 包含点号
        assertThat(validator.isValidUsername("user name")).isFalse();    // 包含空格
        assertThat(validator.isValidUsername("用户名")).isFalse();         // 包含中文
    }
    
    @Test
    public void validateUsername_nullAndEmpty_shouldReturnFalse() {
        assertThat(validator.isValidUsername(null)).isFalse();
        assertThat(validator.isValidUsername("")).isFalse();
        assertThat(validator.isValidUsername("   ")).isFalse();
        assertThat(validator.isValidUsername("\t")).isFalse();
    }
    
    @Test
    public void validateAge_validAges_shouldReturnTrue() {
        assertThat(validator.isValidAge(18)).isTrue();
        assertThat(validator.isValidAge(25)).isTrue();
        assertThat(validator.isValidAge(65)).isTrue();
        assertThat(validator.isValidAge(120)).isTrue();
    }
    
    @Test
    public void validateAge_invalidAges_shouldReturnFalse() {
        assertThat(validator.isValidAge(17)).isFalse();
        assertThat(validator.isValidAge(121)).isFalse();
        assertThat(validator.isValidAge(0)).isFalse();
        assertThat(validator.isValidAge(-5)).isFalse();
    }
    
    @Test
    public void validateCompleteUserData_shouldWorkCorrectly() {
        // 模拟一个完整的用户注册数据验证
        String email = "john.doe@example.com";
        String phone = "13812345678";
        String username = "johndoe123";
        String password = "SecurePass123!";
        int age = 25;
        
        // 验证所有字段
        assertThat(validator.isValidEmail(email)).isTrue();
        assertThat(validator.isValidPhone(phone)).isTrue();
        assertThat(validator.isValidUsername(username)).isTrue();
        assertThat(validator.isValidAge(age)).isTrue();
        
        PasswordStrength passwordStrength = validator.checkPasswordStrength(password);
        assertThat(passwordStrength.isValid()).isTrue();
        
        // 这个测试演示了如何组合多个验证逻辑
        // 在实际应用中，这种组合验证通常在服务层进行
    }
}