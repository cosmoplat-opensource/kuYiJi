/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;


import com.cosmo.hhim.common.event.event.Event;

import java.util.function.BiFunction;

/**
 * @author cosmo-hhim-open Team
 * @description 事件分派器工厂
 * @createTime 2022/3/18
 */
public interface DispatcherFactory extends BiFunction<Class<? extends Event>, Integer, Dispatcher> {

    /**
     * 构建自定义事件发布者
     * @param eventType
     * @param maxQueueSize
     * @return
     */
    @Override
    Dispatcher apply(Class<? extends Event> eventType, Integer maxQueueSize);
}
