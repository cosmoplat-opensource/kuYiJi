/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.config;

import com.cosmo.hhim.micro.filter.CertificateInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/14
 */
@RefreshScope
@Configuration
public class CertificateWebInterceptorConfig extends WebMvcConfigurerAdapter {

    @Value("#{'${certificate.includedPaths}'.split(',')}")
    private List<String> includedPaths;

    @Autowired
    private CertificateInterceptor certificateInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // order目的：保证common包中的WebFilter先执行
        registry.addInterceptor(certificateInterceptor).addPathPatterns(includedPaths).order(100);
    }
}
