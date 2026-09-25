/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.threadlocal;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

/**
 * ThreadContext 线程上下文单元测试
 */
public class ThreadContextTest {

    @Before
    public void setUp() {
        ThreadContext.clear();
    }

    @After
    public void tearDown() {
        ThreadContext.clear();
    }

    @Test
    public void testPutAndGet() {
        ThreadContext.put("testKey", "testValue");
        assertEquals("testValue", ThreadContext.get("testKey"));
    }

    @Test
    public void testPutAndGet_nullValue() {
        ThreadContext.put("nullKey", null);
        assertEquals("", ThreadContext.get("nullKey"));
    }

    @Test
    public void testPutAndGet_integer() {
        ThreadContext.put("intKey", 123);
        assertEquals("123", ThreadContext.get("intKey"));
    }

    @Test
    public void testGet_notExist() {
        assertNull(ThreadContext.get("nonexistent"));
    }

    @Test
    public void testRemove() {
        ThreadContext.put("key", "value");
        ThreadContext.remove("key");
        assertNull(ThreadContext.get("key"));
    }

    @Test
    public void testClear() {
        ThreadContext.put("key1", "value1");
        ThreadContext.put("key2", "value2");
        ThreadContext.clear();
        assertNull(ThreadContext.get("key1"));
        assertNull(ThreadContext.get("key2"));
    }

    @Test
    public void testGetThreadMap() {
        ThreadContext.put("k1", "v1");
        ThreadContext.put("k2", "v2");
        Map<String, String> map = ThreadContext.getThreadMap();
        assertNotNull(map);
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsKey("k2"));
    }

    @Test
    public void testGetThreadMap_empty() {
        ThreadContext.clear();
        Map<String, String> map = ThreadContext.getThreadMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAll() {
        Map<String, String> input = new HashMap<>();
        input.put("a", "1");
        input.put("b", "2");
        ThreadContext.putAll(input);
        assertEquals("1", ThreadContext.get("a"));
        assertEquals("2", ThreadContext.get("b"));
    }

    @Test
    public void testPutAll_null() {
        ThreadContext.putAll(null);
        // Should not throw
    }
}
