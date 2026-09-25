/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.http;

import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpMessageConverterExtractor;
import org.springframework.web.client.ResponseExtractor;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class NoExceptionRestTemplateResponseEntityExtractor<T> implements ResponseExtractor<ResponseEntity<T>> {

    private static final Logger log = LoggerFactory.getLogger(NoExceptionRestTemplateResponseEntityExtractor.class);
    private final HttpMessageConverterExtractor<T> delegate;

    NoExceptionRestTemplateResponseEntityExtractor(Type responseType, List<HttpMessageConverter<?>> converters) {
        if (responseType != null && Void.class != responseType) {
            this.delegate = new HttpMessageConverterExtractor<>(responseType, converters);
        } else {
            this.delegate = null;
        }
    }

    @Override
    public ResponseEntity<T> extractData(ClientHttpResponse response) throws IOException {
        if (this.delegate != null) {
            T body = null;
            try {
                body = this.delegate.extractData(response);
            } catch (RuntimeException e) {
                String content;
                try {
                    content = StreamUtils.copyToString(response.getBody(), Charset.forName("UTF-8"));
                    log.error("请求接口时返回[{}],无法解析返回值", sanitizeLog(content), e);
                } catch (Exception exception) {
                    content = "";
                    log.error("请求接口时返回未知结果,无法解析返回值", exception);
                }
                return new EraseTypeResponseEntity(content, sanitizeHeaders(response.getHeaders()), response.getStatusCode());
            }
            return new ResponseEntity<>(body, sanitizeHeaders(response.getHeaders()), response.getStatusCode());
        } else {
            return new ResponseEntity<>(sanitizeHeaders(response.getHeaders()), response.getStatusCode());
        }
    }

    /**
     * 清洗透传的响应头：剔除 CRLF 等控制字符，防止响应头注入（HTTP响应截断加固）
     */
    private HttpHeaders sanitizeHeaders(HttpHeaders headers) {
        HttpHeaders safe = new HttpHeaders();
        if (headers != null) {
            headers.forEach((name, values) -> {
                String safeName = name == null ? null : name.replace("\r", "").replace("\n", "");
                if (safeName == null || safeName.isEmpty()) {
                    return;
                }
                List<String> cleaned = new ArrayList<>(values.size());
                for (String v : values) {
                    cleaned.add(v == null ? null : v.replace("\r", "").replace("\n", ""));
                }
                safe.put(safeName, cleaned);
            });
        }
        return safe;
    }

    /**
     * 清洗日志内容中的换行符，防止日志伪造/注入
     */
    private static String sanitizeLog(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\r", " ").replace("\n", " ");
    }
}

