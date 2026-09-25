/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * @author cosmo-hhim-open Team
 */
public class OpenFeishuEvent extends ApplicationEvent {
    /**
     * 接收信息
     */
    private final String message;

    public OpenFeishuEvent(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}