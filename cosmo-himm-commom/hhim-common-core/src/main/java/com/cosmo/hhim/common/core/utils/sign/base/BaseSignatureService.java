/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign.base;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class BaseSignatureService {

    protected static volatile Wrapper wrapper = new Wrapper() {
    };

    protected static void enableUrlencodeMode() {
        wrapper = new Wrapper() {
            @Override
            public String wrapVal(Object val) {
                String valStr = String.valueOf(val);
                try {
                    return URLEncoder.encode(valStr, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    return valStr;
                }
            }
        };
    }

    protected static String wrapVal(Object val) {
        return wrapper.wrapVal(val);
    }

    interface Wrapper {
        default String wrapVal(Object val) {
            return String.valueOf(val);
        }
    }

    protected String buildSignContent(Map<String, String> sortedParams) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(sortedParams.keySet());
        Collections.sort(keys);
        int index = 0;
        for (String key : keys) {
            String value = sortedParams.get(key);
            if (areNotEmpty(key, value)) {
                content.append(index == 0 ? "" : "&")
                        .append(key)
                        .append("=")
                        .append(value);
                index++;
            }
        }
        return content.toString();
    }

    protected String buildVerifySignContent(Map<String, ?> params) {
        if (params == null) {
            return null;
        }
        params.remove("sign");
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<String>(params.keySet());
        Collections.sort(keys);

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = wrapVal(params.get(key));
            content.append(i == 0 ? "" : "&")
                    .append(key)
                    .append("=").append(value);
        }
        return content.toString();
    }

    /**
     * 检查指定的字符串列表是否不为空。
     */
    protected boolean areNotEmpty(String... values) {
        boolean result = true;
        if (values == null || values.length == 0) {
            result = false;
        } else {
            for (String value : values) {
                result &= !isEmpty(value);
            }
        }
        return result;
    }

    /**
     * 检查指定的字符串是否为空。
     *
     * @param value 待检查的字符串
     * @return true/false
     */
    protected boolean isEmpty(String value) {
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((!Character.isWhitespace(value.charAt(i)))) {
                return false;
            }
        }
        return true;
    }

}
