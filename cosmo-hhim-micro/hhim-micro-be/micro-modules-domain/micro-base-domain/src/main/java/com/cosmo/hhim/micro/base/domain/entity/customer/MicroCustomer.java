/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.customer;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 客户基础对象 micro_customer
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Data
public class MicroCustomer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 租户编码 */
    private String tenantCode;

    /** 客户编码 */
    private String customerCode;

    /** 客户名称 */
    private String customerName;

    /** 客户简称 */
    private String customerShortName;

    /** 激活标记1是0否 */
    private String activeFlag;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date updatedDate;
}
