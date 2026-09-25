/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class QueryPushDetailMsg {
    // 任务编号
    @NotBlank(message = "任务编号不允许为空")
    private String taskId;

    // 所推送的设备ID
    @NotBlank(message = "所推送的设备ID不允许为空")
    private String cid;
}
