/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.strategy; 

import com.cosmo.hhim.micro.infrastructure.parallel.TaskRequestWrapper;
import com.cosmo.hhim.micro.infrastructure.parallel.TaskRespEntity;

/**
 * 异步并行任务接口类,每一个需要异步并行的任务都需要实现该类
 * 本类{@link T,R}分别为入参和出参的泛型类,实现类需指定业务具体的出入参类
 * 本类execute的实现方法为业务真正执行的逻辑
 *
 * @author cosmo-hhim-open Team
 * {@link com.cosmo.hhim.micro.infrastructure.designPattern.factory.AsyncParallelStrategyFactory#register(String, AsyncParallelTaskStrategy)}
 */
public interface AsyncParallelTaskStrategy<T, R> {
    /**
     * 任务执行逻辑接口
     *
     * @param req 业务入参
     * @return TaskRespEntity<RESPONSE> 业务出参
     */
    TaskRespEntity<R> execute(TaskRequestWrapper<T> req);

    /**
     * 执行策略标识
     *
     * @return 返回标识
     */
    String mark();


}