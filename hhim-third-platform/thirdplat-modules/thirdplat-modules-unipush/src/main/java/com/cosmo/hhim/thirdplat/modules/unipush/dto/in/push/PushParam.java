/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push;

import com.alibaba.fastjson.annotation.JSONField;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.PushMessage;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Setting;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description cid单推请求参数
 * @createTime 2021-09-22
 */
@Data
public class PushParam<T> {
    // 请求唯一标识号，10-32位之间；如果request_id重复，会导致消息丢失
    @JSONField(name = "request_id")
    private String requestId;

    // 任务组名
    @JSONField(name = "group_name")
    private String groupName;

    // 推送目标用户，详细解释见下方audience说明
    private T audience;

    // 推送条件设置，详细解释见下方settings说明
    private Setting settings;

    // 个推推送消息参数，详细内容见push_message
    @JSONField(name = "push_message")
    private PushMessage pushMessage;

    // 厂商推送消息参数，包含ios消息参数，android厂商消息参数，详细内容见push_channel
    @JSONField(name = "push_channel")
    private PushChannel pushChannel;

}
