/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.cosmo.hhim.common.event.EventManager;
import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.listener.CommonListener;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @author cosmo-hhim-open Team
 * @description 默认的事件分派器
 * @createTime 2022/3/18
 */
@Slf4j
public class DefaultDispatcher extends Thread implements Dispatcher {

    // 已初始化标识
    private volatile boolean initialized = false;
    // 已关闭标识
    private volatile boolean shutdown = false;

    // 事件队列最大容量
    private int queueMaxSize = -1;

    // 事件队列
    private BlockingQueue<Event> queue;

    // 承载事件的监听器
    protected final ConcurrentHashSet<CommonListener> commonListeners = new ConcurrentHashSet<>();

    // 事件类型
    private Class<? extends Event> eventType;

    // 用于记录当前事件分派器最后所处理事件的序列号（目的：用于判断所处理的事件是否过期）
    protected volatile Long lastEventSequence = -1L;

    // 原子操作volatile所声明的属性lastEventSequence的值
    private static final AtomicReferenceFieldUpdater<DefaultDispatcher, Long> UPDATER = AtomicReferenceFieldUpdater
            .newUpdater(DefaultDispatcher.class, Long.class, "lastEventSequence");


    /**
     * 初始化
     * @param type
     * @param bufferSize
     */
    @Override
    public void init(Class<? extends Event> type, int bufferSize) {
        setDaemon(true);
        setName("common.dispatcher-" + type.getName());
        this.eventType = type;
        this.queueMaxSize = bufferSize;
        queue = new ArrayBlockingQueue<>(bufferSize);

        // 开启事件分派线程
        start();
    }

    @Override
    public void shutdown() {
        this.shutdown = true;
        this.queue.clear();
    }

    /**
     * 待发布的事件数量
     * @return
     */
    @Override
    public long currentEventSize() {
        return queue.size();
    }

    /**
     * 添加监听器
     * @param commonListener
     */
    @Override
    public void addListener(CommonListener commonListener) {
        commonListeners.add(commonListener);
    }

    /**
     * 移除监听器
     * @param commonListener
     */
    @Override
    public void removeListener(CommonListener commonListener) {
        commonListeners.remove(commonListener);
    }

    /**
     * 获取事件分派器初始化状态
     * @return
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 发布事件
     * @param event
     * @return
     */
    @Override
    public boolean publish(Event event) {
        // 校验当前发布者是否已启动
        if (!initialized) {
            throw new IllegalStateException("默认事件发布者未启动！");
        }

        // 添加事件到待发布事件队列
        boolean success = this.queue.offer(event);
        if (!success) {
            log.warn("发布者持有的待发布事件队列已满，故直接发布事件处理! event:{}", event);

            // 分派事件给该事件的订阅者进行处理
            dispatcherEvent(event);
            return true;
        }

        return true;
    }

    /**
     * 通知指定事件的监听器
     * @param commonListener
     * @param event
     */
    @Override
    public void notifyListener(CommonListener commonListener, Event event) {
        // 定义一个线程处理订阅者事件（目的：方便兼容异步还是同步处理）
        final Runnable job = () -> commonListener.onEvent(event);
        final Executor executor = commonListener.executor(); // 获取监听器的异步处理线程池

        if (executor != null) { // 监听器配置了异步线程池，则异步处理
            executor.execute(job);
        } else { // 监听器未配置异步线程池，则直接调用线程run()同步处理
            try {
                job.run();
            } catch (Exception e) {
                log.error("Event callback exception: ", e);
            }
        }
    }

    @Override
    public synchronized void start() {
        if (!initialized) { //只允许一次
            super.start();
            if (queueMaxSize == -1) {
                queueMaxSize = EventManager.EVENT_QUEUE_MAX_SIZE;
            }
            initialized = true;
        }
    }

    @Override
    public void run() {
        try {
            int waitTimes = 60;
            for (; ; ) {
                if (shutdown || waitTimes <= 0 || !CollectionUtils.isEmpty(commonListeners)) {
                    break;
                }
                TimeUnit.SECONDS.sleep(1);
                waitTimes--;
            }

            for (; ; ) {
                if (shutdown) {
                    break;
                }
                // 提取待发布的事件
                final Event event = queue.take();
                // 分派事件给订阅者处理
                dispatcherEvent(event);

                // 原子操作更新volatile声明的事件序列号 => 更新为当前正在处理的事件的序列号
                UPDATER.compareAndSet(this, lastEventSequence, Math.max(lastEventSequence, event.sequence()));
            }

        } catch (Exception e) {
            log.error("事件发布者处理发布事件发生异常:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 分派事件给到该事件的监听器
     * @param event
     */
    private void dispatcherEvent(Event event) {
        final long currentEventSequence = event.sequence();

        if(CollectionUtils.isEmpty(commonListeners)){
            log.warn("事件:{} 处理失败！无法找到订阅者!", event);
            return;
        }

        for (CommonListener commonListener : commonListeners) {
            if (commonListener.ignoreExpireEvent() && lastEventSequence > currentEventSequence) {
                log.debug("事件:{} 已经过期，无法被订阅者处理！", event);
                continue;
            }

            // 通知订阅者处理
            notifyListener(commonListener, event);
        }

    }
}
