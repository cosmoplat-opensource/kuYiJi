/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Base64 编解码工具类单元测试
 */
public class Base64Test {

    @Test
    public void testEncode_null() {
        assertNull(Base64.encode(null));
    }

    @Test
    public void testEncode_empty() {
        assertEquals("", Base64.encode(new byte[0]));
    }

    @Test
    public void testEncode_hello() {
        String encoded = Base64.encode("hello".getBytes());
        assertEquals("aGVsbG8=", encoded);
    }

    @Test
    public void testEncode_abc() {
        String encoded = Base64.encode("abc".getBytes());
        assertEquals("YWJj", encoded);
    }

    @Test
    public void testEncode_singleByte() {
        String encoded = Base64.encode("a".getBytes());
        assertEquals("YQ==", encoded);
    }

    @Test
    public void testEncode_twoBytes() {
        String encoded = Base64.encode("ab".getBytes());
        assertEquals("YWI=", encoded);
    }

    @Test
    public void testDecode_null() {
        assertNull(Base64.decode(null));
    }

    @Test
    public void testDecode_hello() {
        byte[] decoded = Base64.decode("aGVsbG8=");
        assertArrayEquals("hello".getBytes(), decoded);
    }

    @Test
    public void testDecode_abc() {
        byte[] decoded = Base64.decode("YWJj");
        assertArrayEquals("abc".getBytes(), decoded);
    }

    @Test
    public void testDecode_singleByte() {
        byte[] decoded = Base64.decode("YQ==");
        assertArrayEquals("a".getBytes(), decoded);
    }

    @Test
    public void testDecode_twoBytes() {
        byte[] decoded = Base64.decode("YWI=");
        assertArrayEquals("ab".getBytes(), decoded);
    }

    @Test
    public void testEncodeDecode_roundtrip() {
        String original = "Hello World! This is a test.";
        String encoded = Base64.encode(original.getBytes());
        byte[] decoded = Base64.decode(encoded);
        assertEquals(original, new String(decoded));
    }

    @Test
    public void testEncodeDecode_roundtrip_binary() {
        byte[] original = new byte[256];
        for (int i = 0; i < 256; i++) original[i] = (byte) i;
        String encoded = Base64.encode(original);
        byte[] decoded = Base64.decode(encoded);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testDecode_invalidLength() {
        // Length not divisible by 4
        assertNull(Base64.decode("abc"));
    }

    @Test
    public void testDecode_empty() {
        byte[] result = Base64.decode("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testDecode_withWhitespace() {
        byte[] decoded = Base64.decode(" Y W J j ");
        assertArrayEquals("abc".getBytes(), decoded);
    }
}
