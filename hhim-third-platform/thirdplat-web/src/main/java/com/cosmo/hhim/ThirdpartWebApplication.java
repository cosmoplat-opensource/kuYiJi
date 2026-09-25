/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@MapperScan({"com.cosmo.hhim.thirdplat.web.mapper", "com.cosmo.hhim.external.mapper"})
@EnableConfigurationProperties
@EnableDiscoveryClient
public class ThirdpartWebApplication  {

    private static final Logger log = LoggerFactory.getLogger(ThirdpartWebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ThirdpartWebApplication.class, args);
        log.info("第三方对接平台启动成功");
    }
}
