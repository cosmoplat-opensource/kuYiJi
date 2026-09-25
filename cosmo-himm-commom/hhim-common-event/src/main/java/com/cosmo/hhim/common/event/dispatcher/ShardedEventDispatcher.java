/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;


import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.listener.CommonListener;

/**
 * @author cosmo-hhim-open Team
 * @description 共享事件分派器定义
 * @createTime 2022/3/18
 */
public interface ShardedEventDispatcher extends Dispatcher {

    /**
     * 添加指定事件的监听器
     * @param commonListener
     * @param subscribeType
     */
    void addListener(CommonListener commonListener, Class<? extends Event> subscribeType);

    /**
     * 移除指定事件的监听器
     * @param commonListener
     * @param subscribeType
     */
    void removeListener(CommonListener commonListener, Class<? extends Event> subscribeType);

}
