/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.text;

import org.junit.Test;
import java.nio.charset.Charset;
import static org.junit.Assert.*;

/**
 * CharsetKit 字符集工具类单元测试
 */
public class CharsetKitTest {

    @Test
    public void testCharset_valid() {
        assertEquals(Charset.forName("UTF-8"), CharsetKit.charset("UTF-8"));
        assertEquals(Charset.forName("GBK"), CharsetKit.charset("GBK"));
    }

    @Test
    public void testCharset_empty() {
        assertEquals(Charset.defaultCharset(), CharsetKit.charset(""));
    }

    @Test
    public void testCharset_null() {
        assertEquals(Charset.defaultCharset(), CharsetKit.charset(null));
    }

    @Test
    public void testSystemCharset() {
        String system = CharsetKit.systemCharset();
        assertNotNull(system);
        assertEquals(Charset.defaultCharset().name(), system);
    }

    @Test
    public void testConstants() {
        assertEquals("ISO-8859-1", CharsetKit.ISO_8859_1);
        assertEquals("UTF-8", CharsetKit.UTF_8);
        assertEquals("GBK", CharsetKit.GBK);
        assertNotNull(CharsetKit.CHARSET_UTF_8);
        assertNotNull(CharsetKit.CHARSET_GBK);
        assertNotNull(CharsetKit.CHARSET_ISO_8859_1);
    }

    @Test
    public void testConvert_nullSource() {
        assertNull(CharsetKit.convert(null, "ISO-8859-1", "UTF-8"));
    }

    @Test
    public void testConvert_emptySource() {
        assertEquals("", CharsetKit.convert("", "ISO-8859-1", "UTF-8"));
    }

    @Test
    public void testConvert_sameCharset() {
        String input = "hello";
        String result = CharsetKit.convert(input, "UTF-8", "UTF-8");
        assertEquals(input, result);
    }
}
