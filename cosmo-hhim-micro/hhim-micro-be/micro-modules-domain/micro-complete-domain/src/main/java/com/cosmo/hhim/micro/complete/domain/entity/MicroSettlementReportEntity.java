/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 计件结算报告对象 micro_settlement_report
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
public class MicroSettlementReportEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 报工ID
     */
    private Long submitId;

    /**
     * 结算员工ID
     */
    private Long employeeId;

    /**
     * 审核后数量
     */
    private BigDecimal checkedNum;

    /**
     * 已结算数量
     */
    private BigDecimal settledNum;

    /**
     * 调整后可结数量
     */
    private BigDecimal adjustedNum;
    /**
     * 调整前数量(配合插入历史表使用)
     */
    private BigDecimal adjustedFromNum;

    /**
     * 产品完工数量(记录结算时扣减的完工报告的产品数量)
     */
    private BigDecimal productCompleteNum;
    /**
     * 记录结算时扣减的完工报告单号
     */
    private String productCompleteReportNo;

    /**
     * 结算日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date settlementDay;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;
    private String productUnit;

    /**
     * 操作工序唯一码
     */
    private String operateProcessSeq;

    /**
     * 操作工序编码
     */
    private String operateProcessCode;

    /**
     * 操作工序名称
     */
    private String operateProcessName;

    /**
     * 结算类型(未结算10,已结算20)
     */
    private String settlementStatus;

    /**
     * 结算报告号
     */
    private String settlementNo;
    /**
     * 备注
     */
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
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdDate;

}
