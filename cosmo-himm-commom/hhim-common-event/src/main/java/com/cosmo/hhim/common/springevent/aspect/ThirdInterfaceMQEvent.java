/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.springevent.aspect;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.event.event.EventHeader;
import org.springframework.context.ApplicationEvent;

/**
 * @author cosmo-hhim-open Team
 * @description 抽象spring的ApplicationEvent事件
 *              对业务ThreadLocal中的的核心信息提取组装到EventHeader中
 * @createTime 2022-07-21
 */
public abstract class ThirdInterfaceMQEvent extends ApplicationEvent {

    private final EventHeader eventHeader;

    public ThirdInterfaceMQEvent(Object source) {
        super(source);
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
