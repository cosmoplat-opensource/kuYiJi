/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.dispatcher;


import com.cosmo.hhim.common.event.event.Event;
import com.cosmo.hhim.common.event.listener.CommonListener;

/**
 * @author cosmo-hhim-open Team
 * @description 事件分配器定义
 * @createTime 2022/3/18
 */
public interface Dispatcher extends Closeable {

    /**
     * 初始化
     * @param type
     * @param bufferSize
     */
    void init(Class<? extends Event> type, int bufferSize);

    /**
     * 待发布的事件数量
     * @return
     */
    long currentEventSize();

    /**
     * 添加监听器
     * @param commonListener
     */
    void addListener(CommonListener commonListener);

    /**
     * 移除监听器
     * @param commonListener
     */
    void removeListener(CommonListener commonListener);

    /**
     * 发布事件
     * @param event
     * @return
     */
    boolean publish(Event event);

    /**
     * 通知指定的事件的监听器
     * @param commonListener
     * @param event
     */
    void notifyListener(CommonListener commonListener, Event event);

}
