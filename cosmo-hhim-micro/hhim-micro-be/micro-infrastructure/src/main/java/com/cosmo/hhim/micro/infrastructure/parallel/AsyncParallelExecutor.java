/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.parallel;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.thread.ChildMdcThreadPoolTaskExcutor;
import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.AsyncParallelExitStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 异步并行执行器
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
public class AsyncParallelExecutor<T> {
    /**
     * 超时时间
     */
    private long timeout = 10 * 60 * 1000L;
    /**
     * 自定义线程池
     */
    private final ChildMdcThreadPoolTaskExcutor threadPool;
    /**
     * 退出策略
     */
    private final AsyncParallelExitStrategy<T> exitStrategy;
    private final CompletionService<T> completionService;

    public AsyncParallelExecutor() {
        this.threadPool = initThreadPool();
        this.exitStrategy = null;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    public AsyncParallelExecutor(AsyncParallelExitStrategy<T> exitStrategy) {
        this.threadPool = initThreadPool();
        this.exitStrategy = exitStrategy;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    public AsyncParallelExecutor(long timeoutMillis, ChildMdcThreadPoolTaskExcutor threadPool) {
        this.timeout = timeoutMillis;
        this.threadPool = threadPool;
        this.exitStrategy = null;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    public AsyncParallelExecutor(long timeoutMillis) {
        this.timeout = timeoutMillis;
        this.threadPool = initThreadPool();
        this.exitStrategy = null;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    public AsyncParallelExecutor(long timeoutMillis, AsyncParallelExitStrategy<T> exitStrategy) {
        this.timeout = timeoutMillis;
        this.threadPool = initThreadPool();
        this.exitStrategy = exitStrategy;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    private static ChildMdcThreadPoolTaskExcutor initThreadPool() {
        ChildMdcThreadPoolTaskExcutor taskExecutor = new ChildMdcThreadPoolTaskExcutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        taskExecutor.setAwaitTerminationMillis(500L);
        taskExecutor.setThreadNamePrefix("async_parallel_executor");
        taskExecutor.initialize();
        return taskExecutor;
    }

    public AsyncParallelExecutor(long timeoutMillis, ChildMdcThreadPoolTaskExcutor threadPool, AsyncParallelExitStrategy<T> exitStrategy) {
        this.timeout = timeoutMillis;
        this.threadPool = threadPool;
        this.exitStrategy = exitStrategy;
        this.completionService = new ExecutorCompletionService<>(this.threadPool);
    }

    /**
     * 获取任意一个数据
     *
     * @param tasks
     * @return
     */
    public synchronized T anyOfExecute(List<Callable<T>> tasks) {
        // 获取调用线程的MDC参数
        List<Future> futures = this.addFutureTask(tasks);
        long startTime = System.currentTimeMillis();
        T result = null;
        for (int i = 0; i < futures.size(); i++) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > this.timeout) {
                throw new CustomException("任务执行超时");
            }
            try {
                Future<T> future = completionService.take();
                if (future == null) {
                    throw new CustomException("无法获取任务信息");
                }
                if (exitStrategy.shouldExit(future)) {
                    futures.remove(future);
                    result = future.get();
                    futures.forEach(f -> f.cancel(true));
                    this.threadPool.shutdown();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        this.threadPool.shutdown();
        return result;
    }

    /**
     * 获取全部任务数据
     *
     * @param tasks
     * @return
     */
    public synchronized List<T> allOfExecute(List<Callable<T>> tasks) {
        // 获取调用线程的MDC参数
        List<Future> futures = this.addFutureTask(tasks);
        long startTime = System.currentTimeMillis();
        List<T> resultList = new ArrayList<>();
        T result;
        for (int i = 0; i < futures.size(); i++) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > this.timeout) {
                throw new CustomException("任务执行超时");
            }
            try {
                Future<T> future = completionService.take();
                if (future == null) {
                    throw new CustomException("无法获取任务信息");
                }
                if (exitStrategy.shouldExit(future)) {
                    futures.remove(future);
                    result = future.get();
                    if (result != null) {
                        resultList.add(result);
                    }
                    futures.forEach(f -> f.cancel(true));
                    this.threadPool.shutdown();
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
        this.threadPool.shutdown();
        return resultList;
    }

    /**
     * 添加future任务
     *
     * @param tasks
     * @return
     */
    private List<Future> addFutureTask(List<Callable<T>> tasks) {
        List<Future> futures = new ArrayList<>();
        for (Callable<T> task : tasks) {
            futures.add(completionService.submit(task));
        }
        return futures;
    }
}
