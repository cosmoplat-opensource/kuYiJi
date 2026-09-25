/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
//package com.cosmo.hhim.common.log.config;
//
//
//import com.cosmo.hhim.common.log.interceptor.ReplaceStreamFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.servlet.Filter;
//
///**
// * @author cosmo-hhim-open Team
// * @program wrapper-demo
// * @description 过滤器配置类
// * @create 2018-12-24 21:06
// * @since 1.0
// **/
//@Configuration
//public class FilterConfig {
//    /**
//     * 注册过滤器
//     *
//     * @return FilterRegistrationBean
//     */
//    @Bean
//    public FilterRegistrationBean someFilterRegistration() {
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(replaceStreamFilter());
//        registration.addUrlPatterns("/*");
//        registration.setName("streamFilter");
//        return registration;
//    }
//
//    /**
//     * 实例化StreamFilter
//     *
//     * @return Filter
//     */
//    @Bean(name = "replaceStreamFilter")
//    public Filter replaceStreamFilter() {
//        return new ReplaceStreamFilter();
//    }
//
//
//}
