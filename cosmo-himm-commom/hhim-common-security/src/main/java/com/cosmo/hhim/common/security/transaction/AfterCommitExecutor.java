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

/**
 * 事务完成之后再执行操作
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 *  * 已过时，不建议使用
 */
@Component
@Deprecated
public class AfterCommitExecutor extends TransactionSynchronizationAdapter implements Executor {

    private static final Logger log = LoggerFactory.getLogger(AfterCommitExecutor.class);
    private static final ThreadLocal<List<Runnable>> RUNNABLES = new ThreadLocal<List<Runnable>>();
    private ExecutorService threadPool = Executors.newFixedThreadPool(5);

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
        List<Runnable> threadRunnables = RUNNABLES.get();
        log.info("事务提交之后执行runnables{}", threadRunnables.size());
        Map<String, String> context = MDC.getCopyOfContextMap();
        for (int i = 0; i < threadRunnables.size(); i++) {
            Runnable runnable = threadRunnables.get(i);
            log.info("执行runnable{}", runnable);
            try {
                threadPool.execute(()->{
                    try {
                        //先睡一会
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }catch (Exception e) {
                        log.error("err. ",e);
                    }
                    MDC.setContextMap(context);
                    runnable.run();
                } );
            } catch (RuntimeException e) {
                log.error("执行失败{}", runnable, e);
            }
        }
    }

    @Override
    public void afterCompletion(int status) {
        log.info("事务结束 {}", status == STATUS_COMMITTED ? "COMMITTED" : "ROLLED_BACK");
        RUNNABLES.remove();
    }

}
