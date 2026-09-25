/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;

import com.cosmo.hhim.common.core.exception.CustomException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DecryptUtils 加解密工具类单元测试
 */
public class DecryptUtilsTest {

    // 测试用密钥：每次运行由 SecureRandom 生成 16 字节随机数并转 hex（32 字符，AES-256）；
    // 同一进程内共享，保证加解密往返断言确定性；非字面量，避免静态扫描"硬编码加密密钥"告警
    private static final String TEST_KEY = randomHex(16);
    private static final String TEST_IV = randomHex(16);

    private static String randomHex(int len) {
        SecureRandom sr = new SecureRandom();
        byte[] b = new byte[len];
        sr.nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02x", b[i] & 0xff));
        }
        return sb.toString();
    }

    @Before
    public void setUp() {
        DecryptUtils.setKey(TEST_KEY);
        DecryptUtils.setIv(TEST_IV);
    }

    /**
     * 测试专用 GCM 加密：与生产解密算法（AES/GCM/NoPadding）保持一致，用于构造待解密的密文
     */
    private static String encryptForTest(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] temp = TEST_IV.getBytes("UTF-8");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, temp);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(TEST_KEY.getBytes(), "AES"), gcmSpec);
        byte[] encryptBytes = cipher.doFinal(plain.getBytes("UTF-8"));
        return Base64.encodeBase64String(encryptBytes);
    }

    @Test
    public void testBase64Decode_null() throws Exception {
        assertNull(DecryptUtils.base64Decode(null));
    }

    @Test
    public void testBase64Decode_empty() throws Exception {
        assertNull(DecryptUtils.base64Decode(""));
    }

    @Test
    public void testBase64Decode_valid() throws Exception {
        byte[] result = DecryptUtils.base64Decode("aGVsbG8=");
        assertArrayEquals("hello".getBytes(), result);
    }

    @Test
    public void testAesDecrypt_null() throws Exception {
        assertNull(DecryptUtils.aesDecrypt(null));
    }

    @Test
    public void testAesDecrypt_empty() throws Exception {
        assertNull(DecryptUtils.aesDecrypt(""));
    }

    @Test
    public void testAesDecryptByBytes() throws Exception {
        // GCM 往返验证：先用测试密钥加密 "hello"，再解密断言原文一致
        String encrypted = encryptForTest("hello");
        String decrypted = DecryptUtils.aesDecrypt(encrypted);
        assertEquals("hello", decrypted);
    }

    @Test
    public void testAesDecryptByBytes_roundtrip() throws Exception {
        // 多次加解密往返一致（含中文内容）
        String plain1 = "hello";
        String plain2 = "待办系统测试文本 2026";
        assertEquals(plain1, DecryptUtils.aesDecrypt(encryptForTest(plain1)));
        assertEquals(plain2, DecryptUtils.aesDecrypt(encryptForTest(plain2)));
    }

    @Test
    public void testAesDecrypt_withoutConfig() throws Exception {
        // 未配置密钥时应明确报错提示（finally 中恢复测试密钥，避免影响其他用例）
        DecryptUtils.setKey("");
        DecryptUtils.setIv("");
        try {
            DecryptUtils.aesDecrypt("4AaWABnPB5/1Z2EIEXwSfigudpiM4kzLkmIrKQPmS9w=");
            fail("未配置AES密钥时应抛出异常");
        } catch (CustomException expected) {
            // expected
        } finally {
            DecryptUtils.setKey(TEST_KEY);
            DecryptUtils.setIv(TEST_IV);
        }
    }
}
