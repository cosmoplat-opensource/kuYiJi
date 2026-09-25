/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 撤回消息时使用(仅支持撤回个推通道消息)，与notification、transmission三选一，都填写时报错(消息撤回请勿填写策略参数)
 * @createTime 2021-09-22
 */
@Data
public class Revoke {
    // 需要撤回的taskId
    @JSONField(name = "old_task_id")
    private String oldTaskId;

    // 在没有找到对应的taskId，是否把对应appId下所有的通知都撤回
    private boolean force;
}
