/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.log.config;

import com.cosmo.hhim.common.core.thread.ChildMdcThreadPoolTaskExcutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.util.concurrent.Executor;

/**
 * @author cosmo-hhim-open Team
 * @description @Async自定义线程池配置
 * @createTime 2022-11-22
 */
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ChildMdcThreadPoolTaskExcutor childMdcThreadPoolTaskExcutor = new ChildMdcThreadPoolTaskExcutor();
        childMdcThreadPoolTaskExcutor.setCorePoolSize(5);
        childMdcThreadPoolTaskExcutor.setMaxPoolSize(20);
        childMdcThreadPoolTaskExcutor.setQueueCapacity(50);
        childMdcThreadPoolTaskExcutor.initialize();
        return childMdcThreadPoolTaskExcutor;
    }
}
