/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * 刷新用户信息事件
 *
 * @author cosmo-hhim-open Team
 */
public class FlushCacheUserInfoEvent extends ApplicationEvent {
    /**
     * 接收信息
     */
    private final String message;

    public FlushCacheUserInfoEvent(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}