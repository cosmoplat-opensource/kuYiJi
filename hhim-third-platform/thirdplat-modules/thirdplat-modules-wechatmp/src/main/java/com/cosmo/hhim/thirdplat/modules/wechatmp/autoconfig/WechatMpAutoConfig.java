/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.autoconfig;

import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.AccessTokenUrlInjectInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.GainTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.GainWebTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.interceptor.RefreshWebTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.WxAutoScanProxy;
import com.cosmo.hhim.thirdplat.modules.wechatmp.proxy.interceptor.WxResponseExceptionInterceptor;
import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@ForestScan(basePackages = "com.cosmo.hhim.thirdplat.modules.wechatmp.api")
public class WechatMpAutoConfig {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    public AccessTokenUrlInjectInterceptor accessTokenUrlInjectInterceptor() {
        AccessTokenUrlInjectInterceptor interceptor = new AccessTokenUrlInjectInterceptor();
        interceptor.setRedisTemplate(redisTemplate);
        return interceptor;
    }

    @Bean
    public GainTokenInterceptor gainTokenInterceptor() {
        GainTokenInterceptor interceptor = new GainTokenInterceptor();
        interceptor.setRedisTemplate(redisTemplate);
        return interceptor;
    }

    @Bean
    public GainWebTokenInterceptor gainWebTokenInterceptor() {
        return new GainWebTokenInterceptor();
    }

    @Bean
    public RefreshWebTokenInterceptor refreshWebTokenInterceptor() {
        return new RefreshWebTokenInterceptor();
    }

    @Bean
    public WxResponseExceptionInterceptor wxResponseExceptionInterceptor(){
        return new WxResponseExceptionInterceptor();
    }

    @Bean
    public WxAutoScanProxy wxAutoScanProxy(){
        return new WxAutoScanProxy();
    }

}
