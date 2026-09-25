/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.log.config;


import com.cosmo.hhim.common.log.aspect.LogAspect;
import com.cosmo.hhim.common.log.service.AsyncLogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.AsyncConfigurer;

/**
 * @author cosmo-hhim-open Team
 * @description 异步日志自动配置
 * @createTime 2022-11-22
 */
public class AsyncLogAutoConfig {

    @Bean
    public LogAspect getLogAspectBean() {
        LogAspect logAspect = new LogAspect();
        return logAspect;
    }

    @Bean
    public AsyncLogService getAsyncLogServiceBean() {
        AsyncLogService asyncLogService = new AsyncLogService();
        return asyncLogService;
    }

    @Bean
    @ConditionalOnMissingBean(AsyncConfigurer.class)
    public AsyncConfig getAsyncConfig(){
        return new AsyncConfig();
    }


}
