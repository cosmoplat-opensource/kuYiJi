/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.config;

import com.cosmo.hhim.common.security.accessauth.cache.ControllerMethodsCache;
import com.cosmo.hhim.common.security.accessauth.filter.AccessAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 访问权限自动配置类
 * @createTime 2022-11-07
 */
public class AccessAuthConfig extends AbstractAccessAuthConfiguration {

    private static final String BASE_PACKAGES_ATTRIBUTE_NAME = "basePackages";

    @Bean
    public ControllerMethodsCache controllerMethodsCache() {
        ControllerMethodsCache controllerMethodsCache = new ControllerMethodsCache();

        List<String> basePackages = Arrays.asList(this.annotationAttributes.getStringArray(BASE_PACKAGES_ATTRIBUTE_NAME));
        if (!CollectionUtils.isEmpty(basePackages)) {
            basePackages.forEach(controllerMethodsCache::initClassMethod);
        }
        return controllerMethodsCache;
    }

    @Bean
    public AccessAuthFilter accessAuthFilter(){
        return new AccessAuthFilter();
    }

//    @Bean
//    public FilterRegistrationBean accessAuthFilterRegistration(){
//        FilterRegistrationBean<AccessAuthFilter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(accessAuthFilter());
//        registration.addUrlPatterns("/*");
//        registration.setName("accessAuthFilter");
//        registration.setOrder(1);
//        return registration;
//    }


}
