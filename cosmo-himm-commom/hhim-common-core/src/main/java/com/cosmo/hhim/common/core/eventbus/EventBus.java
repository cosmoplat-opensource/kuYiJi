/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.eventbus;

import com.google.common.util.concurrent.MoreExecutors;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author cosmo-hhim-open Team
 * @description 同步事件执行总线
 * @createTime 2021-06-01
 */
public class EventBus {

    private Executor executor;
    private ObserverRegistry registry = new ObserverRegistry(); //事件订阅管理者

    public EventBus() {
        //使用guava的当前线程的线程池实现
        this(MoreExecutors.directExecutor());
    }

    public EventBus(Executor executor) {
        this.executor = executor;
    }

    /**
     * 注册订阅者
     *
     * @param object
     */
    public void register(Object object) {
        registry.register(object);
    }

    /**
     * 推送事件
     *
     * @param event
     */
    public void post(Object event) {
        //获取对应事件的订阅者集合
        List<ObserverAction> observerActions = registry.getMatchedObserverActions(event);
        //执行订阅方法（反射调用）
        for (ObserverAction observerAction : observerActions) {
            executor.execute(() -> observerAction.execute(event));
        }
    }
}
