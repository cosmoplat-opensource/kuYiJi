/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 推送条件设置
 * @createTime 2021-09-22
 */
@Data
public class Setting {
    // 消息离线时间设置，单位毫秒，-1表示不设离线，-1 ～ 3 * 24 * 3600 * 1000(3天)之间
    private Integer ttl;

    // 厂商通道策略，详细内容见strategy
    private Strategy strategy;

    // 定速推送，例如100，个推控制下发速度在100条/秒左右，0表示不限速
    private Integer speed;

    // 定时推送时间，格式：毫秒时间戳，此功能需要开通VIP，如需开通请点击右侧“技术咨询”了解详情
    @JSONField(name = "schedule_time")
    private Integer scheduleTime;
}
