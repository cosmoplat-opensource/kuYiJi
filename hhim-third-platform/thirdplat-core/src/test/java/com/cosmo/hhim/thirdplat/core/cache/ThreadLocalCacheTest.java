/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.core.cache;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class ThreadLocalCacheTest {

    @Before
    public void setUp() {
        ThreadLocalCache.clearCache();
    }

    @After
    public void tearDown() {
        ThreadLocalCache.clearCache();
    }

    @Test
    public void testSaveAndGetCache() {
        ThreadLocalCache.saveCache("key1", "value1");
        assertEquals("value1", ThreadLocalCache.getCache("key1"));
    }

    @Test
    public void testSaveCache_nullKey() {
        ThreadLocalCache.saveCache(null, "value");
        assertNull(ThreadLocalCache.getCache(null));
    }

    @Test
    public void testSaveCache_emptyKey() {
        ThreadLocalCache.saveCache("", "value");
        assertNull(ThreadLocalCache.getCache(""));
    }

    @Test
    public void testGetCache_nonExistent() {
        assertNull(ThreadLocalCache.getCache("notExist"));
    }

    @Test
    public void testSaveMultiple() {
        ThreadLocalCache.saveCache("k1", "v1");
        ThreadLocalCache.saveCache("k2", "v2");
        assertEquals("v1", ThreadLocalCache.getCache("k1"));
        assertEquals("v2", ThreadLocalCache.getCache("k2"));
    }

    @Test
    public void testSaveAllCache() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        ThreadLocalCache.saveAllCache(map);
        assertEquals("1", ThreadLocalCache.getCache("a"));
        assertEquals("2", ThreadLocalCache.getCache("b"));
    }

    @Test
    public void testSaveAllCache_null() {
        ThreadLocalCache.saveAllCache(null);
        assertNull(ThreadLocalCache.getAllCache());
    }

    @Test
    public void testRemoveCache() {
        ThreadLocalCache.saveCache("key", "value");
        ThreadLocalCache.removeCache("key");
        assertNull(ThreadLocalCache.getCache("key"));
    }

    @Test
    public void testRemoveCache_nonExistent() {
        ThreadLocalCache.removeCache("notExist");
        // Should not throw
    }

    @Test
    public void testClearCache() {
        ThreadLocalCache.saveCache("k1", "v1");
        ThreadLocalCache.saveCache("k2", "v2");
        ThreadLocalCache.clearCache();
        assertNull(ThreadLocalCache.getCache("k1"));
        assertNull(ThreadLocalCache.getCache("k2"));
    }

    @Test
    public void testGetAllCache_empty() {
        assertNull(ThreadLocalCache.getAllCache());
    }

    @Test
    public void testGetAllCache_nonEmpty() {
        ThreadLocalCache.saveCache("key", "value");
        Map<String, Object> all = ThreadLocalCache.getAllCache();
        assertNotNull(all);
        assertTrue(all.containsKey("key"));
    }

    @Test
    public void testGetCache_nullKey() {
        assertNull(ThreadLocalCache.getCache(null));
    }
}
