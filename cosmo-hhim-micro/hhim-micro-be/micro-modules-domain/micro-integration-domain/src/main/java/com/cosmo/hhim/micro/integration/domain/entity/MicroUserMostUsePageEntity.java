/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 用户行为事件埋点对象 micro_user_event_tracking
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
@Data
public class MicroUserMostUsePageEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private MicroUserEventTrackingEntity conditionEntity;
    private Long userId;
    private List<String> pageList;
}
