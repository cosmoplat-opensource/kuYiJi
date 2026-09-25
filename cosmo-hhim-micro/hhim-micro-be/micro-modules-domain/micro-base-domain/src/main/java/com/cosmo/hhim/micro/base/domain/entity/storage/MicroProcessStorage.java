/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存对象 micro_process_storage
 *
 * @date 2022-10-12
 */
@Data
public class MicroProcessStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品唯一码(用来关联产品)
     */
    private Long productId;
    private String productSeq;
    @Excel(name = "产品编码", sort = 1)
    private String productCode;
    @Excel(name = "产品名称", sort = 2)
    private String productName;
    private String productUnit;

    /**
     * 工序唯一码（用来关联工序）
     */
    private String processSeq;
    private String processCode;
    @Excel(name = "工序", sort = 3)
    private String processName;
    private String isLastProcess;

    /**
     * 良品
     */
    @Excel(name = "良品数", sort = 4, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal passNum;

    /**
     * 不良品
     */
    @Excel(name = "不良品数", sort = 5, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal ngNum;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdDate;

    /**
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    private Date lastUpdDate;

    /**
     * 首序
     */
    private String isFirstProcess;

    /**
     * 工序名称或者编码
     */
    private String processNameOrCode;
}
