/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push;

import com.alibaba.fastjson.annotation.JSONField;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Audience;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class BatchSingleCidPushParam {

    // 是否异步推送，true是异步，false同步。异步推送不会返回data详情
    @JSONField(name = "is_async")
    private boolean isAsync;

    // 消息内容，数组长度不大于 200
    @JSONField(name = "msg_list")
    private List<PushParam<Audience>> msgList;
}
