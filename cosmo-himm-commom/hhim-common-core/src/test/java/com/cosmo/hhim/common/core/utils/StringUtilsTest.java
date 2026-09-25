/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * StringUtils 工具类单元测试
 */
public class StringUtilsTest {

    // ==================== nvl ====================
    @Test
    public void testNvl_withNonNullValue_returnsValue() {
        assertEquals("hello", StringUtils.nvl("hello", "default"));
    }

    @Test
    public void testNvl_withNullValue_returnsDefault() {
        assertEquals("default", StringUtils.nvl(null, "default"));
    }

    @Test
    public void testNvl_withNullValueAndNullDefault_returnsNull() {
        assertNull(StringUtils.nvl(null, null));
    }

    // ==================== isEmpty/isNotEmpty - Collection ====================
    @Test
    public void testIsEmpty_collection_null() {
        assertTrue(StringUtils.isEmpty((Collection<?>) null));
    }

    @Test
    public void testIsEmpty_collection_empty() {
        assertTrue(StringUtils.isEmpty(new ArrayList<>()));
    }

    @Test
    public void testIsEmpty_collection_notEmpty() {
        assertFalse(StringUtils.isEmpty(Arrays.asList("a")));
    }

    @Test
    public void testIsNotEmpty_collection_notEmpty() {
        assertTrue(StringUtils.isNotEmpty(Arrays.asList("a")));
    }

    // ==================== isEmpty/isNotEmpty - Object[] ====================
    @Test
    public void testIsEmpty_objectArray_null() {
        assertTrue(StringUtils.isEmpty((Object[]) null));
    }

    @Test
    public void testIsEmpty_objectArray_empty() {
        assertTrue(StringUtils.isEmpty(new Object[0]));
    }

    @Test
    public void testIsEmpty_objectArray_notEmpty() {
        assertFalse(StringUtils.isEmpty(new Object[]{"a"}));
    }

    @Test
    public void testIsNotEmpty_objectArray_notEmpty() {
        assertTrue(StringUtils.isNotEmpty(new Object[]{"a"}));
    }

    // ==================== isEmpty/isNotEmpty - Map ====================
    @Test
    public void testIsEmpty_map_null() {
        assertTrue(StringUtils.isEmpty((Map<?, ?>) null));
    }

    @Test
    public void testIsEmpty_map_empty() {
        assertTrue(StringUtils.isEmpty(new HashMap<>()));
    }

    @Test
    public void testIsEmpty_map_notEmpty() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertFalse(StringUtils.isEmpty(map));
    }

    @Test
    public void testIsNotEmpty_map_notEmpty() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        assertTrue(StringUtils.isNotEmpty(map));
    }

    // ==================== isEmpty/isNotEmpty - String ====================
    @Test
    public void testIsEmpty_string_null() {
        assertTrue(StringUtils.isEmpty((String) null));
    }

    @Test
    public void testIsEmpty_string_empty() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void testIsEmpty_string_whitespace() {
        assertTrue(StringUtils.isEmpty("   "));
    }

    @Test
    public void testIsEmpty_string_notEmpty() {
        assertFalse(StringUtils.isEmpty("hello"));
    }

    @Test
    public void testIsNotEmpty_string_notEmpty() {
        assertTrue(StringUtils.isNotEmpty("hello"));
    }

    // ==================== isNull/isNotNull ====================
    @Test
    public void testIsNull() {
        assertTrue(StringUtils.isNull(null));
        assertFalse(StringUtils.isNull("hello"));
    }

    @Test
    public void testIsNotNull() {
        assertTrue(StringUtils.isNotNull("hello"));
        assertFalse(StringUtils.isNotNull(null));
    }

    // ==================== isArray ====================
    @Test
    public void testIsArray() {
        assertTrue(StringUtils.isArray(new String[]{"a"}));
        assertFalse(StringUtils.isArray("hello"));
        assertFalse(StringUtils.isArray(null));
    }

    // ==================== trim ====================
    @Test
    public void testTrim() {
        assertEquals("hello", StringUtils.trim("  hello  "));
        assertEquals("", StringUtils.trim(null));
        assertEquals("", StringUtils.trim("   "));
    }

    // ==================== substring ====================
    @Test
    public void testSubstring_start() {
        assertEquals("world", StringUtils.substring("hello world", 6));
        assertEquals("world", StringUtils.substring("hello world", -5));
    }

    @Test
    public void testSubstring_startEnd() {
        assertEquals("hello", StringUtils.substring("hello world", 0, 5));
        assertEquals("world", StringUtils.substring("hello world", 6, 11));
    }

    @Test
    public void testSubstring_null() {
        assertEquals("", StringUtils.substring(null, 0));
        assertEquals("", StringUtils.substring(null, 0, 5));
    }

    @Test
    public void testSubstring_startOutOfBounds() {
        assertEquals("", StringUtils.substring("hi", 5));
    }

    @Test
    public void testSubstring_startGreaterThanEnd() {
        assertEquals("", StringUtils.substring("hello", 5, 0));
    }

    // ==================== format ====================
    @Test
    public void testFormat() {
        assertEquals("this is a for b", StringUtils.format("this is {} for {}", "a", "b"));
    }

    @Test
    public void testFormat_emptyTemplate() {
        assertEquals("", StringUtils.format("", "a"));
    }

    @Test
    public void testFormat_nullTemplate() {
        assertNull(StringUtils.format(null, "a"));
    }

    @Test
    public void testFormat_noParams() {
        assertEquals("hello {}", StringUtils.format("hello {}"));
    }

    // ==================== toUnderScoreCase ====================
    @Test
    public void testToUnderScoreCase() {
        assertEquals("hello_world", StringUtils.toUnderScoreCase("HelloWorld"));
        assertEquals("user_name", StringUtils.toUnderScoreCase("UserName"));
    }

    @Test
    public void testToUnderScoreCase_null() {
        assertNull(StringUtils.toUnderScoreCase(null));
    }

    // ==================== inStringIgnoreCase ====================
    @Test
    public void testInStringIgnoreCase_true() {
        assertTrue(StringUtils.inStringIgnoreCase("hello", "HELLO", "world"));
    }

    @Test
    public void testInStringIgnoreCase_false() {
        assertFalse(StringUtils.inStringIgnoreCase("hello", "world", "foo"));
    }

    @Test
    public void testInStringIgnoreCase_null() {
        assertFalse(StringUtils.inStringIgnoreCase(null, "hello"));
    }

    // ==================== convertToCamelCase ====================
    @Test
    public void testConvertToCamelCase() {
        assertEquals("HelloWorld", StringUtils.convertToCamelCase("HELLO_WORLD"));
        assertEquals("UserName", StringUtils.convertToCamelCase("user_name"));
    }

    @Test
    public void testConvertToCamelCase_null() {
        assertEquals("", StringUtils.convertToCamelCase(null));
    }

    @Test
    public void testConvertToCamelCase_empty() {
        assertEquals("", StringUtils.convertToCamelCase(""));
    }

    @Test
    public void testConvertToCamelCase_noUnderscore() {
        assertEquals("Hello", StringUtils.convertToCamelCase("hello"));
    }

    // ==================== humpToUnderline ====================
    @Test
    public void testHumpToUnderline() {
        assertEquals("user_name", StringUtils.humpToUnderline("userName"));
        assertEquals("hello_world", StringUtils.humpToUnderline("helloWorld"));
    }

    @Test
    public void testHumpToUnderline_noUpper() {
        assertEquals("username", StringUtils.humpToUnderline("username"));
    }

    // ==================== toCamelCase ====================
    @Test
    public void testToCamelCase() {
        assertEquals("userName", StringUtils.toCamelCase("user_name"));
        assertEquals("helloWorld", StringUtils.toCamelCase("HELLO_WORLD"));
    }

    @Test
    public void testToCamelCase_null() {
        assertNull(StringUtils.toCamelCase(null));
    }

    // ==================== matches/isMatch ====================
    @Test
    public void testMatches_true() {
        List<String> patterns = Arrays.asList("/api/**", "/admin/**");
        assertTrue(StringUtils.matches("/api/user/list", patterns));
    }

    @Test
    public void testMatches_false() {
        List<String> patterns = Arrays.asList("/api/**");
        assertFalse(StringUtils.matches("/public/info", patterns));
    }

    @Test
    public void testMatches_empty() {
        assertFalse(StringUtils.matches("/api", new ArrayList<>()));
        assertFalse(StringUtils.matches("", Arrays.asList("/api/**")));
    }

    // ==================== cast ====================
    @Test
    public void testCast() {
        Object obj = "hello";
        String result = StringUtils.cast(obj);
        assertEquals("hello", result);
    }

    // ==================== getObjectString ====================
    @Test
    public void testGetObjectString_null() {
        assertNull(StringUtils.getObjectString(null));
    }

    @Test
    public void testGetObjectString_string() {
        assertEquals("hello", StringUtils.getObjectString("hello"));
    }

    @Test
    public void testGetObjectString_long() {
        assertEquals("123", StringUtils.getObjectString(123L));
    }

    @Test
    public void testGetObjectString_integer() {
        assertEquals("456", StringUtils.getObjectString(456));
    }

    @Test
    public void testGetObjectString_exception() {
        Exception e = new RuntimeException("test error");
        String result = StringUtils.getObjectString(e);
        assertTrue(result.contains("RuntimeException"));
        assertTrue(result.contains("test error"));
    }

    @Test
    public void testGetObjectString_list() {
        List<String> list = Arrays.asList("a", "b");
        String result = StringUtils.getObjectString(list);
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
    }

    // ==================== convertObject ====================
    @Test
    public void testConvertObject_integer() {
        assertEquals(Integer.valueOf(123), StringUtils.convertObject("123", Integer.class));
        assertEquals(Integer.valueOf(0), StringUtils.convertObject(null, int.class));
    }

    @Test
    public void testConvertObject_long() {
        assertEquals(Long.valueOf(123L), StringUtils.convertObject("123", Long.class));
        assertEquals(Long.valueOf(0L), StringUtils.convertObject(null, long.class));
    }

    @Test
    public void testConvertObject_boolean() {
        assertTrue(StringUtils.convertObject("true", Boolean.class));
        assertFalse(StringUtils.convertObject(null, boolean.class));
    }

    @Test
    public void testConvertObject_double() {
        assertEquals(Double.valueOf(3.14), StringUtils.convertObject("3.14", Double.class));
        assertEquals(Double.valueOf(0.0), StringUtils.convertObject(null, double.class));
    }

    @Test
    public void testConvertObject_string() {
        assertEquals("hello", StringUtils.convertObject("hello", String.class));
    }

    // ==================== usingRandom ====================
    @Test
    public void testUsingRandom() {
        String random = StringUtils.usingRandom(8);
        assertEquals(8, random.length());
    }

    @Test
    public void testUsingRandom_zero() {
        String random = StringUtils.usingRandom(0);
        assertEquals(0, random.length());
    }

    // ==================== jsonToStringExtendContent ====================
    @Test
    public void testJsonToStringExtendContent_na() {
        assertEquals("NA", StringUtils.jsonToStringExtendContent(null));
        assertEquals("NA", StringUtils.jsonToStringExtendContent("NA"));
    }

    // ==================== jsonToStringExtendContentNoNa ====================
    @Test
    public void testJsonToStringExtendContentNoNa_na() {
        assertEquals("", StringUtils.jsonToStringExtendContentNoNa(null));
        assertEquals("", StringUtils.jsonToStringExtendContentNoNa("NA"));
    }
}
