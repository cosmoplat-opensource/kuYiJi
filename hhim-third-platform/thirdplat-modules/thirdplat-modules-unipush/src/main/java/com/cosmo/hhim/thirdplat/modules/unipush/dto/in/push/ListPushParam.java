/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Audience;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-23
 */
@Data
public class ListPushParam {
    // 推送目标用户
    private Audience audience;

    // 是否异步推送，true是异步，false同步。异步推送不会返回data详情
    @JSONField(name = "is_async")
    private boolean isAsync;

    // 使用创建消息接口返回的taskId，可以多次使用
    private String taskid;
}
