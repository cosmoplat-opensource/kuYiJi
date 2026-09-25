/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.micro.infrastructure.annotation.ChangeField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 生产任务对象 micro_manufacture_task
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-09
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroManufactureTaskDto extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** id */
    private Long id;

    /** 租户编码 */
    private String tenantCode;

    /** 任务单号 */
    private String taskNo;

    /** 工单号 */
    private String workOrderNo;

    /** 生产订单号 */
    private String orderNo;

    /** 产品Seq */
    private String productSeq;

    /** 工序Seq */
    private String processSeq;

    /** 工序编码 */
    private String processCode;

    /** 计划数量 */
    private BigDecimal taskPlanNum;

    /** 可报工数量 */
    private BigDecimal submitableNum;

    /** 实际报工合格数量 */
    private BigDecimal passNum;

    /** 不良品数量 */
    private BigDecimal ngNum;

    /** 待处理不合格数量 */
    private BigDecimal pendNgNum;

    /** 报工状态（00-待报工；10-报工中；20-报工完成，30-已关闭） */
    private String submitStatus;

    /** 上一状态*/
    private String beforeStatus;

    /** 车间编码 */
    private String wshopCode;

    /** 产线编码 */
    private String mlineCode;

    /** STANDARD标准  DRAFT草稿 */
    private String techType;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;

    /** 激活标记1是0否 */
    private String activeFlag;

    /**
     * 标准工序id
     */
    private Long processId;

    /**
     * 标准工序名称
     */
    private String processName;


    /** 生产完成进度 已产/计划*/
    private String completionRate;

    /**
     * 是否最后一道工序
     */
    private String isLastProcess;

    /**
     * 是否是首序
     */
    private String isFirstProcess;

    /**
     * 报工图片url
     * 使用分号进行分割
     */
    @ChangeField
    private String submitPictures;


}
