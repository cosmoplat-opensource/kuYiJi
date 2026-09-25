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
 * 用户与小程序映射关系对象 micro_user_app_re
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-10
 */
@Data
public class MicroUserAppRe implements Serializable {
    private static final long serialVersionUID = 1L;

    // 自增ID 
    private Long id;

    // 用户ID 
    private Long userId;

    // 用户名 
    private String userName;

    // 租户编码 
    private String tenantCode;

    // 应用编码 
    private String appCode;

    /**
     * 账号状态(0正常 1停用)
     */
    private String userStatus;

    /**
     * 账号有效期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date validDate;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

}
