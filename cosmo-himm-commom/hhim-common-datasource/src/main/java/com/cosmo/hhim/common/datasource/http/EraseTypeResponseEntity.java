/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.http;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public class EraseTypeResponseEntity extends ResponseEntity {

    private String msg;

    EraseTypeResponseEntity(String msg, MultiValueMap<String, String> headers, HttpStatus status) {
        super(null, headers, status);
        // 透传内容清洗换行符，防止上游响应内容携带 CRLF 造成响应截断/日志注入
        this.msg = msg == null ? null : msg.replace("\r", " ").replace("\n", " ");
    }

    @Override
    public Object getBody() {
        return this.msg;
    }
}

