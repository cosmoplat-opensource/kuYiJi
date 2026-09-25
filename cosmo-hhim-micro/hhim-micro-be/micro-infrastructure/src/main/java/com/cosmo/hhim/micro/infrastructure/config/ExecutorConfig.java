/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.config;

import com.cosmo.hhim.common.core.thread.ChildMdcThreadPoolTaskExcutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class ExecutorConfig { 

    /**
 * @author cosmo-hhim-open Team
     * 记录日志线程池
     *
     * @return
     */
    @Bean("redisOperateExecutor")
    public Executor redisOperateExecutor() {
        ThreadPoolTaskExecutor executor = getExecutor("redisOperateExecutor", 20, 30, 20, 5);
        return executor;
    }

    /**
     * 记录日志线程池
     *
     * @return
     */
    @Bean("asyncEventExecutor")
    public Executor asyncEventExecutor() {
        ThreadPoolTaskExecutor executor = getExecutor("asyncEventExecutor", 5, 10, 10, 2);
        return executor;
    }

    private ThreadPoolTaskExecutor getExecutor(String theadName, int corePoolSize, int maxPoolSize, int queue, int second) {
        ThreadPoolTaskExecutor executor = new ChildMdcThreadPoolTaskExcutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queue);
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(second);
        executor.setThreadNamePrefix(theadName + "-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }
}