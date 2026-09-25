/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.custom;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 扩展字段关系对象 micro_extend_field_relation
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-27
 */
@ApiModel(value = "扩展字段关系对象 micro_extend_field_relation", description = "扩展字段关系对象 micro_extend_field_relation")
@Data
public class MicroExtendFieldRelation extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id", name = "id", example = "")
    private Long id;

    /**
     * 扩展字段id
     */
    @Excel(name = "扩展字段id")
    @ApiModelProperty(value = "扩展字段id", name = "extFieldId", example = "")
    private Long extFieldId;

    /**
     * 所属领域编码
     */
    @Excel(name = "所属领域编码")
    @ApiModelProperty(value = "所属领域编码", name = "businessCode", example = "")
    private String businessCode;

    /**
     * 在用标志（1-在用；0-删除）
     */
    @Excel(name = "在用标志", readConverterExp = "1=-在用；0-删除")
    @ApiModelProperty(value = "在用标志（1-在用；0-删除）", name = "activeFlag", example = "")
    private String activeFlag;

    /**
     * 创建者
     */
    @Excel(name = "创建者")
    @ApiModelProperty(value = "创建者", name = "createdBy", example = "")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间", name = "createdDate", example = "")
    private Date createdDate;

    /**
     * 更新者
     */
    @Excel(name = "更新者")
    @ApiModelProperty(value = "更新者", name = "lastUpdBy", example = "")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    @ApiModelProperty(value = "更新时间", name = "lastUpdDate", example = "")
    private Date lastUpdDate;

    /**
     * 序号
     */
    @Excel(name = "序号")
    @ApiModelProperty(value = "序号", name = "sequence", example = "")
    private Integer sequence;

    /**
     * 租户编码
     */
    @Excel(name = "租户编码")
    @ApiModelProperty(value = "租户编码", name = "tenantCode", example = "")
    private String tenantCode;

    /** 字段 例如：ext1*/
    private String extField;

    /** 字段名称*/
    private String extFieldLabel;

    /** 字段类型 （1-字符串；2-数字；3-日期时间；4-枚举-单选；5-集合-多选 ）*/
    private String fieldType;

    /** 字段选项*/
    private String extFieldOption;
}

