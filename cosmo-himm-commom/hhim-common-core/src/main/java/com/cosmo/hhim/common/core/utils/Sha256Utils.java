/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 摘要工具类。
 * <p>
 * 用于前后端约定的事件编码等安全场景，替代弱哈希算法（MD5/SHA-1）。
 *
 * @author cosmo-hhim-open Team
 */
public class Sha256Utils {

    public static final String ENCODE = "UTF-8";

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

    private static final ThreadLocal<MessageDigest> MESSAGE_DIGEST_LOCAL = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("SHA-256 algorithm not available", e);
            }
        }
    };

    /**
     * Calculate SHA-256 hex string.
     *
     * @param bytes byte arrays
     * @return SHA-256 hex string of input
     */
    public static String sha256Hex(byte[] bytes) {
        try {
            MessageDigest messageDigest = MESSAGE_DIGEST_LOCAL.get();
            if (messageDigest != null) {
                return encodeHexString(messageDigest.digest(bytes));
            }
            throw new IllegalStateException("MessageDigest get SHA-256 instance error");
        } finally {
            MESSAGE_DIGEST_LOCAL.remove();
        }
    }

    /**
     * Calculate SHA-256 hex string with encode charset.
     *
     * @param value  value
     * @param encode encode charset of input
     * @return SHA-256 hex string of input
     */
    public static String sha256Hex(String value, String encode) {
        try {
            return sha256Hex(value.getBytes(encode));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert a byte array into a visible string.
     */
    public static String encodeHexString(byte[] bytes) {
        int l = bytes.length;

        char[] out = new char[l << 1];

        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & bytes[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & bytes[i]];
        }

        return new String(out);
    }

}
