/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 附件对象 hyzz_files
 *
 * @author cosmo-hhim-open Team
 */
@Data
@ApiModel(value="附件对象",description="附件对象")
public class HyzzFiles extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    @ApiModelProperty(value = "主键id", name = "rowId", example = "")
    private Long rowId;

    /** 相关表名 */
    @ApiModelProperty(value = "相关表名", name = "fileTableName", example = "")
    private String fileTableName;

    /** 相关表row_id */
    @ApiModelProperty(value = "相关表row_id", name = "fileTableId", example = "")
    private String fileTableId;

    /** 文件名称 */
    @ApiModelProperty(value = "文件名称", name = "fileName", example = "")
    private String fileName;

    /** 文件上传路径 */
    @ApiModelProperty(value = "文件上传路径", name = "filePath", example = "")
    private String filePath;

    /** 附件类型(1、图片 2、音频 3、视频 4、文件) */
    @ApiModelProperty(value = "附件类型(1、图片 2、音频 3、视频 4、文件)", name = "fileType", example = "")
    private String fileType;

    /** 附件大小 */
    @ApiModelProperty(value = "附件大小", name = "fileSize", example = "")
    private String fileSize;

    /** 附件时长(音视频使用) */
    @ApiModelProperty(value = "附件时长(音视频使用)", name = "fileMins", example = "")
    private String fileMins;

    /** 在用标志0-删除；1-在用 */
    @ApiModelProperty(value = "在用标志0-删除；1-在用", name = "activeFlag", example = "")
    private String activeFlag;

    /** 扩展字段1 */
    private String ext1;

    /** 扩展字段2 */
    private String ext2;

    /** 扩展字段3 */
    private String ext3;

    /**
     * 类型 A新增 D删除
     */
    private String flag;

}
