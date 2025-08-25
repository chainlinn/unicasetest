package com.oneblue3.unicasetest.tools;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * 字符串处理器测试类
 * 演示文档中字符串处理工具测试：数据验证、格式转换、编解码等
 */
public class StringProcessorTest {
    
    private StringProcessor processor;
    
    @Before
    public void setUp() {
        processor = new StringProcessor();
    }
    
    @Test
    public void isValidEmail_validEmails_shouldReturnTrue() {
        assertThat(processor.isValidEmail("test@example.com")).isTrue();
        assertThat(processor.isValidEmail("user.name@domain.org")).isTrue();
        assertThat(processor.isValidEmail("test123@test-domain.net")).isTrue();
        assertThat(processor.isValidEmail("a@b.co")).isTrue();
    }
    
    @Test
    public void isValidEmail_invalidEmails_shouldReturnFalse() {
        assertThat(processor.isValidEmail("invalid.email")).isFalse();
        assertThat(processor.isValidEmail("@domain.com")).isFalse();
        assertThat(processor.isValidEmail("test@")).isFalse();
        assertThat(processor.isValidEmail("test@domain")).isFalse();
        assertThat(processor.isValidEmail("")).isFalse();
        assertThat(processor.isValidEmail(null)).isFalse();
        assertThat(processor.isValidEmail("   ")).isFalse();
    }
    
    @Test
    public void isValidChinesePhone_validPhones_shouldReturnTrue() {
        assertThat(processor.isValidChinesePhone("13812345678")).isTrue();
        assertThat(processor.isValidChinesePhone("15987654321")).isTrue();
        assertThat(processor.isValidChinesePhone("138-1234-5678")).isTrue();
        assertThat(processor.isValidChinesePhone("138 1234 5678")).isTrue();
        assertThat(processor.isValidChinesePhone("19812345678")).isTrue();
    }
    
    @Test
    public void isValidChinesePhone_invalidPhones_shouldReturnFalse() {
        assertThat(processor.isValidChinesePhone("12812345678")).isFalse(); // 不以1[3-9]开头
        assertThat(processor.isValidChinesePhone("1381234567")).isFalse(); // 位数不够
        assertThat(processor.isValidChinesePhone("138123456789")).isFalse(); // 位数过多
        assertThat(processor.isValidChinesePhone("")).isFalse();
        assertThat(processor.isValidChinesePhone(null)).isFalse();
        assertThat(processor.isValidChinesePhone("abcdefghijk")).isFalse();
    }
    
    @Test
    public void stripHtmlTags_htmlContent_shouldReturnPlainText() {
        String html = "<p>Hello <strong>World</strong>!</p>";
        String result = processor.stripHtmlTags(html);
        assertThat(result).isEqualTo("Hello World!");
    }
    
    @Test
    public void stripHtmlTags_complexHtml_shouldReturnCleanText() {
        String html = "<div class='content'><h1>Title</h1><p>Paragraph with <a href='#'>link</a></p></div>";
        String result = processor.stripHtmlTags(html);
        assertThat(result).isEqualTo("TitleParagraph with link");
    }
    
    @Test
    public void stripHtmlTags_noHtml_shouldReturnOriginal() {
        String plainText = "Just plain text";
        String result = processor.stripHtmlTags(plainText);
        assertThat(result).isEqualTo(plainText);
    }
    
    @Test
    public void stripHtmlTags_nullInput_shouldReturnNull() {
        String result = processor.stripHtmlTags(null);
        assertThat(result).isNull();
    }
    
    @Test
    public void formatPhone_validPhone_shouldReturnFormatted() {
        String phone = "13812345678";
        String result = processor.formatPhone(phone);
        assertThat(result).isEqualTo("138-1234-5678");
    }
    
    @Test
    public void formatPhone_alreadyFormatted_shouldReturnFormatted() {
        String phone = "138-1234-5678";
        String result = processor.formatPhone(phone);
        assertThat(result).isEqualTo("138-1234-5678");
    }
    
    @Test
    public void formatPhone_invalidPhone_shouldThrowException() {
        try {
            processor.formatPhone("12345678901");
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无效的手机号格式");
        }
    }
    
    @Test
    public void encodeBase64_normalString_shouldReturnEncoded() {
        String input = "Hello World";
        String result = processor.encodeBase64(input);
        assertThat(result).isEqualTo("SGVsbG8gV29ybGQ=");
    }
    
    @Test
    public void encodeBase64_chineseString_shouldReturnEncoded() {
        String input = "你好世界";
        String result = processor.encodeBase64(input);
        // 验证可以正确编码中文
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }
    
    @Test
    public void encodeBase64_nullInput_shouldReturnNull() {
        String result = processor.encodeBase64(null);
        assertThat(result).isNull();
    }
    
    @Test
    public void decodeBase64_validEncoded_shouldReturnOriginal() {
        String encoded = "SGVsbG8gV29ybGQ=";
        String result = processor.decodeBase64(encoded);
        assertThat(result).isEqualTo("Hello World");
    }
    
    @Test
    public void decodeBase64_invalidEncoded_shouldThrowException() {
        try {
            processor.decodeBase64("invalid base64!");
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("无效的Base64编码");
        }
    }
    
    @Test
    public void encodeDecodeBase64_roundTrip_shouldReturnOriginal() {
        String original = "测试Base64编解码功能";
        String encoded = processor.encodeBase64(original);
        String decoded = processor.decodeBase64(encoded);
        assertThat(decoded).isEqualTo(original);
    }
    
    @Test
    public void containsChinese_chineseText_shouldReturnTrue() {
        assertThat(processor.containsChinese("你好世界")).isTrue();
        assertThat(processor.containsChinese("Hello 世界")).isTrue();
        assertThat(processor.containsChinese("测试")).isTrue();
    }
    
    @Test
    public void containsChinese_englishText_shouldReturnFalse() {
        assertThat(processor.containsChinese("Hello World")).isFalse();
        assertThat(processor.containsChinese("123456")).isFalse();
        assertThat(processor.containsChinese("!@#$%")).isFalse();
    }
    
    @Test
    public void containsChinese_emptyOrNull_shouldReturnFalse() {
        assertThat(processor.containsChinese("")).isFalse();
        assertThat(processor.containsChinese(null)).isFalse();
    }
    
    @Test
    public void capitalize_normalText_shouldReturnCapitalized() {
        assertThat(processor.capitalize("hello")).isEqualTo("Hello");
        assertThat(processor.capitalize("WORLD")).isEqualTo("World");
        assertThat(processor.capitalize("tEST")).isEqualTo("Test");
    }
    
    @Test
    public void capitalize_singleChar_shouldReturnCapitalized() {
        assertThat(processor.capitalize("a")).isEqualTo("A");
        assertThat(processor.capitalize("Z")).isEqualTo("Z");
    }
    
    @Test
    public void capitalize_emptyOrNull_shouldReturnOriginal() {
        assertThat(processor.capitalize("")).isEqualTo("");
        assertThat(processor.capitalize(null)).isNull();
    }
    
    @Test
    public void camelToSnake_camelCase_shouldReturnSnakeCase() {
        assertThat(processor.camelToSnake("userName")).isEqualTo("user_name");
        assertThat(processor.camelToSnake("firstName")).isEqualTo("first_name");
        assertThat(processor.camelToSnake("getUserInfo")).isEqualTo("get_user_info");
    }
    
    @Test
    public void camelToSnake_alreadyLowercase_shouldReturnOriginal() {
        assertThat(processor.camelToSnake("username")).isEqualTo("username");
        assertThat(processor.camelToSnake("test")).isEqualTo("test");
    }
    
    @Test
    public void snakeToCamel_snakeCase_shouldReturnCamelCase() {
        assertThat(processor.snakeToCamel("user_name")).isEqualTo("userName");
        assertThat(processor.snakeToCamel("first_name")).isEqualTo("firstName");
        assertThat(processor.snakeToCamel("get_user_info")).isEqualTo("getUserInfo");
    }
    
    @Test
    public void snakeToCamel_noUnderscore_shouldReturnLowercase() {
        assertThat(processor.snakeToCamel("username")).isEqualTo("username");
        assertThat(processor.snakeToCamel("TEST")).isEqualTo("test");
    }
    
    @Test
    public void camelSnakeConversion_roundTrip_shouldWork() {
        String original = "getUserInfo";
        String snake = processor.camelToSnake(original);
        String backToCamel = processor.snakeToCamel(snake);
        assertThat(backToCamel).isEqualTo(original);
    }
    
    @Test
    public void truncate_shortText_shouldReturnOriginal() {
        String text = "Short";
        String result = processor.truncate(text, 10);
        assertThat(result).isEqualTo(text);
    }
    
    @Test
    public void truncate_longText_shouldTruncateWithEllipsis() {
        String text = "This is a very long text that should be truncated";
        String result = processor.truncate(text, 20);
        assertThat(result).isEqualTo("This is a very lo...");
        assertThat(result).hasLength(20);
    }
    
    @Test
    public void truncate_maxLengthTooSmall_shouldTruncateWithoutEllipsis() {
        String text = "Hello";
        String result = processor.truncate(text, 3);
        assertThat(result).isEqualTo("Hel");
    }
    
    @Test
    public void truncate_nullText_shouldReturnNull() {
        String result = processor.truncate(null, 10);
        assertThat(result).isNull();
    }
    
    @Test
    public void truncate_negativeMaxLength_shouldThrowException() {
        try {
            processor.truncate("test", -1);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("最大长度不能为负数");
        }
    }
    
    @Test
    public void maskSensitiveInfo_phoneNumber_shouldMaskCorrectly() {
        String phone = "13812345678";
        String result = processor.maskSensitiveInfo(phone, 3, 4);
        assertThat(result).isEqualTo("138****5678");
    }
    
    @Test
    public void maskSensitiveInfo_idNumber_shouldMaskCorrectly() {
        String id = "110101199001011234";
        String result = processor.maskSensitiveInfo(id, 6, 4);
        assertThat(result).isEqualTo("110101********1234");
    }
    
    @Test
    public void maskSensitiveInfo_shortText_shouldReturnMasked() {
        String text = "123";
        String result = processor.maskSensitiveInfo(text, 1, 1);
        assertThat(result).isEqualTo("1*3");
    }
    
    @Test
    public void maskSensitiveInfo_emptyText_shouldReturnOriginal() {
        String result = processor.maskSensitiveInfo("", 1, 1);
        assertThat(result).isEqualTo("");
    }
    
    @Test
    public void maskSensitiveInfo_negativeLength_shouldThrowException() {
        try {
            processor.maskSensitiveInfo("test", -1, 1);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).contains("显示长度不能为负数");
        }
    }
}