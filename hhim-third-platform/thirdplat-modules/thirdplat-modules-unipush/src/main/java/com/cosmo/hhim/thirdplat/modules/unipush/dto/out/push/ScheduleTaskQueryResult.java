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
public class ScheduleTaskQueryResult {

    // 定时任务创建时间，毫秒时间戳
    @JSONField(name = "create_time")
    private String createTime;

    // 定时任务状态：success/failed
    private String status;

    // 透传内容
    @JSONField(name = "transmission_content")
    private String transmissionContent;

    // 定时任务推送时间，毫秒时间戳
    @JSONField(name = "push_time")
    private String pushTime;
}
