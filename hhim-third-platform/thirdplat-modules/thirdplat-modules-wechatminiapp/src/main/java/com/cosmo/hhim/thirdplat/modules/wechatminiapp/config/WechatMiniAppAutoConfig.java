/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatminiapp.config;

import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.Code2SessionInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.GainAppAccessTokenInterceptor;
import com.cosmo.hhim.thirdplat.modules.wechatminiapp.interceptor.WxAppAccessTokenInjectInterceptor;
import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.context.annotation.Bean;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/19
 */
@ForestScan(basePackages = "com.cosmo.hhim.thirdplat.modules.wechatminiapp.api")
public class WechatMiniAppAutoConfig {

    @Bean
    public GainAppAccessTokenInterceptor gainAppAccessTokenInterceptor() {
        return new GainAppAccessTokenInterceptor();
    }


    @Bean
    public Code2SessionInterceptor code2SessionInterceptor(){
        return new Code2SessionInterceptor();
    }

    @Bean
    public WxAppAccessTokenInjectInterceptor wxAppAccessTokenInjectInterceptor(){
        return new WxAppAccessTokenInjectInterceptor();
    }
}
