/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushMessage;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class SinglePushCidMsg extends BasePushMessage {
    // 推送目标设备ID
    @NotBlank(message = "推送目标设备ID不允许为空")
    private String cid;

    // 个推通道消息
//    @NotEmpty(message = "个推通道消息不允许为空")
    private PushMessage pushMessage;

    // 厂商通道消息
    private PushChannel pushChannel;
}
