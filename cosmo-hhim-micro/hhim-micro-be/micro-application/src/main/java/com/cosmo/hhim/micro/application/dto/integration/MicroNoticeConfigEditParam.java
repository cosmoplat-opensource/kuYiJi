/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.integration;

import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/8
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroNoticeConfigEditParam {

    // 通知业务标识 
    @NotNull(message = "通知业务标识不允许为空")
    private NotifyEnums.NoticeBusinessSignEnum businessSign;

    // 通知用户Id列表 
    private List<String> noticeUserIdList;

    // 通知角色编码列表 
    private List<String> noticeRoleCodeList;

    // 通知渠道列表 
    @NotNull(message = "通知渠道列表不允许为空！")
    private List<NotifyEnums.NoticeChannelEnum> noticeChannelList;
}
