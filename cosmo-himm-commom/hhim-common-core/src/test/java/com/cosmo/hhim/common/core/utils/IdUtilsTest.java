/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * IdUtils ID生成工具类单元测试
 */
public class IdUtilsTest {

    @Test
    public void testSimpleUUID() {
        String uuid = IdUtils.simpleUUID();
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        // Should not contain hyphens
        assertFalse(uuid.contains("-"));
    }

    @Test
    public void testFastUUID() {
        String uuid = IdUtils.fastUUID();
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.contains("-"));
    }

    @Test
    public void testRandomUUID() {
        String uuid = IdUtils.randomUUID();
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.contains("-"));
    }

    @Test
    public void testSimpleUUID_uniqueness() {
        Set<String> uuids = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            uuids.add(IdUtils.simpleUUID());
        }
        assertEquals(100, uuids.size());
    }
}
