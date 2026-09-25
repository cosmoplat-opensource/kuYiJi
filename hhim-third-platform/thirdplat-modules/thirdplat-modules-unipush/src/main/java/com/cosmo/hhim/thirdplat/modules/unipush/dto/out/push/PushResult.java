/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-23
 */
@Data
public class PushResult {
    // 任务编号，用于执行cid批量推和执行别名批量推，此taskid可以多次使用，有效期为用户设置的离线时间
    @JSONField(name = "taskid")
    private String taskId;
}
