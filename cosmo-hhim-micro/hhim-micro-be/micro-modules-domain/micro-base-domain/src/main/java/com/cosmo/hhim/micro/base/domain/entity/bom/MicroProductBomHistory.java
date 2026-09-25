/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.bom;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品BOM对象 micro_product_bom_history
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
@Data
public class MicroProductBomHistory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 租户编码 */
    private String tenantCode;

    /** 产品编码 */
    private String productSeq;

    /** 产品类型(成品CP、半成品BCP、原材料YCL) */
    private String productType;

    /** 制造方式 */
    private String productionMode;

    /** 使用数量  */
    private BigDecimal productNumber;

    /** 上级编码 */
    private String parentProductSeq;

    /** 上级id */
    private Long parentId;

    /** 是否存在下级  0否  1是 */
    private String ifChildren;

    /** STANDARD标准 DRAFT 草稿 */
    private String bomType;

    /** 历史版本 */
    private String version;

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

    /** 激活标记1是0否 */
    private String activeFlag;

}
