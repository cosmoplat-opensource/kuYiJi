/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * MD5Utils 工具类单元测试（MD5 哈希已移除，业务统一改用 Sha256Utils）
 */
public class MD5UtilsTest {

    @Test
    public void testEncodeHexString() {
        byte[] bytes = {(byte) 0x5d, (byte) 0x41};
        String hex = MD5Utils.encodeHexString(bytes);
        assertEquals("5d41", hex);
    }

    @Test
    public void testConvertMD5() {
        String original = "hello";
        String encrypted = MD5Utils.convertMD5(original);
        assertNotEquals(original, encrypted);
        // Double encryption returns original
        String decrypted = MD5Utils.convertMD5(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    public void testConvertMD5_empty() {
        String result = MD5Utils.convertMD5("");
        assertEquals("", result);
    }
}
