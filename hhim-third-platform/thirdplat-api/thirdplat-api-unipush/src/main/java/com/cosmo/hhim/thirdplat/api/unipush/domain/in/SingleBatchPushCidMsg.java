/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class SingleBatchPushCidMsg {

    // 是否异步推送，true是异步，false同步。异步推送不会返回data详情，默认：false
    private Boolean async;

    // 消息列表
//    @NotEmpty(message = "批量推送的消息列表不允许为空")
    private List<SinglePushCidMsg> msgList;
}
