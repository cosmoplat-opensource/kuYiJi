/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.cosmo.hhim.common.core.exception.CustomException;

public class DecryptUtils {
    //密钥 (需要前端和后端保持一致)十六位作为密钥，部署时通过 todo.aes.key 注入（-D 系统属性或 Spring setKey 注入）；未配置时为 null，视为未启用该功能
    private static String key = System.getProperty("todo.aes.key");

    //密钥偏移量 (需要前端和后端保持一致)十六位作为密钥偏移量，部署时通过 todo.aes.iv 注入（-D 系统属性或 Spring setIv 注入）
    private static String iv = System.getProperty("todo.aes.iv");

    //算法（GCM 认证加密，避免 CBC 的填充预言机攻击面）
    private static final String ALGORITHMSTR = "AES/GCM/NoPadding";

    /**
     * 注入AES密钥（由Spring配置 todo.aes.key 注入）
     */
    public static void setKey(String key) {
        DecryptUtils.key = key;
    }

    /**
     * 注入AES密钥偏移量（由Spring配置 todo.aes.iv 注入）
     */
    public static void setIv(String iv) {
        DecryptUtils.iv = iv;
    }

    /**
     * 校验密钥配置：未配置时明确报错提示，避免静默失败
     */
    private static void assertConfigured() {
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(iv)) {
            throw new CustomException("未配置待办系统AES密钥（todo.aes.key / todo.aes.iv）");
        }
    }

    /**
     * base 64 decode
     * @param base64Code 待解码的base 64 code
     * @return 解码后的byte[]
     * @throws Exception
     */
    public static byte[] base64Decode(String base64Code) throws Exception{
        return StringUtils.isEmpty(base64Code) ? null : Base64.decodeBase64(base64Code);
    }

    /**
     * AES解密
     * @param encryptBytes 待解密的byte[]
     * @return 解密后的String
     * @throws Exception
     */
    public static String aesDecryptByBytes(byte[] encryptBytes) throws Exception {

        assertConfigured();

        Cipher cipher = Cipher.getInstance(ALGORITHMSTR);

        byte[] temp = iv.getBytes("UTF-8");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, temp);

        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), gcmSpec);
        byte[] decryptBytes = cipher.doFinal(encryptBytes);

        return new String(decryptBytes);
    }

    /**
     * 将base 64 code AES解密
     * @param encryptStr 待解密的base 64 code
     * @return 解密后的string
     * @throws Exception
     */
    public static String aesDecrypt(String encryptStr) throws Exception {
        return StringUtils.isEmpty(encryptStr) ? null : aesDecryptByBytes(base64Decode(encryptStr));
    }
}
