/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.text;

import org.junit.Test;
import java.math.BigDecimal;
import java.math.BigInteger;
import static org.junit.Assert.*;

/**
 * Convert 类型转换器单元测试
 */
public class ConvertTest {

    // ==================== toStr ====================
    @Test
    public void testToStr_null() {
        assertNull(Convert.toStr(null));
    }

    @Test
    public void testToStr_nullWithDefault() {
        assertEquals("default", Convert.toStr(null, "default"));
    }

    @Test
    public void testToStr_string() {
        assertEquals("hello", Convert.toStr("hello"));
    }

    @Test
    public void testToStr_integer() {
        assertEquals("123", Convert.toStr(123));
    }

    // ==================== toChar ====================
    @Test
    public void testToChar() {
        assertEquals(Character.valueOf('a'), Convert.toChar("abc"));
    }

    @Test
    public void testToChar_null() {
        assertNull(Convert.toChar(null));
    }

    @Test
    public void testToChar_nullWithDefault() {
        assertEquals(Character.valueOf('x'), Convert.toChar(null, 'x'));
    }

    @Test
    public void testToChar_char() {
        assertEquals(Character.valueOf('a'), Convert.toChar('a'));
    }

    // ==================== toByte ====================
    @Test
    public void testToByte() {
        assertEquals(Byte.valueOf((byte) 1), Convert.toByte("1"));
    }

    @Test
    public void testToByte_null() {
        assertNull(Convert.toByte(null));
    }

    @Test
    public void testToByte_nullWithDefault() {
        assertEquals(Byte.valueOf((byte) 0), Convert.toByte(null, (byte) 0));
    }

    @Test
    public void testToByte_invalid() {
        assertEquals(Byte.valueOf((byte) 1), Convert.toByte("abc", (byte) 1));
    }

    // ==================== toShort ====================
    @Test
    public void testToShort() {
        assertEquals(Short.valueOf((short) 100), Convert.toShort("100"));
    }

    @Test
    public void testToShort_null() {
        assertNull(Convert.toShort(null));
    }

    @Test
    public void testToShort_nullWithDefault() {
        assertEquals(Short.valueOf((short) 0), Convert.toShort(null, (short) 0));
    }

    @Test
    public void testToShort_invalid() {
        assertEquals(Short.valueOf((short) 1), Convert.toShort("abc", (short) 1));
    }

    // ==================== toNumber ====================
    @Test
    public void testToNumber() {
        assertEquals(100L, Convert.toNumber("100"));
    }

    @Test
    public void testToNumber_null() {
        assertNull(Convert.toNumber(null));
    }

    @Test
    public void testToNumber_nullWithDefault() {
        assertEquals(99, Convert.toNumber(null, 99));
    }

    // ==================== toInt ====================
    @Test
    public void testToInt() {
        assertEquals(Integer.valueOf(100), Convert.toInt("100"));
    }

    @Test
    public void testToInt_null() {
        assertNull(Convert.toInt(null));
    }

    @Test
    public void testToInt_nullWithDefault() {
        assertEquals(Integer.valueOf(0), Convert.toInt(null, 0));
    }

    @Test
    public void testToInt_invalid() {
        assertEquals(Integer.valueOf(1), Convert.toInt("abc", 1));
    }

    // ==================== toIntArray ====================
    @Test
    public void testToIntArray() {
        Integer[] result = Convert.toIntArray("1,2,3");
        assertEquals(3, result.length);
        assertEquals(Integer.valueOf(1), result[0]);
        assertEquals(Integer.valueOf(2), result[1]);
        assertEquals(Integer.valueOf(3), result[2]);
    }

    @Test
    public void testToIntArray_empty() {
        Integer[] result = Convert.toIntArray("");
        assertEquals(0, result.length);
    }

    @Test
    public void testToIntArray_customSplit() {
        Integer[] result = Convert.toIntArray("-", "1-2-3");
        assertEquals(3, result.length);
        assertEquals(Integer.valueOf(1), result[0]);
    }

    // ==================== toLong ====================
    @Test
    public void testToLong() {
        assertEquals(Long.valueOf(100L), Convert.toLong("100"));
    }

    @Test
    public void testToLong_null() {
        assertNull(Convert.toLong(null));
    }

    @Test
    public void testToLong_nullWithDefault() {
        assertEquals(Long.valueOf(0L), Convert.toLong(null, 0L));
    }

    @Test
    public void testToLong_invalid() {
        assertEquals(Long.valueOf(1L), Convert.toLong("abc", 1L));
    }

    // ==================== toLongArray ====================
    @Test
    public void testToLongArray() {
        Long[] result = Convert.toLongArray("1,2,3");
        assertEquals(3, result.length);
        assertEquals(Long.valueOf(1L), result[0]);
    }

    @Test
    public void testToLongArray_empty() {
        Long[] result = Convert.toLongArray("");
        assertEquals(0, result.length);
    }

    // ==================== toStrArray ====================
    @Test
    public void testToStrArray() {
        String[] result = Convert.toStrArray("a,b,c");
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
    }

    // ==================== toDouble ====================
    @Test
    public void testToDouble() {
        assertEquals(Double.valueOf(3.14), Convert.toDouble("3.14"));
    }

    @Test
    public void testToDouble_null() {
        assertNull(Convert.toDouble(null));
    }

    @Test
    public void testToDouble_nullWithDefault() {
        assertEquals(Double.valueOf(0.0), Convert.toDouble(null, 0.0));
    }

    // ==================== toFloat ====================
    @Test
    public void testToFloat() {
        assertEquals(Float.valueOf(3.14f), Convert.toFloat("3.14"));
    }

    @Test
    public void testToFloat_null() {
        assertNull(Convert.toFloat(null));
    }

    @Test
    public void testToFloat_nullWithDefault() {
        assertEquals(Float.valueOf(0.0f), Convert.toFloat(null, 0.0f));
    }

    // ==================== toBool ====================
    @Test
    public void testToBool_true() {
        assertTrue(Convert.toBool("true"));
        assertTrue(Convert.toBool("yes"));
        assertTrue(Convert.toBool("ok"));
        assertTrue(Convert.toBool("1"));
    }

    @Test
    public void testToBool_false() {
        assertFalse(Convert.toBool("false"));
        assertFalse(Convert.toBool("no"));
        assertFalse(Convert.toBool("0"));
    }

    @Test
    public void testToBool_null() {
        assertNull(Convert.toBool(null));
    }

    @Test
    public void testToBool_nullWithDefault() {
        assertTrue(Convert.toBool(null, true));
    }

    // ==================== toEnum ====================
    enum TestEnum { A, B, C }

    @Test
    public void testToEnum() {
        assertEquals(TestEnum.A, Convert.toEnum(TestEnum.class, "A"));
    }

    @Test
    public void testToEnum_null() {
        assertNull(Convert.toEnum(TestEnum.class, null));
    }

    @Test
    public void testToEnum_nullWithDefault() {
        assertEquals(TestEnum.B, Convert.toEnum(TestEnum.class, null, TestEnum.B));
    }

    @Test
    public void testToEnum_instance() {
        assertEquals(TestEnum.A, Convert.toEnum(TestEnum.class, TestEnum.A));
    }

    // ==================== toBigInteger ====================
    @Test
    public void testToBigInteger() {
        assertEquals(new BigInteger("100"), Convert.toBigInteger("100"));
    }

    @Test
    public void testToBigInteger_null() {
        assertNull(Convert.toBigInteger(null));
    }

    @Test
    public void testToBigInteger_nullWithDefault() {
        assertEquals(BigInteger.ZERO, Convert.toBigInteger(null, BigInteger.ZERO));
    }

    // ==================== toBigDecimal ====================
    @Test
    public void testToBigDecimal() {
        assertEquals(new BigDecimal("100.5"), Convert.toBigDecimal("100.5"));
    }

    @Test
    public void testToBigDecimal_null() {
        assertNull(Convert.toBigDecimal(null));
    }

    @Test
    public void testToBigDecimal_nullWithDefault() {
        assertEquals(BigDecimal.ZERO, Convert.toBigDecimal(null, BigDecimal.ZERO));
    }

    @Test
    public void testToBigDecimal_invalid() {
        assertEquals(BigDecimal.ONE, Convert.toBigDecimal("abc", BigDecimal.ONE));
    }

    // ==================== digitUppercase ====================
    @Test
    public void testDigitUppercase() {
        String result = Convert.digitUppercase(100.00);
        assertTrue(result.contains("壹佰元"));
    }

    @Test
    public void testDigitUppercase_zero() {
        String result = Convert.digitUppercase(0);
        assertTrue(result.contains("零元整"));
    }

    @Test
    public void testDigitUppercase_negative() {
        String result = Convert.digitUppercase(-100.00);
        assertTrue(result.startsWith("负"));
    }
}
