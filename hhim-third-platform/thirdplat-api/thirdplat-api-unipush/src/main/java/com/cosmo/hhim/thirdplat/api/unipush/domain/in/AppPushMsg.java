/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushMessage;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class AppPushMsg extends BasePushMessage {
    // 任务组名
    private String groupName;

    // 消息离线时间设置，单位毫秒，-1表示不设离线，-1 ～ 3 * 24 * 3600 * 1000(3天)之间，默认：1h
    private Integer messageTtl;

    // 定速推送，例如100，个推控制下发速度在100条/秒左右，0表示不限速，默认：0
    private Integer speedLimit;

    // 定时推送时间，必须是7天内的时间，格式：毫秒时间戳，默认：无
    private Integer scheduleTime;

    // 个推通道消息
//    @NotEmpty(message = "个推通道消息不允许为空")
    private PushMessage pushMessage;

    // 厂商通道消息
    private PushChannel pushChannel;
}
