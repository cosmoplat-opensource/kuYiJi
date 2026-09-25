/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;

import cn.hutool.core.collection.ConcurrentHashSet;
import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.event.ShareEvent;
import com.cosmo.hhim.common.event.listener.CommonListener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cosmo-hhim-open Team
 * @description 默认共享事件队列的分派器
 * @createTime 2022/3/18
 */
public class DefaultShareDispatcher extends DefaultDispatcher implements ShardedEventDispatcher {

    // 维护事件与事件监听器的映射
    private final Map<Class<? extends ShareEvent>, Set<CommonListener>> subMappings = new ConcurrentHashMap<>();

    private final Lock lock = new ReentrantLock();

    /**
     * 添加指定事件的监听器
     * @param commonListener
     * @param subscribeType
     */
    @Override
    public void addListener(CommonListener commonListener, Class<? extends Event> subscribeType) {
        Class<? extends ShareEvent> subShareEventType = (Class<? extends ShareEvent>) subscribeType;

        commonListeners.add(commonListener);
        lock.lock();
        try {
            Set<CommonListener> sets = subMappings.get(subShareEventType);
            if (sets == null) {
                Set<CommonListener> newSet = new ConcurrentHashSet<>();
                newSet.add(commonListener);
                subMappings.put(subShareEventType, newSet);
                return;
            }
            sets.add(commonListener);
        } finally {
            lock.unlock();
        }

    }

    /**
     * 移除指定事件的监听器
     * @param commonListener
     * @param subscribeType
     */
    @Override
    public void removeListener(CommonListener commonListener, Class<? extends Event> subscribeType) {
        Class<? extends ShareEvent> subShareEventType = (Class<? extends ShareEvent>) subscribeType;

        commonListeners.remove(commonListener);

        lock.lock();
        try {
            Set<CommonListener> sets = subMappings.get(subShareEventType);

            if (sets != null) {
                sets.remove(commonListener);
            }
        } finally {
            lock.unlock();
        }
    }
}
