/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

/**
 * CheckObjectUtils 检查工具类单元测试
 */
public class CheckObjectUtilsTest {

    @Test
    public void testIsEmpty_null() {
        assertTrue(CheckObjectUtils.isEmpty(null));
    }

    @Test
    public void testIsEmpty_emptyString() {
        assertTrue(CheckObjectUtils.isEmpty(""));
    }

    @Test
    public void testIsEmpty_whitespaceString() {
        assertTrue(CheckObjectUtils.isEmpty("   "));
    }

    @Test
    public void testIsEmpty_nonEmptyString() {
        assertFalse(CheckObjectUtils.isEmpty("hello"));
    }

    @Test
    public void testIsEmpty_emptyList() {
        assertTrue(CheckObjectUtils.isEmpty(new ArrayList<>()));
    }

    @Test
    public void testIsEmpty_nonEmptyList() {
        assertFalse(CheckObjectUtils.isEmpty(Arrays.asList("a")));
    }

    @Test
    public void testIsEmpty_emptyMap() {
        assertTrue(CheckObjectUtils.isEmpty(new HashMap<>()));
    }

    @Test
    public void testIsEmpty_nonEmptyMap() {
        Map<String, String> map = new HashMap<>();
        map.put("k", "v");
        assertFalse(CheckObjectUtils.isEmpty(map));
    }

    @Test
    public void testIsEmpty_emptySet() {
        assertTrue(CheckObjectUtils.isEmpty(new HashSet<>()));
    }

    @Test
    public void testIsEmpty_nonEmptySet() {
        Set<String> set = new HashSet<>();
        set.add("a");
        assertFalse(CheckObjectUtils.isEmpty(set));
    }

    @Test
    public void testIsEmpty_emptyArray() {
        assertTrue(CheckObjectUtils.isEmpty(new Object[0]));
    }

    @Test
    public void testIsEmpty_nonEmptyArray() {
        assertFalse(CheckObjectUtils.isEmpty(new Object[]{"a"}));
    }

    @Test
    public void testIsNotEmpty() {
        assertTrue(CheckObjectUtils.isNotEmpty("hello"));
        assertFalse(CheckObjectUtils.isNotEmpty(null));
        assertFalse(CheckObjectUtils.isNotEmpty(""));
    }

    @Test
    public void testIsAnyEmpty_allNonEmpty() {
        assertFalse(CheckObjectUtils.isAnyEmpty("a", "b", "c"));
    }

    @Test
    public void testIsAnyEmpty_hasEmpty() {
        assertTrue(CheckObjectUtils.isAnyEmpty("a", "", "c"));
    }

    @Test
    public void testIsAnyEmpty_null() {
        assertTrue(CheckObjectUtils.isAnyEmpty("a", null, "c"));
    }

    @Test
    public void testIsAnyEmpty_noArgs() {
        assertFalse(CheckObjectUtils.isAnyEmpty());
    }

    @Test
    public void testIsOrEmpty_allNonEmpty() {
        assertFalse(CheckObjectUtils.isOrEmpty("a", "b"));
    }

    @Test
    public void testIsOrEmpty_hasEmpty() {
        assertTrue(CheckObjectUtils.isOrEmpty("a", ""));
    }

    @Test
    public void testIsOrEmpty_nullArray() {
        assertTrue(CheckObjectUtils.isOrEmpty((Object[]) null));
    }

    @Test
    public void testIsOrEmpty_emptyArray() {
        assertTrue(CheckObjectUtils.isOrEmpty());
    }

    @Test
    public void testIsOrNotEmpty_true() {
        assertTrue(CheckObjectUtils.isOrNotEmpty("", "a"));
    }

    @Test
    public void testIsOrNotEmpty_false() {
        assertFalse(CheckObjectUtils.isOrNotEmpty("", null));
    }

    @Test
    public void testIsOrNotEmpty_nullArray() {
        assertFalse(CheckObjectUtils.isOrNotEmpty((Object[]) null));
    }

    @Test
    public void testIsAndEmpty_allEmpty() {
        assertTrue(CheckObjectUtils.isAndEmpty("", null));
    }

    @Test
    public void testIsAndEmpty_hasNonEmpty() {
        assertFalse(CheckObjectUtils.isAndEmpty("", "a"));
    }

    @Test
    public void testIsAndEmpty_nullArray() {
        assertTrue(CheckObjectUtils.isAndEmpty((Object[]) null));
    }
}
