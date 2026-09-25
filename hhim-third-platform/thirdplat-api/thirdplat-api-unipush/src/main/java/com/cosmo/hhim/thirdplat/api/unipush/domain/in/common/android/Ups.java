/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android;

import lombok.Data;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class Ups {
    /**
     * 通知消息内容，与transmission 二选一，两个都填写时报错
     */
    private ThirdNotification notification;
    /**
     * 透传消息内容，与notification 二选一，两个都填写时报错，长度 ≤ 3072
     */
    private String transmission;

    /**
     * 第三方厂商扩展内容
     * 扩展内容对应厂商通道设置如：ALL,HW,XM,VV,OP,MZ,ST -> (	厂商内容扩展字段 -> value的设置根据key值决定)
     */
    private Map<String, Map<String, Object>> options;
}
