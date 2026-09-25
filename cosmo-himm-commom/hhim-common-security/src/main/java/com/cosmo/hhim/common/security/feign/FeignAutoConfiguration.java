/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.feign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Primary;


/**
 * Feign 配置注册
 *
 * @author cosmo-hhim-open Team
 **/
@Configuration
public class FeignAutoConfiguration {
    @Bean
    @Primary
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor() ;
    }

    @Bean
    @Primary
    public FeignResponseInterceptor responseInterceptor() {
        return new FeignResponseInterceptor();
    }
}
