/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import lombok.Data;

/**
 * 返修复核对象 micro_repair_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
@Data
@ApiModel(value = "返修复核对象 micro_repair_record", description = "返修复核对象 micro_repair_record")
public class MicroRepairRecord extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @ApiModelProperty(value = "自增主键id", name = "id", example = "")
    private Long id;

    /**
     * 返修单号
     */
    @Excel(name = "返修单号")
    @ApiModelProperty(value = "返修单号", name = "repairNo", example = "")
    private String repairNo;

    /**
     * 报工记录id
     */
    @Excel(name = "报工记录id")
    @ApiModelProperty(value = "报工记录id", name = "submitId", example = "")
    private Long submitId;

    /**
     * 产品序列码
     */
    @Excel(name = "产品序列码")
    @ApiModelProperty(value = "产品序列码", name = "productSeq", example = "")
    private String productSeq;

    /**
     * 工序序列码
     */
    @Excel(name = "工序序列码")
    @ApiModelProperty(value = "工序序列码", name = "processSeq", example = "")
    private String processSeq;

    /**
     * 前工序序列码
     */
    @Excel(name = "前工序序列码")
    @ApiModelProperty(value = "前工序序列码", name = "preProcessSeq", example = "")
    private String preProcessSeq;

    /**
     * 报工人
     */
    @Excel(name = "报工人")
    @ApiModelProperty(value = "报工人", name = "submitUser", example = "")
    private Long submitUser;

    /**
     * 返修人
     */
    @Excel(name = "返修人")
    @ApiModelProperty(value = "返修人", name = "repairUser", example = "")
    private Long repairUser;

    /**
     * 返修完成数量
     */
    @Excel(name = "返修完成数量")
    @ApiModelProperty(value = "返修完成数量", name = "repairNum", example = "")
    private BigDecimal repairNum;

    /**
     * 让步接收数量
     */
    @Excel(name = "让步接收数量")
    @ApiModelProperty(value = "让步接收数量", name = "concessionNum", example = " ")
    private BigDecimal concessionNum;

    /**
     * 报废数量
     */
    @Excel(name = "报废数量")
    @ApiModelProperty(value = "报废数量", name = "abandonedNum", example = "")
    private BigDecimal abandonedNum;

    /**
     * 报废类型
     */
    @Excel(name = "报废类型")
    @ApiModelProperty(value = "报废类型", name = "abandonedType", example = " ")
    private String abandonedType;

    /**
     * 复核人员
     */
    @Excel(name = "复核人员")
    @ApiModelProperty(value = "复核人员", name = "createdBy", example = "")
    private Long createdBy;

    /**
     * 复核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "复核时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "复核时间", name = "createdDate", example = "")
    private Date createdDate;

    /**
     * 最后修改人
     */
    @Excel(name = "最后修改人")
    @ApiModelProperty(value = "最后修改人", name = "lastUpdBy", example = "")
    private Long lastUpdBy;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后修改时间", width = 30, dateFormat = "yyyy-MM - dd")
    @ApiModelProperty(value = "最后修改时间", name = "lastUpdDate", example = " ")
    private Date lastUpdDate;

    /**
     * 激活标记 1是 0否
     */
    @Excel(name = "激活标记 1是 0否")
    @ApiModelProperty(value = "激活标记 1是 0否", name = "activeFlag", example = "")
    private String activeFlag;

    /**
     * 租户编码
     */
    @Excel(name = "租户编码")
    @ApiModelProperty(value = "租户编码", name = "tenantCode", example = "")
    private String tenantCode;
}
