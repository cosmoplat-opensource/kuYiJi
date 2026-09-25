/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 产成品库存导入临时对象 micro_finished_storage_imported_temp
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-05
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroFinishedStorageImportedTemp {
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
     * 产品类型(成品CP、半成品BCP、原材料YCL)
     */
    @Excel(name = "产品类型(成品CP、半成品BCP、原材料YCL)")
    private String productType;

    /**
     * 安全库存上限
     */
    @Excel(name = "安全库存上限")
    private BigDecimal stockUpperLimit;

    /**
     * 安全库存下限
     */
    @Excel(name = "安全库存下限")
    private BigDecimal stockLowerLimit;

    /**
     * 单位
     */
    @Excel(name = "单位")
    private String unit;

    /**
     * 规格
     */
    @Excel(name = "规格")
    private String standards;

    // 当前库存数量 
    private BigDecimal num;

    /**
     * 导入库存数量
     */
    @Excel(name = "导入库存数量")
    private BigDecimal storageNum;

    // 备注 
    private String remark;

    /**
     * 创建人
     */
    @Excel(name = "创建人")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /**
     * 更新人
     */
    @Excel(name = "更新人")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;

}
