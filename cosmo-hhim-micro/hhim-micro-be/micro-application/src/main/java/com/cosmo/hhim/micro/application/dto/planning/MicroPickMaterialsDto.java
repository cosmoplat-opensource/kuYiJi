/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 投料单/退料单对象 micro_pick_materials
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
@Data
public class MicroPickMaterialsDto extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** $column.columnComment */
    private Long id;

    /** 投退料单号（投料单LL开头；退料单TL开头） */
    private String pickingNo;

    /** 生产订单号 */
    private String orderNo;

    /** 工单号 */
    private String workOrderNo;

    /** 物料序列号 */
    private String productSeq;

    /** 类型：0:投料单，1：退料单 */
    private String materialsType;

    /** 实际投料/退料 */
    private BigDecimal pickingNumber;

    /** 实际退料 */
    private BigDecimal returnNumber;

    /** 激活标记 1是 0否 */
    private String activityFlag;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastUpDate;

    /** 租户编码 */
    private String tenantCode;

    /** 产品类型(成品CP、半成品BCP、原材料YCL) */
    private String productType;

    /** 制造方式 */
    private String productionMode;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * STANDARD标准 DRAFT 草稿
     */
    private String bomType;

}
