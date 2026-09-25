/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Data
public class ListPushCidMsg {
    // 是否异步推送，true是异步，false同步。异步推送不会返回data详情，默认：false
    private Boolean async;

    // 推送设备ID数组，数组长度不大于1000
//    @NotEmpty(message = "推送设备ID数组不允许为空")
    private List<String> cids;

    // 推送任务ID，使用创建消息接口返回的taskId，可以多次使用
    @NotBlank(message = "推送的任务ID不允许为空")
    private String taskId;
}
