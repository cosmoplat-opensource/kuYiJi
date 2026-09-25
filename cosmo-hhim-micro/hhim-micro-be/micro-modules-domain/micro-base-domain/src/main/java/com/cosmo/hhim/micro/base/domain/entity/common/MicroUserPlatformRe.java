/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户平台信息关联对象 micro_user_platfrom_re
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-21
 */
@Data
public class MicroUserPlatformRe implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 平台用户ID
     */
    private String openId;

    /**
     * 用户ID
     */
    private Long userId;

    // 应用类型 
    private String applicationSign;

    /**
     * 平台类型
     */
    private String platformType;

    /**
     * 可用标识（0：正常，1：停用）
     */
    private String activeFlag;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    /**
     * 备注
     */
    private String remark;
}
