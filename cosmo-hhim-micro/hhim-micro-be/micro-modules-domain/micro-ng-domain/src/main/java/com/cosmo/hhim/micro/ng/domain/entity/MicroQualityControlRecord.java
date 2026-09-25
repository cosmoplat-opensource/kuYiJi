/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import com.cosmo.hhim.common.core.annotation.Excel;

/**
 * 质检记录对象 micro_quality_control_record
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-31
 */
@Data
@ApiModel(value = "质检记录对象 micro_quality_control_record", description = "质检记录对象 micro_quality_control_record")
public class MicroQualityControlRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 自增主键id
     */
    @ApiModelProperty(value = "自增主键id", name = "id", example = "")
    private Long id;

    /**
     * 质检记录号
     */
    @Excel(name = "质检记录号")
    @ApiModelProperty(value = "质检记录号", name = "qualityControlRecordNo", example = " ")
    private String qualityControlRecordNo;

    /**
     * 报工记录id
     */
    @Excel(name = "报工记录id")
    @ApiModelProperty(value = "报工记录id", name = "submitId", example = " ")
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
    @ApiModelProperty(value = "工序序列码", name = "processSeq", example = " ")
    private String processSeq;

    /**
     * 前工序序列码
     */
    @Excel(name = "前工序序列码")
    @ApiModelProperty(value = "前工序序列码", name = "preProcessSeq", example = " ")
    private String preProcessSeq;

    /**
     * 不良类型
     */
    @Excel(name = "不良类型")
    @ApiModelProperty(value = "不良类型", name = "ngType", example = "")
    private String ngType;

    /**
     * 良品数, 质检之后的, 形成的质检记录第一条记录记录所有良品数, 其余为0
     */
    @Excel(name = "良品数")
    @ApiModelProperty(value = "良品数", name = "passNum", example = "")
    private BigDecimal passNum;

    /**
     * 不良数量
     */
    @Excel(name = "不良数量")
    @ApiModelProperty(value = "不良数量", name = "ngNum", example = "")
    private BigDecimal ngNum;

    /**
     * 报工人
     */
    @Excel(name = "报工人")
    @ApiModelProperty(value = "报工人", name = "submitUser", example = "")
    private String submitUser;

    /**
     * 报工时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "报工时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "报工时间", name = "submitDate", example = "")
    private Date submitDate;

    /**
     * 质检人员
     */
    @Excel(name = "质检人员")
    @ApiModelProperty(value = "质检人员", name = "createdBy", example = "")
    private Long createdBy;

    /**
     * 质检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "质检时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "质检时间", name = "createdDate", example = "")
    private Date createdDate;

    /**
     * 备注
     */
    @Excel(name = "备注")
    @ApiModelProperty(value = "备注", name = "remark", example = "")
    private String remark;

    /**
     * 最后更新人
     */
    @Excel(name = "最后更新人")
    @ApiModelProperty(value = "最后更新人", name = "lastUpdBy", example = " ")
    private Long lastUpdBy;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后更新时间 ", width = 30, dateFormat = "yyyy-MM - dd")
    @ApiModelProperty(value = "最后更新时间", name = "lastUpdDate", example = " ")
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

    /** *********
     *  冗余字段
     * ********/
    private String qcNickName;
}

