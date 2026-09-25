/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * 获取所有产品+工序目前的良品率及日产能指标事件
 *
 * @author cosmo-hhim-open Team
 */
public class FlushWarningMetricsEvent extends ApplicationEvent {
    /**
     * 接收信息
     */
    private final String message;

    public FlushWarningMetricsEvent(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}