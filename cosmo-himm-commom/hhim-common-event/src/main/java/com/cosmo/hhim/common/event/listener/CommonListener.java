/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.listener;


import com.cosmo.hhim.common.event.event.Event;

import java.util.concurrent.Executor;

/**
 * @author cosmo-hhim-open Team
 * @description 通用事件监听器抽象
 * @createTime 2022/3/18
 */
public abstract class CommonListener<T extends Event> {

    /**
     * 处理事件
     * @param event
     */
    public abstract void onEvent(T event);

    /**
     * 所关注的事件类型
     * @return
     */
    public abstract Class<? extends Event> subscribeType();

    /**
     * 异步处理事件线程池
     * @return
     */
    public Executor executor() {
        return null;
    }

    /**
     * 是否忽略过期事件
     * @return
     */
    public boolean ignoreExpireEvent() {
        return false;
    }
}
