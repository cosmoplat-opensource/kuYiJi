/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description ios厂商通道消息
 * @createTime 2021-09-22
 */
@Data
public class IosChannel {
    /**
     * voip：voip语音推送，notify：apns通知消息
     */
    private String type;

    /**
     * 推送通知消息内容
     */
    private Aps aps;
    /**
     * 用于计算icon上显示的数字，还可以实现显示数字的自动增减，如“+1”、 “-1”、 “1” 等，计算结果将覆盖badge
     */
    @JSONField(name = "auto_badge")
    private String autoBadge;
    /**
     * 增加自定义的数据
     */
    private String payload;
    /**
     * 多媒体设置
     */
    private List<Multimedia> multimedia;

    /**
     * 使用相同的apns-collapse-id可以覆盖之前的消息
     */
    @JSONField(name = "apns-collapse-id")
    private String apnsCollapseId;

}
