/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * JwtUtil JWT工具类单元测试
 */
public class JwtUtilTest {

    private static final String TEST_KEY = "testSecretKey1234567890ABCDEFGH!";
    // 长度合法（>=32）但与 TEST_KEY 不同的密钥，用于验签失败测试
    private static final String WRONG_KEY = "wrongKey1234567890abcdefghijklmn";

    @Test
    public void testGetRandomString_length() {
        String random = JwtUtil.getRandomString(32);
        assertEquals(32, random.length());
    }

    @Test
    public void testGetRandomString_alphanumeric() {
        String random = JwtUtil.getRandomString(100);
        assertTrue(random.matches("[a-zA-Z0-9]+"));
    }

    @Test
    public void testGetRandomString_zero() {
        String random = JwtUtil.getRandomString(0);
        assertEquals(0, random.length());
    }

    @Test
    public void testGetTimeout_nullToken() {
        assertEquals(JwtUtil.NOT_VALUE_EXPIRE, JwtUtil.getTimeout(null, TEST_KEY));
    }

    @Test
    public void testNeverExpire() {
        assertEquals(-1L, JwtUtil.NEVER_EXPIRE);
    }

    @Test
    public void testNotValueExpire() {
        assertEquals(-2L, JwtUtil.NOT_VALUE_EXPIRE);
    }

    @Test
    public void testCreateToken_valid() {
        String token = JwtUtil.createToken("wx", "user123", 3600, null, TEST_KEY);
        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    public void testCreateToken_neverExpire() {
        String token = JwtUtil.createToken("web", "admin", JwtUtil.NEVER_EXPIRE, null, TEST_KEY);
        assertNotNull(token);
    }

    @Test(expected = com.cosmo.hhim.common.core.exception.CustomException.class)
    public void testCreateToken_nullKey() {
        JwtUtil.createToken("wx", "user", 3600, null, null);
    }

    @Test(expected = com.cosmo.hhim.common.core.exception.CustomException.class)
    public void testCreateToken_emptyKey() {
        JwtUtil.createToken("wx", "user", 3600, null, "");
    }

    @Test
    public void testParseAndGetLoginId() {
        String token = JwtUtil.createToken("wx", "testUser123", 3600, null, TEST_KEY);
        Object loginId = JwtUtil.getLoginId(token, TEST_KEY);
        assertEquals("testUser123", loginId);
    }

    @Test
    public void testGetTimeout_validToken() {
        String token = JwtUtil.createToken("wx", "user", 3600, null, TEST_KEY);
        long timeout = JwtUtil.getTimeout(token, TEST_KEY);
        assertTrue(timeout > 0);
        assertTrue(timeout <= 3600);
    }

    @Test(expected = com.cosmo.hhim.common.core.exception.JwtNotLoginException.class)
    public void testParseToken_null() {
        JwtUtil.parseToken(null, TEST_KEY);
    }

    @Test(expected = Exception.class)
    public void testParseToken_invalid() {
        JwtUtil.parseToken("invalid.token.string", TEST_KEY);
    }

    @Test(expected = com.cosmo.hhim.common.core.exception.JwtNotLoginException.class)
    public void testParseToken_wrongKey() {
        String token = JwtUtil.createToken("wx", "user", 3600, null, TEST_KEY);
        JwtUtil.parseToken(token, WRONG_KEY);
    }
}
