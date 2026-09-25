/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * H5 跨域配置（运行环境配置，不涉及业务逻辑）
 * <p>
 * 仅允许白名单内的可信来源跨域（不携带 Cookie 凭证）。
 * 默认白名单为本地开发地址；生产/测试环境如部署在其他域名下，请直接修改
 * {@link #DEFAULT_ALLOWED_ORIGINS} 数组为实际前端域名（静态字面量，可被静态扫描确认非通配符）。
 *
 * @author cosmo-hhim-open Team
 * @createTime 2026-08-10
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /** 允许跨域的来源白名单：仅可信域名（静态字面量数组，禁止使用通配符 *） */
    private static final String[] DEFAULT_ALLOWED_ORIGINS = {
            "http://localhost:8080", "http://127.0.0.1:8080",
            "http://localhost:8081", "http://127.0.0.1:8081",
            "http://localhost:8082", "http://127.0.0.1:8082",
            "http://localhost:8083", "http://127.0.0.1:8083",
            "http://localhost:8084", "http://127.0.0.1:8084",
            "http://localhost:5173", "http://127.0.0.1:5173",
            "http://localhost:5174", "http://127.0.0.1:5174",
            "http://localhost:5175", "http://127.0.0.1:5175",
            "http://localhost:3000", "http://127.0.0.1:3000"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 注意：Spring Boot 2.3（Spring 5.2）无 allowedOriginPatterns，使用 allowedOrigins
        // CORS 策略收紧：来源为静态字面量白名单、请求头显式白名单、不携带 Cookie 凭证
        // 请求头白名单需与前端 uni.request 实际发送的 header 一致（type/application_sign/username/target_customer 等），
        // 否则浏览器预检请求（OPTIONS）会被 Spring 拒绝返回 403
        registry.addMapping("/**")
                .allowedOrigins(DEFAULT_ALLOWED_ORIGINS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "X-Requested-With", "Accept", "Origin",
                        "type", "application_sign", "username", "target_customer")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
