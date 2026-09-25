/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/24
 */
@Data
public class WxMiniAppInviteCodeResult {

    // 成功时返回的图片 Buffer base64编码 
    private byte[] buffer;

    // 角色编码 
    private String roleCode;

    // 角色名称 
    private String roleName;

    // 邀请码 
    private String inviteCode;

    // 租户名称 
    private String tenantName;

    // 租户编码 
    private String tenantCode;

    // 邀请码到期时间 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private Date inviteCodeExpireTime;

}
