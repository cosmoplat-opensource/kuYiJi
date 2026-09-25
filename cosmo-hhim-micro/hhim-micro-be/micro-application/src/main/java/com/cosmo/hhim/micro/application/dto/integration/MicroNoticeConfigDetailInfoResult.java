/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.integration;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroNoticeConfig;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/8
 */
@Data
public class MicroNoticeConfigDetailInfoResult extends MicroNoticeConfig {

    // 通知用户集合 
    private List<MicroUser> noticeUserList;

    // 通知的角色集合 
    private List<MicroRole> noticeRoleList;

    // 通知渠道集合 
    private List<NotifyEnums.NoticeChannelEnum>  noticeChannelEnumList;

    // 通知业务条件 
    private Map<String, String> noticeConditionMap;
}
