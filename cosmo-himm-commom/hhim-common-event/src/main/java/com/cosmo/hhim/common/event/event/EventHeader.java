/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.event.event;

import lombok.Builder;
import lombok.Getter;

/**
 * @author cosmo-hhim-open Team
 * @description 事件头信息定义
 * @createTime 2022/3/18
 */
@Getter
@Builder
public class EventHeader {

    // 租户编码
    private final String targetCustomer;

    // 数据源
    private final String targetDs;

    // 数据库名
    private final String targetSchema;

    // 追踪码
    private final String traceId;
}