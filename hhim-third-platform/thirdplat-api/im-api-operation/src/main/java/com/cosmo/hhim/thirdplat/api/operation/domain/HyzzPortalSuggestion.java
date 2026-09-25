/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 门户的意见反馈对象 hyzz_portal_suggestion
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HyzzPortalSuggestion extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private Long id;

    /**
     * 编码
     */
    private String orderNo;

    /**
     * 类型(数据字典hyzz_suggestion_type)
     */
    @NotBlank(message = "类型不能为空！！")
    private String type;

    /**
     * 状态(数据字典hyzz_suggestion_status)
     */
    private String status;

    /**
     * 问题描述
     */
    @Size(max = 255, message = "问题描述不能超过255")
    private String content;

    /**
     * 处理方式
     */
    private String dealWay;

    /**
     * 处理结果
     */
    private String dealResult;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date dealTime;

    /**
     * 完成人
     */
    private String finishBy;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date finishTime;

    /**
     * 联系电话
     */
    @Size(max = 20, message = "联系电话不能超过20")
    private String contactPhone;

    /**
     * 在用标志（1在用；0停用）
     */
    private String activeFlag;

    /**
     * 创建者昵称
     */
    private String createByName;

    /**
     * 租户编码
     */
    @NotBlank(message = "租户编码不能为空！！")
    private String tenantCode;

    /**
     * 租户名称
     */
    private String tenantName;
    /**
     * 来源平台
     */
    private String sourcePlat;
    /**
     * 是否自动处理
     */
    private Integer autoDeal;

    /**
     * 附件列表
     */
    private List<HyzzFiles> hyzzFiles;


    private Integer pageNum;
    private Integer pageSize;
    private String orderByColumn;
    private String isAsc = "asc";
}
