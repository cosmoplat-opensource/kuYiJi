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
 * 用户对象 micro_user
 *
 * @date 2022-10-11
 */
@Data
public class MicroTags implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 租户编码
     */
    private String tenantCode;
    /**
     * 账号
     */
    private String tagName;
    /**
     * 类型
     */
    private String tagType;
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
