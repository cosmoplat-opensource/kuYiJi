/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

/**
 * JSON 输出 HTML 特殊字符转义（XSS 输出编码防护）
 * <p>
 * 将 &lt; &gt; &amp; ' 等 HTML 敏感字符转义为 \u003c 等 Unicode 转义序列输出。
 * JSON 解析后内容与原文完全一致（不影响前端业务），但可防止浏览器将 JSON 内容
 * 误解析为 HTML 标签执行脚本。
 */
public class HtmlXssCharacterEscapes extends CharacterEscapes {

    private static final long serialVersionUID = 1L;

    private final int[] asciiEscapes;

    public HtmlXssCharacterEscapes() {
        // 复用 Jackson 默认 ASCII 转义表，再补充 HTML 敏感字符
        asciiEscapes = standardAsciiEscapesForJSON();
        asciiEscapes['<'] = ESCAPE_CUSTOM;
        asciiEscapes['>'] = ESCAPE_CUSTOM;
        asciiEscapes['&'] = ESCAPE_CUSTOM;
        asciiEscapes['\''] = ESCAPE_CUSTOM;
    }

    @Override
    public int[] getEscapeCodesForAscii() {
        return asciiEscapes;
    }

    @Override
    public SerializableString getEscapeSequence(int ch) {
        return new SerializedString(String.format("\\u%04x", ch));
    }
}
