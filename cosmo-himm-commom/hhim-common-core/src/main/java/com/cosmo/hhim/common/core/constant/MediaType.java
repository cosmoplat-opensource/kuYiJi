/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

import org.springframework.util.StringUtils;

/**
 * @author cosmo-hhim-open Team
 * @description http请求媒体类型
 * @createTime 2022-11-07
 */
public final class MediaType {
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded;charset=UTF-8";

    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    public static final String APPLICATION_SVG_XML = "application/svg+xml";

    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    public static final String APPLICATION_XML = "application/xml;charset=UTF-8";

    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";

    public static final String MULTIPART_FORM_DATA = "multipart/form-data;charset=UTF-8";

    public static final String TEXT_HTML = "text/html;charset=UTF-8";

    public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";

    private MediaType(String type, String charset) {
        this.type = type;
        this.charset = charset;
    }

    // 媒体内容类型
    private final String type;

    // 媒体内容类型编码
    private final String charset;

    /**
     * 反向解析为MediaType类型
     * @param contentType
     * @return
     */
    public static MediaType valueOf(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw new IllegalArgumentException("媒体类型不允许为空！");
        }
        String[] values = contentType.split(";");
        String charset = Constants.UTF8;
        for (String value : values) {
            if (value.startsWith("charset=")) {
                charset = value.substring("charset=".length());
            }
        }
        return new MediaType(values[0], charset);
    }

    /**
     * 根据给出的媒体内容类型和媒体内容编码封装为MediaType类型
     * @param contentType
     * @param charset
     * @return
     */
    public static MediaType valueOf(String contentType, String charset) {
        if (StringUtils.isEmpty(contentType)) {
            throw new IllegalArgumentException("媒体类型不允许为空！");
        }
        String[] values = contentType.split(";");
        return new MediaType(values[0], StringUtils.isEmpty(charset) ? Constants.UTF8 : charset);
    }

    public String getType() {
        return type;
    }

    public String getCharset() {
        return charset;
    }

    @Override
    public String toString() {
        return type + ";charset=" + charset;
    }
}
