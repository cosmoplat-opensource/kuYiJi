/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.factory; 

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.SpringUtils;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.AsyncParallelTaskStrategy;
import com.cosmo.hhim.micro.infrastructure.parallel.TaskRequestWrapper;
import com.cosmo.hhim.micro.infrastructure.parallel.TaskRespEntity;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步并行任务策略工厂
 * 使用较为简单的策略+工厂模式来管理每一个任务的注册与执行逻辑
 * {@link #STRATEGY_MAP} 存储每一个并行任务,key为具体的业务类型或业务标记,value为任务自身
 * {@link #register(String, AsyncParallelTaskStrategy)} 该方法为异步并行任务实现{@link InitializingBean#afterPropertiesSet()}后,注册到工厂map中
 * {@link #executeTask(String, TaskRequestWrapper)} 该方法为任务执行时根据不同任务策略执行不同业务
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class AsyncParallelStrategyFactory implements ApplicationListener<ApplicationEvent> {

    private static final Map<String, AsyncParallelTaskStrategy> STRATEGY_MAP = new ConcurrentHashMap<>(16);

    /**
     * 注册策略
     *
     * @param mark                  并行的任务策略
     * @param AsyncParallelBaseTask 并行的业务任务
     */
    public static void register(String mark, AsyncParallelTaskStrategy AsyncParallelBaseTask) { 
        STRATEGY_MAP.put(mark, AsyncParallelBaseTask);
    }

    public static <T, R> TaskRespEntity<R> executeTask(String mark, TaskRequestWrapper<T> req) {
        AsyncParallelTaskStrategy baseTask = STRATEGY_MAP.get(mark);
        if (baseTask != null) {
            return baseTask.execute(req);
        }
        return null;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        Map<String, AsyncParallelTaskStrategy> actual = SpringUtils.getBeansOfType(AsyncParallelTaskStrategy.class);
        actual.forEach((beanName, bean) -> {
            AsyncParallelTaskStrategy beanExist = STRATEGY_MAP.get(bean.mark());
            if (beanExist != null) {
                throw new CustomException(String.format("[%s] Duplicate execution strategy", bean.mark()));
            }
            STRATEGY_MAP.put(bean.mark(), bean);
        });
    }
}