/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * 记录用户登录事件
 *
 * @author cosmo-hhim-open Team
 */
public class RecordUserLoginEvent extends ApplicationEvent {
    /**
     * 用户名
     */
    private final String username;

    public RecordUserLoginEvent(String username) {
        super(username);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}