/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.parallel;

import com.cosmo.hhim.micro.infrastructure.designPattern.factory.AsyncParallelStrategyFactory;

import java.util.concurrent.Callable;

/**
 * 异步并行任务类
 * 使用此类构建要异步并行的任务,该类继承了Callable接口,在异步调用时会触发{@link #call()}方法
 * <p>
 * 在{@link #call()}方法中使用策略模式来选择具体的业务实现逻辑
 * <p>
 * mark属性:每个异步并行任务的类型标记 属性值应与AsyncParallelBaseTask的实现类保持一致
 * reqWrapper:每个异步并行任务所需的参数包装类 使用构造器创建包装参数 new TaskRequestWrapper<DemoBizEntity>
 * <p>
 * 使用方法: new AsyncParallelTask(mark,reqWrapper)
 *
 * @author cosmo-hhim-open Team
 */
public class AsyncParallelTask<T, R> implements Callable<TaskRespEntity<R>> {

    private String mark;
    private TaskRequestWrapper<T> reqWrapper;

    public AsyncParallelTask(String mark, TaskRequestWrapper<T> reqWrapper) {
        this.mark = mark;
        this.reqWrapper = reqWrapper;
    }

    @Override
    public TaskRespEntity<R> call() throws Exception {
        return AsyncParallelStrategyFactory.executeTask(mark, reqWrapper);
    }
}