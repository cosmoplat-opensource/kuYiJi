/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.events;

import org.springframework.context.ApplicationEvent;

/**
 * 用户推荐统计时间窗口刷新事件
 *
 * @author cosmo-hhim-open Team
 */
public class UserRecommendStatisticsEvent extends ApplicationEvent {
    /**
     * 接收信息
     */
    private final Long userId;
    private final String tenantCode;

    public UserRecommendStatisticsEvent(Long userId, String tenantCode) {
        super(userId);
        this.userId = userId;
        this.tenantCode = tenantCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTenantCode() {
        return tenantCode;
    }
}