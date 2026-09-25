/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.http;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.net.URI;

public class RestTemplateProxy extends RestTemplate {

    private static final Logger log = LoggerFactory.getLogger(RestTemplateProxy.class);

    @Override
    public <T> ResponseExtractor<ResponseEntity<T>> responseEntityExtractor(Type responseType) {
        return new NoExceptionRestTemplateResponseEntityExtractor<>(responseType, getMessageConverters());
    }

    @Override
    protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
        // 请求日志做安全编码（OWASP Encode.forJava 转义换行、引号等），防止 URL 中的 CRLF 被写入日志（日志伪造/注入加固）；
        // 响应体内容不记录日志（内部 HTTP 调用返回体可能包含业务数据，无日志价值）
        log.info("发起请求 {} {}", method, Encode.forJava(url.toString()));
        return super.doExecute(url, method, requestCallback, responseExtractor);
    }
}
