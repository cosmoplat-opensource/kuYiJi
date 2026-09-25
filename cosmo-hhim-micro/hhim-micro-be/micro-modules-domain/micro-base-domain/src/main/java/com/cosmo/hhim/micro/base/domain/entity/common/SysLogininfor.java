/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 系统访问记录表 sys_logininfor
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class SysLogininfor extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long infoId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 状态 0成功 1失败
     */
    private String status;

    /**
     * 地址
     */
    private String ipaddr;

    /**
     * 描述
     */
    private String msg;

    // 来源平台类型 
    private String platformSource;

    // 隶属系统 
    private String belongSys;

    /**
     * 访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date accessTime;
}