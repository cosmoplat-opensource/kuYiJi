/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.config;

import com.cosmo.hhim.common.core.converter.IntegerCodeToEnumConverterFactory;
import com.cosmo.hhim.common.core.converter.StringCodeToEnumConverterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/9
 */
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 枚举类的转换器工厂 addConverterFactory
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverterFactory(new IntegerCodeToEnumConverterFactory());
        registry.addConverterFactory(new StringCodeToEnumConverterFactory());
    }
}