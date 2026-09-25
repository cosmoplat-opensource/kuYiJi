/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.utils;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SecurityUtilsTest {

    @Before
    public void setUp() {
        ThreadContext.clear();
    }

    @After
    public void tearDown() {
        ThreadContext.clear();
    }

    @Test
    public void testIsAdmin_true() {
        assertTrue(SecurityUtils.isAdmin(1L));
    }

    @Test
    public void testIsAdmin_false() {
        assertFalse(SecurityUtils.isAdmin(2L));
    }

    @Test
    public void testIsAdmin_null() {
        assertFalse(SecurityUtils.isAdmin(null));
    }

    @Test
    public void testEncryptPassword() {
        String encrypted = SecurityUtils.encryptPassword("123456");
        assertNotNull(encrypted);
        assertTrue(encrypted.startsWith("$2a$10$"));
    }

    @Test
    public void testMatchesPassword() {
        String raw = "testPassword123";
        String encoded = SecurityUtils.encryptPassword(raw);
        assertTrue(SecurityUtils.matchesPassword(raw, encoded));
    }

    @Test
    public void testMatchesPassword_wrong() {
        String encoded = SecurityUtils.encryptPassword("correct");
        assertFalse(SecurityUtils.matchesPassword("wrong", encoded));
    }

    @Test
    public void testGetTokenKey() {
        String tokenKey = SecurityUtils.getTokenKey("myToken", "myUser");
        assertNotNull(tokenKey);
        assertTrue(tokenKey.contains("myUser"));
        assertTrue(tokenKey.contains("myToken"));
    }

    @Test
    public void testGetTokenKeyPortal() {
        String tokenKey = SecurityUtils.getTokenKeyPortal("myToken", "myUser");
        assertNotNull(tokenKey);
        assertTrue(tokenKey.contains("myUser"));
        assertTrue(tokenKey.contains("myToken"));
    }

    @Test
    public void testGetUsername_fromThreadContext() {
        ThreadContext.put(CacheConstants.DETAILS_USERNAME, "testUser");
        String username = SecurityUtils.getUsername();
        assertNotNull(username);
    }

    @Test
    public void testGetUserId_fromThreadContext() {
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, "123");
        Long userId = SecurityUtils.getUserId();
        assertEquals(Long.valueOf(123), userId);
    }
}
