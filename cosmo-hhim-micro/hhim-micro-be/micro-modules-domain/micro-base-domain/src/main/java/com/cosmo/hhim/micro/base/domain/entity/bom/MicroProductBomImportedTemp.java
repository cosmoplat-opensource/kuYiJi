/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.bom;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * BOM导入临时对象 micro_product_bom_imported_temp
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-10
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroProductBomImportedTemp implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品编码
     */
    @Excel(name = "产品编码")
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称")
    private String productName;

    /**
     * 层级
     */
    @Excel(name = "层级")
    private Long hierarchy;

    /**
     * 组成数量
     */
    @Excel(name = "组成数量")
    private BigDecimal componentQuantity;

    // 备注 
    @Excel(name = "备注")
    private String remark;

    /**
     * 创建人
     */
    @Excel(name = "创建人")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "创建时间" , width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    @Excel(name = "更新人")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "更新时间" , width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

}
