/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.parallel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Future;

public class MDCCompletionService<V> extends ExecutorCompletionService<V> { 

    private final ThreadLocal<Map<String, String>> mdcContextMap = new InheritableThreadLocal<>();

    public MDCCompletionService(Executor executor) {
        super(executor);
    }

    @Override
    public Future<V> submit(Callable<V> task) {
        return super.submit(() -> {
            Map<String, String> parentMdcContext = mdcContextMap.get();
            if (parentMdcContext != null) {
                // 将调用线程的MDC参数设置到子线程中
                mdcContextMap.set(new HashMap<>(parentMdcContext));
            }
            try {
                return task.call();
            } finally {
                // 清除子线程中的MDC参数
                mdcContextMap.remove();
            }
        });
    }

    public void setMdcContext(Map<String, String> mdcContext) {
        mdcContextMap.set(mdcContext);
    }
}
