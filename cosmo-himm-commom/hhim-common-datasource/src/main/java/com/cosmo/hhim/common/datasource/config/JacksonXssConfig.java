/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.datasource.config;

import com.fasterxml.jackson.core.JsonFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 JSON 序列化 XSS 输出编码配置
 * <p>
 * 对 Spring MVC 响应 JSON 应用 HTML 特殊字符转义（&lt; &gt; &amp; ' → \u003c 等）。
 * JSON 解析后内容不变，前端业务无感知，同时消除 XSS 输出点。
 */
@Configuration
public class JacksonXssConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer htmlEscapeJacksonCustomizer() {
        // Spring Boot 2.3 的 customizer 作用于 Jackson2ObjectMapperBuilder：
        // 通过注入配置了 HTML 转义的 JsonFactory，使构建出的 ObjectMapper 对 JSON 输出做转义
        return builder -> {
            JsonFactory factory = new JsonFactory();
            factory.setCharacterEscapes(new HtmlXssCharacterEscapes());
            builder.factory(factory);
        };
    }
}
