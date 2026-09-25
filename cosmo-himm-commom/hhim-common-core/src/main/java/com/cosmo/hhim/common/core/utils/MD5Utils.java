/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications by cosmo-hhim-open Team: 2026 - 适配 Ku易记 项目需求
 */

package com.cosmo.hhim.common.core.utils;

/**
 * 摘要/编码工具类（原 MD5 实现已移除，业务统一改用 {@link Sha256Utils} 进行安全哈希）。
 * <p>
 * 保留通用十六进制编码与可逆字符串混淆方法（非哈希，无安全用途）。
 *
 * @author cosmo-hhim-open Team
 */
public class MD5Utils {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
            'e', 'f'};

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

    /**
     * 可逆的的加密解密方法；两次是解密，一次是加密
     */
    public static String convertMD5(String inStr){

        char[] a = inStr.toCharArray();
        for (int i = 0; i < a.length; i++){
            a[i] = (char) (a[i] ^ 't');
        }
        String s = new String(a);
        return s;

    }
    
}

