/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 完工报告单对象 micro_complete_report
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Data
public class MicroCompleteReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键ID
     */
    private Long id;

    /**
     * 完工报告单号
     */
    @Excel(name = "完工报告单号")
    private String reportNo;

    /**
     * 产品唯一码
     */
    @Excel(name = "产品唯一码")
    private String productSeq;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称")
    private String productName;

    /**
     * 完工数量
     */
    @Excel(name = "完工数量")
    private BigDecimal completeNum;

    /**
     * 完工单状态（0:正常，1:撤销）
     */
    @Excel(name = "完工单状态", readConverterExp = "0=:正常，1:撤销")
    private String state;

    /**
     * 报工记录ID（尾序）/生产工单ID
     */
    @Excel(name = "报工记录ID", readConverterExp = "尾=序")
    private Long workSubmitId;

    /**
     * 完工时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "完工时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date completeTime;

    /**
     * 完工操作人ID
     */
    @Excel(name = "完工操作人ID")
    private Long completeUser;

    // 来源渠道 
    private String sourceChannel;

    private String remark;

    /**
     * 可用标识（0：正常，1：停用）
     */
    @Excel(name = "可用标识", readConverterExp = "0=：正常，1：停用")
    private String activeFlag;

    /**
     * 创建人
     */
    @Excel(name = "创建人")
    @ApiModelProperty(value = "创建人", name = "createdBy", example = "")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 更新人
     */
    @Excel(name = "更新人")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    // 分组标识（产品:工序） 
    private String productSeqAndProcessSeq;

}

