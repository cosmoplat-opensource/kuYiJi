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
 * 扩展字段对象 micro_extend_field
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
@ApiModel(value = "扩展字段对象 micro_extend_field", description = "扩展字段对象 micro_extend_field")
public class MicroExtendField extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @ApiModelProperty(value = "id", name = "id", example = "")
    private Long id;


    /**
     * 扩展字段
     */
    @ApiModelProperty(value = "扩展字段", name = "extField", example = "")
    private String extField;

    /**
     * 扩展字段名称
     */
    @ApiModelProperty(value = "扩展字段名称", name = "extFieldLabel", example = "")
    private String extFieldLabel;

    /**
     * 扩展字段选项
     */
    @ApiModelProperty(value = "扩展字段选项", name = "extFieldOption", example = "")
    private String extFieldOption;

    /**
     * 字段类型（1-字符串；2-数字；3-日期时间；4-枚举-单选；5-集合-多选 ）
     */
    @ApiModelProperty(value = "字段类型（1-字符串；2-数字；3-日期时间；4-枚举-单选；5-集合-多选 ）", name = "fieldType", example = "")
    private String fieldType;

    /**
     * 在用标志（1-在用；0-删除）
     */
    @ApiModelProperty(value = "在用标志（1-在用；0-删除）", name = "activeFlag", example = "")
    private String activeFlag;

    /**
     * 创建者
     */
    @ApiModelProperty(value = "创建者", name = "createdBy", example = "")
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间", name = "createdDate", example = "")
    private Date createdDate;

    /**
     * 更新者
     */
    @Excel(name = "更新者")
    private String lastUpdBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(value = "更新时间", name = "lastUpdDate", example = "")
    private Date lastUpdDate;

    /**
     * 租户编码
     */
    @ApiModelProperty(value = "租户编码", name = "tenantCode", example = "")
    private String tenantCode;

}

