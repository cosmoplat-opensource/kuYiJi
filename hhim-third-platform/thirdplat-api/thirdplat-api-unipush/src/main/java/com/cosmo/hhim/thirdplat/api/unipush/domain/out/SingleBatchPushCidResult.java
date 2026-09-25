/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.out;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class SingleBatchPushCidResult {
    // 任务编号
    private String taskId;
    // 推送结果详情列表
    private List<CidPushResult> cidPushResults;
}
