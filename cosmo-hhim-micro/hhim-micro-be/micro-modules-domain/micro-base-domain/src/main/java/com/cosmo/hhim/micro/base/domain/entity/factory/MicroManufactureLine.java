/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.factory;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 生产线基础信息对象 micro_manufacture_line
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Data
public class MicroManufactureLine extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /**  租户编码 */
    private String tenantCode;

    /** 生产线编号 */
    private String mlineCode;

    /** 生产线名称 */
    private String mlineName;

    /** 车间编号 */
    private String wshopCode;

    /** 车间名称 */
    private String wshopName;

    /** 激活标记 1是 0否 */
    private String activeFlag;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;
}
