/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.event;


import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;

/**
 * @author cosmo-hhim-open Team
 * @description 基础事件抽象
 * @createTime 2022/3/18
 */
public abstract class BaseEvent extends Event {

    private final EventHeader eventHeader;

    public BaseEvent() {
        String targetCustomer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String targetDs = (String) ThreadContext.get(Constants.TARGET_DS);
        String targetSchema = (String) ThreadContext.get(Constants.TARGET_SCHEMA);
        String traceId = (String) ThreadContext.get(Constants.TRACEID);
        eventHeader = EventHeader.builder().targetCustomer(targetCustomer).targetDs(targetDs).targetSchema(targetSchema).traceId(traceId).build();
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }

}