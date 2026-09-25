/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.text;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * StrFormatter 字符串格式化工具单元测试
 */
public class StrFormatterTest {

    @Test
    public void testFormat_simple() {
        assertEquals("this is a for b", StrFormatter.format("this is {} for {}", "a", "b"));
    }

    @Test
    public void testFormat_emptyTemplate() {
        assertEquals("", StrFormatter.format(""));
    }

    @Test
    public void testFormat_nullTemplate() {
        assertNull(StrFormatter.format(null));
    }

    @Test
    public void testFormat_noArgs() {
        assertEquals("hello {}", StrFormatter.format("hello {}"));
    }

    @Test
    public void testFormat_emptyArgs() {
        assertEquals("hello {}", StrFormatter.format("hello {}", new Object[0]));
    }

    @Test
    public void testFormat_morePlaceholdersThanArgs() {
        assertEquals("this is a for {}", StrFormatter.format("this is {} for {}", "a"));
    }

    @Test
    public void testFormat_escapedBraces() {
        // Single backslash before {} treats {} as escaped literal
        assertEquals("this is {} for a", StrFormatter.format("this is \\{} for {}", "a"));
    }

    @Test
    public void testFormat_doubleEscape() {
        assertEquals("this is \\a for b", StrFormatter.format("this is \\\\{} for {}", "a", "b"));
    }

    @Test
    public void testFormat_multiple() {
        assertEquals("a b c", StrFormatter.format("{} {} {}", "a", "b", "c"));
    }

    @Test
    public void testFormat_numberArgs() {
        assertEquals("num: 42", StrFormatter.format("num: {}", 42));
    }

    @Test
    public void testFormat_nullArg() {
        assertEquals("val: null", StrFormatter.format("val: {}", (Object) null));
    }
}
