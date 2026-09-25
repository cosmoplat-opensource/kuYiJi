/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.config;

import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.AcquireTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.DelTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.TokenHeaderInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.UniPushAutoScanProxy;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.interceptor.UniPushResponseExceptionInterceptor;
import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.context.annotation.Bean;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@ForestScan(basePackages = "com.cosmo.hhim.thirdplat.modules.unipush.api")
public class UniPushAutoConfig {

    @Bean
    public AcquireTokenInterceptor acquireTokenInterceptor() {
        return new AcquireTokenInterceptor();
    }

    @Bean
    public DelTokenInterceptor delTokenInterceptor() {
        return new DelTokenInterceptor();
    }

    @Bean
    public TokenHeaderInterceptor tokenHeaderInterceptor() {
        return new TokenHeaderInterceptor();
    }

    @Bean
    public UniPushResponseExceptionInterceptor responseExceptionInterceptor() {
        return new UniPushResponseExceptionInterceptor();
    }

    @Bean
    public UniPushResponseExceptionInterceptor uniPushResponseExceptionInterceptor(){
        return new UniPushResponseExceptionInterceptor();
    }

    @Bean
    public UniPushAutoScanProxy uniPushAutoScanProxy(){
        return new UniPushAutoScanProxy();
    }
}
