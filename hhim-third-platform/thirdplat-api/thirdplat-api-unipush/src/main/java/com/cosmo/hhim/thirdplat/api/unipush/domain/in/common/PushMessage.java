/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 个推通道消息内容
 *      通知消息(notification)，仅支持安卓系统，iOS系统不展示个推通道下发的通知消息
 * @createTime 2021-09-22
 */
@Data
public class PushMessage {
    // 手机端通知展示时间段，格式为毫秒时间戳段，两个时间的时间差必须大于10分钟，例如："1590547347000-1590633747000"
    private String duration;

    // 通知消息内容，仅支持安卓系统，iOS系统不展示个推通知消息，与transmission、revoke三选一，都填写时报错
    private Notification notification;

    // 纯透传消息内容，安卓和iOS均支持，与notification、revoke 三选一，都填写时报错，长度 ≤ 3072
    private String transmission;

    // 撤回消息时使用(仅支持撤回个推通道消息)，与notification、transmission三选一，都填写时报错(消息撤回请勿填写策略参数)
    private Revoke revoke;
}
