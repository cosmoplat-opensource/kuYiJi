/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import com.github.pagehelper.Page;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * PageUtils 分页工具类单元测试
 */
public class PageUtilsTest {

    @Test
    public void testListToPage_emptyTargetList() {
        List<String> sourceList = new ArrayList<>();
        List<String> targetList = new ArrayList<>();
        Page<String> result = PageUtils.listToPage(sourceList, targetList);
        assertNotNull(result);
        assertEquals(0L, result.getTotal());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testListToPage_sourceIsNotPage() {
        List<String> sourceList = Arrays.asList("a", "b", "c");
        List<String> targetList = Arrays.asList("x", "y");
        Page<String> result = PageUtils.listToPage(sourceList, targetList);
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
        assertEquals(2, result.size());
        assertTrue(result.contains("x"));
        assertTrue(result.contains("y"));
    }

    @Test
    public void testListToPage_sourceHasNull() {
        List<String> sourceList = null;
        List<String> targetList = Arrays.asList("a", "b");
        Page<String> result = PageUtils.listToPage(sourceList, targetList);
        assertNotNull(result);
        assertEquals(2L, result.getTotal());
    }
}
