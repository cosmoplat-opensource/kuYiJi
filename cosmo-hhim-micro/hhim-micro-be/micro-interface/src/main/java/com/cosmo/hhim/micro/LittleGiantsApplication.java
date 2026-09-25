/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro;

import com.cosmo.hhim.common.log.annotation.EnableLogAspect;
import com.cosmo.hhim.common.security.annotation.EnableAccessAuth;
import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.EnableContentTip;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author cosmo-hhim-open Team
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cosmo.hhim.*.api"})
@EnableTransactionManagement
@MapperScan("com.cosmo.hhim.**.mapper")
@EnableAccessAuth(basePackages = {"com.cosmo.hhim.micro.controller"})
@SpringBootApplication(exclude = {MultipartAutoConfiguration.class})
@EnableLogAspect
@EnableAsync
@EnableContentTip
@EnableAspectJAutoProxy(exposeProxy = true)
@Slf4j
public class LittleGiantsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LittleGiantsApplication.class, args);
        log.info("微应用服务启动成功");
    }
}