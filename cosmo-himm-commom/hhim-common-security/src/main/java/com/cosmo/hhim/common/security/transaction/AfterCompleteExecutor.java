/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.cosmo.hhim.common.core.constant.CommitStatus.COMMITTED;
import static com.cosmo.hhim.common.core.constant.CommitStatus.UNLIMITED;

/**
 * 事务完成之后再执行操作
 */
@Component
public class AfterCompleteExecutor extends TransactionSynchronizationAdapter implements Executor {
    private static final Logger log = LoggerFactory.getLogger(AfterCompleteExecutor.class);
    private static final ThreadLocal<List<Runnable>> RUNNABLES = new ThreadLocal<List<Runnable>>();
    private static final ThreadLocal<Integer> STATUS = new ThreadLocal<>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

    public int getStatus() {
        return STATUS.get();
    }
    public void setStatus(int status) {
        STATUS.set(status);
    }
    public AfterCompleteExecutor(int status) {
        this.setStatus(status);
    }
    public AfterCompleteExecutor() {
        this.setStatus(UNLIMITED);
    }

    @Override
    public void execute(Runnable runnable) {
        log.info("commit之后创建新的的runnable{}", runnable);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.info("Transaction synchronization is NOT ACTIVE. Executing right now runnable {}", runnable);
            runnable.run();
            return;
        }
        List<Runnable> threadRunnables = RUNNABLES.get();
        if (threadRunnables == null) {
            threadRunnables = new ArrayList<>();
            RUNNABLES.set(threadRunnables);
            TransactionSynchronizationManager.registerSynchronization(this);
        }
        threadRunnables.add(runnable);
    }

    @Override
    public void afterCommit() {
        //仅事务提交后执行的，数据库还没执行
    }

    @Override
    public void afterCompletion(int status) {
        //事务完成后执行的，这时候数据库已经是最新的数据
        log.info("事务结束 {}", status == COMMITTED ? "COMMITTED" : "ROLLED_BACK");
        List<Runnable> threadRunnables = RUNNABLES.get();
        log.info("事务提交之后执行runnables{}", threadRunnables.size());
        if (status == this.getStatus() || this.getStatus() == UNLIMITED){
            Map<String, String> context = MDC.getCopyOfContextMap();
            for (int i = 0; i < threadRunnables.size(); i++) {
                Runnable runnable = threadRunnables.get(i);
                log.info("执行runnable{}", runnable);
                try {
                    threadPool.execute(()->{
                        MDC.setContextMap(context);
                        runnable.run();
                    } );
                } catch (RuntimeException e) {
                    log.error("执行失败{}", runnable, e);
                }
            }
        }
        RUNNABLES.remove();
        STATUS.remove();
    }
}
