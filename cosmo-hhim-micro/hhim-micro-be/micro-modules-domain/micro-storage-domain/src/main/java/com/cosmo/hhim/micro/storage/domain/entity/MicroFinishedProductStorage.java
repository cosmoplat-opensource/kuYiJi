/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 成品库存对象 micro_finished_product_storage
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Data
public class MicroFinishedProductStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    // 产品唯一码 
    private String productSeq;

    /**
     * 产品编码
     */
    @Excel(name = "产品编码", sort = 1)
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称", sort = 2)
    private String productName;

    /**
     * 库存数量
     */
    @Excel(name = "产成品数量", sort = 3)
    private BigDecimal num;

    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 更新人
     */
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

    // 产品编码或名称 
    private String productCodeOrName;

    // 产品规格 
    private String productStandards;

    // 产品类型(成品CP、半成品BCP、原材料YCL) 
    private String productType;

    // 产品单位 
    private String productUnit;

    // 预警信息 
    private String warningInfo;

    // 安全库存上限 
    private BigDecimal stockUpperLimit;

    // 安全库存下限 
    private BigDecimal stockLowerLimit;

}

