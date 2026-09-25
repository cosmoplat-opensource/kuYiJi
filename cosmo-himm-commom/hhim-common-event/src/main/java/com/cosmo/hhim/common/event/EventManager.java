/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event;

import com.cosmo.hhim.common.event.dispatcher.DefaultDispatcher;
import com.cosmo.hhim.common.event.dispatcher.DefaultShareDispatcher;
import com.cosmo.hhim.common.event.dispatcher.Dispatcher;
import com.cosmo.hhim.common.event.dispatcher.DispatcherFactory;
import com.cosmo.hhim.common.event.dispatcher.ShardedEventDispatcher;
import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.event.ShareEvent;
import com.cosmo.hhim.common.event.listener.CommonListener;
import com.cosmo.hhim.common.event.listener.CommonMultiListener;
import com.cosmo.hhim.common.event.utils.MapUtil;
import com.cosmo.hhim.common.event.dispatcher.DispatcherServiceLoader;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author cosmo-hhim-open Team
 * @description 事件管理器
 * @createTime 2022/3/18
 */
@Slf4j
public class EventManager {

    public static final int EVENT_QUEUE_MAX_SIZE = 16384;

    public static final int SHARE_PUBLISHER_QUEUE_SIZE = 1024;

    private static final EventManager INSTANCE = new EventManager();

    // 默认事件分派器工厂类
    private static final DispatcherFactory DEFAULT_DISPATCHER_FACTORY;

    // 默认的共享队列事件分派器
    private DefaultShareDispatcher shareDispatcher;

    private static Class<? extends Dispatcher> clazz;

    // 发布订阅管理器已关闭标识
    private static final AtomicBoolean CLOSED = new AtomicBoolean(false);

    // 维护事件与事件分派器的映射
    private final Map<String, Dispatcher> dispatcherMap = new ConcurrentHashMap<>(16);

    static {
        // 通过JDK SPI机制加载事件分派器接口的外部扩展实现类
        final Collection<Dispatcher> dispatchers = DispatcherServiceLoader.load(Dispatcher.class);
        Iterator<Dispatcher> iterator = dispatchers.iterator();
        if (iterator.hasNext()) { // 采用自定义的事件分派器
            clazz = iterator.next().getClass();
        } else { // 采用默认的事件分派器
            clazz = DefaultDispatcher.class;
        }

        // 初始化事件分派器
        DEFAULT_DISPATCHER_FACTORY = (subscribeType, bufferSize) -> {
            try {
                Dispatcher dispatcher = clazz.newInstance();
                dispatcher.init(subscribeType, bufferSize);
                return dispatcher;
            } catch (Exception e) {
                log.error("事件分派器实例化发生异常:{}", Throwables.getStackTraceAsString(e));
                throw new RuntimeException("事件分派器实例化发生异常！", e);
            }
        };

        // 初始化共享队列事件分派器
        try {
            INSTANCE.shareDispatcher = new DefaultShareDispatcher();
            INSTANCE.shareDispatcher.init(ShareEvent.class, SHARE_PUBLISHER_QUEUE_SIZE);
        } catch (Exception e) {
            log.error("实例化共享队列事件分派器发生异常:{}", Throwables.getStackTraceAsString(e));
        }

        // 线程销毁回调
        Runtime.getRuntime().addShutdownHook(new Thread(EventManager::shutdown));
    }

    /**
     * 关闭发布订阅管理器维护的所有事件分派器实例
     */
    public static void shutdown() {
        // CAS操作保证多线程并发时只处理一次
        if (!CLOSED.compareAndSet(false, true)) {
            return;
        }

        // 关闭所有事件分派器
        for (Map.Entry<String, Dispatcher> entry : INSTANCE.dispatcherMap.entrySet()) {
            try {
                Dispatcher dispatcher = entry.getValue();
                dispatcher.shutdown();
            } catch (Exception e) {
                log.error("事件分派器shutdown异常:{}", Throwables.getStackTraceAsString(e));
            }
        }

        // 关闭共享事件分派器
        try {
            INSTANCE.shareDispatcher.shutdown();
        } catch (Exception e) {
            log.error("共享事件分派器shutdown发生异常:{}", Throwables.getStackTraceAsString(e));
        }
    }

    //-----------------------------------------------------------------订阅者管理------------------------------------------------------------------------------------------

    // 注册监听器（采用默认的事件分派器工厂构造）
    public static void registerListener(final CommonListener commonListener) {
        registerListener(commonListener, DEFAULT_DISPATCHER_FACTORY);
    }

    public static void registerListener(final CommonListener commonListener, final DispatcherFactory factory) {

        // 1.多事件监听器处理
        if (commonListener instanceof CommonMultiListener) {
            for (Class<? extends Event> subscribeType : ((CommonMultiListener) commonListener).subscribeTypes()) {
                // 共享队列事件
                if (ShareEvent.class.isAssignableFrom(subscribeType)) {
                    INSTANCE.shareDispatcher.addListener(commonListener, subscribeType);
                } else { // 普通事件
                    addListener(commonListener, subscribeType, factory);
                }
            }
            return;
        }

        // 2.单事件监听器处理
        final Class<? extends Event> subscribeType = commonListener.subscribeType();
        // 共享队列事件
        if (ShareEvent.class.isAssignableFrom(subscribeType)) {
            INSTANCE.shareDispatcher.addListener(commonListener, subscribeType);
            return;
        }
        // 普通事件
        addListener(commonListener, subscribeType, factory);
    }

    private static void addListener(final CommonListener commonListener, Class<? extends Event> subscribeType, DispatcherFactory factory) {
        final String topic = subscribeType.getCanonicalName();
        synchronized (EventManager.class) {
            // 维护事件与该事件分派器映射关系
            MapUtil.computeIfAbsent(INSTANCE.dispatcherMap, topic, factory, subscribeType, EVENT_QUEUE_MAX_SIZE);
        }

        // 将监听器添加到该事件类型对应的发布者所维护的订阅者队列中
        Dispatcher dispatcher = INSTANCE.dispatcherMap.get(topic);
        if (dispatcher instanceof ShardedEventDispatcher) {
            ((ShardedEventDispatcher) dispatcher).addListener(commonListener, subscribeType);
        } else {
            dispatcher.addListener(commonListener);
        }
    }

    /**
     * 移除监听器
     *
     * @param commonListener
     */
    public static void deregisterListener(final CommonListener commonListener) {

        // 多事件监听器
        if (commonListener instanceof CommonMultiListener) {
            for (Class<? extends Event> subscribeType : ((CommonMultiListener) commonListener).subscribeTypes()) {
                if (ShareEvent.class.isAssignableFrom(subscribeType)) {
                    INSTANCE.shareDispatcher.removeListener(commonListener, subscribeType);
                } else {
                    removeListener(commonListener, subscribeType);
                }
            }
            return;
        }

        // 单事件监听器
        final Class<? extends Event> subscribeType = commonListener.subscribeType();
        if (ShareEvent.class.isAssignableFrom(subscribeType)) {
            INSTANCE.shareDispatcher.removeListener(commonListener, subscribeType);
            return;
        }

        if (removeListener(commonListener, subscribeType)) {
            return;
        }
        throw new NoSuchElementException("所要取消的订阅者没有发布者！");
    }

    private static boolean removeListener(final CommonListener commonListener, Class<? extends Event> subscribeType) {
        final String topic = subscribeType.getCanonicalName();
        Dispatcher eventDispatcher = INSTANCE.dispatcherMap.get(topic);
        if (null == eventDispatcher) {
            return false;
        }
        if (eventDispatcher instanceof ShardedEventDispatcher) {
            ((ShardedEventDispatcher) eventDispatcher).removeListener(commonListener, subscribeType);
        } else {
            eventDispatcher.removeListener(commonListener);
        }
        return true;
    }


    //-----------------------------------------------------------------事件管理------------------------------------------------------------------------------------------

    /**
     * 发布事件
     *
     * @param event
     * @return
     */
    public static boolean publishEvent(final Event event) {
        try {
            return publishEvent(event.getClass(), event);
        } catch (Exception e) {
            log.error("发布事件发生异常:{}", Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    private static boolean publishEvent(final Class<? extends Event> eventType, final Event event) {
        // 共享队列事件
        if (ShareEvent.class.isAssignableFrom(eventType)) {
            return INSTANCE.shareDispatcher.publish(event);
        }

        // 普通事件
        final String topic = eventType.getCanonicalName();
        Dispatcher dispatcher = INSTANCE.dispatcherMap.get(topic);
        if (dispatcher != null) {
            return dispatcher.publish(event);
        }

        log.warn("该事件:{}，没有对应的发布者！", topic);
        return false;
    }

    //-----------------------------------------------------------------事件分派器管理------------------------------------------------------------------------------------------

    /**
     * 注册事件对应的分派器（事件分派器不存在时，采用默认的事件分派器工厂创建）
     *
     * @param eventType
     * @param queueMaxSize
     * @return
     */
    public static Dispatcher registerToDispatcher(final Class<? extends Event> eventType, final int queueMaxSize) {
        return registerToDispatcher(eventType, DEFAULT_DISPATCHER_FACTORY, queueMaxSize);
    }

    public static Dispatcher registerToDispatcher(final Class<? extends Event> eventType, final DispatcherFactory factory, final int queueMaxSize) {
        // 共享队列事件
        if (ShareEvent.class.isAssignableFrom(eventType)) {
            return INSTANCE.shareDispatcher;
        }

        // 普通事件
        final String topic = eventType.getCanonicalName();
        synchronized (EventManager.class) {
            MapUtil.computeIfAbsent(INSTANCE.dispatcherMap, topic, factory, eventType, queueMaxSize);
        }
        return INSTANCE.dispatcherMap.get(topic);
    }

    /**
     * 注册事件对应的分派器
     *
     * @param eventType
     * @param dispatcher
     */
    public static void registerToDispatcher(final Class<? extends Event> eventType, final Dispatcher dispatcher) {
        if (null == dispatcher) {
            return;
        }
        final String topic = eventType.getCanonicalName();
        synchronized (EventManager.class) {
            INSTANCE.dispatcherMap.putIfAbsent(topic, dispatcher);
        }
    }

    /**
     * 移除事件对应的分派器
     *
     * @param eventType
     */
    public static void deregisterDispatcher(final Class<? extends Event> eventType) {
        final String topic = eventType.getCanonicalName();
        Dispatcher dispatcher = INSTANCE.dispatcherMap.remove(topic);
        try {
            dispatcher.shutdown();
        } catch (Exception e) {
            log.error("事件分派器shutdown异常:{}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * 获取共享队列事件分派器
     *
     * @return
     */
    public static Dispatcher getShareDispatcher() {
        return INSTANCE.shareDispatcher;
    }

    /**
     * 获取事件与分派器映射关系集合
     *
     * @return
     */
    public static Map<String, Dispatcher> getDispatcherMap() {
        return INSTANCE.dispatcherMap;
    }

    /**
     * 获取指定事件对应的分派器
     *
     * @param topic
     * @return
     */
    public static Dispatcher getDispatcher(Class<? extends Event> topic) {
        if (ShareEvent.class.isAssignableFrom(topic)) {
            return INSTANCE.shareDispatcher;
        }
        return INSTANCE.dispatcherMap.get(topic.getCanonicalName());
    }

}
