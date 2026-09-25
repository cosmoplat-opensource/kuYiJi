/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author cosmo-hhim-open Team
 * @description: 客户基础信息数据传输类
 * @classname: MicroCustomerDTO
 * @date: 2023/3/8 9:51
 * @author cosmo-hhim-open Team
 * @version 1.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroCustomerDTO extends BaseEntity {

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
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date updatedDate;

    private String key;
}
