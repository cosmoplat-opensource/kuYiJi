/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 产品BOM对象 micro_product_bom
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProductBomDto extends BaseEntity
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

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;

    /** 激活标记1是0否 */
    private String activeFlag;

    /**
     * 二级BOM集合
     */
    private List<MicroProductBomDto> secondBomList;

    /**
     * 操作类型
     */
    private String formType;

    /**
     * 工艺编码
     */
    private String techCode;

    /**
     * 工艺名称
     */
    private String techName;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 产品单位
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 子BOM数量
     */
    private int sonNum;

    /**
     * 子bom库存数量
     */
    private BigDecimal stockNum;

}
