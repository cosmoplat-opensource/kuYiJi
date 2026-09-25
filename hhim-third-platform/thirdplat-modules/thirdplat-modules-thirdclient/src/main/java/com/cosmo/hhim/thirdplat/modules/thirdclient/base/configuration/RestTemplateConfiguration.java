/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.configuration;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfiguration {
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create()
                .setMaxConnTotal(20)
                .setMaxConnPerRoute(20)
                .build());
        httpRequestFactory.setConnectionRequestTimeout(10000);
        httpRequestFactory.setConnectTimeout(20000);
        httpRequestFactory.setReadTimeout(20000);
        return new RestTemplate(httpRequestFactory);
    }
}