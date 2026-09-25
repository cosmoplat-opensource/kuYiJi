/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitParam;
import com.cosmo.hhim.micro.application.dto.base.MicroExtendFieldRelationDTO;
import com.cosmo.hhim.micro.application.dto.tech.MicroSaveSingleTechDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * 生产工单对象 micro_manufacture_work_order
 * 
 * @date 2023-03-06
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroManufactureWorkOrderDto extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 租户编码 */
    private String tenantCode;

    /** 工单号 */
    @SubmitParam
    private String workOrderNo;

    /** 生产订单号 */
    private String orderNo;

    /** 工单类型 */
    private String wordOrderType;

    /**
     * 产品id
     */
    private Long productId;

    /** 产品唯一编码 */
    private String productSeq;

    /** 产品编码 */
    private String productCode;

    /** 产品名称 */
    private String productName;

    /** 车间编码 */
    private String wshopCode;
    /** 车间名称 */
    private String wshopName;

    /** 生产线编码 */
    private String mlineCode;

    /** 生产线名称 */
    private String mlineName;

    /** 工单数量 */
    private BigDecimal workOrderNum;

    /** 订单交付日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date deliveryDate;

    /** 计划开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;

    /** 计划完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;

    /** 生产开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date produceStartDate;

    /** 生产完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date produceEndDate;

    /** 工单状态：10待生产、20生产中、30已完成、40关闭 */
    private String workOrderStatus;

    /** 工单状态：10待生产、20生产中、30已完成、40关闭 */
    private String workOrderStatusName;

    /** 原状态（上一状态） */
    private String beforeStatus;

    /** 开工人 */
    private Long sendBy;

    /** 开工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date sendDate;

    /** 入库时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date inboundDate;

    /** 完成数量 */
    private BigDecimal finishNum;

    /** 不良品数量 */
    private BigDecimal ngNum;

    /** 关单原因 */
    private String closeReason;

    /** 扩展字段 */
    private String extendContent;

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

    /** 产品类型(成品CP、半成品BCP、原材料YCL) */
    private String productType;

    /** 制造方式 */
    private String productionMode;

    /**
     * 生产任务集合
     */
    private List<MicroManufactureTaskDto> taskDtoList;

    /** STANDARD标准  DRAFT草稿 */
    private String techType;

    /** 工单下发前的状态 STANDARD标准  DRAFT草稿 */
    private String beforeTechType;

    /**
     * 工艺链绑定值对象
     */
    private MicroSaveSingleTechDTO microSaveSingleTechDTO;

    /** STANDARD标准 DRAFT 草稿 */
    private String bomType;

    /** 类型：0:投料单，1：退料单 */
    private String materialsType;

    /** 投/退料单集合 */
    private List<MicroPickMaterialsDto> microPickMaterialsDtoList;

    private String operateFlag;

    /** 主工单标记1是0否 */
    private String mainFlag;

    /** 工单状态数量*/
    private int workOrderStatusCount;

    /** 生产完成进度 已产/计划*/
    private String completionRate;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 查询参数  单号
     */
    private String queryNo;

    /**
     * 最后一道工序报工数
     */
    private BigDecimal passNum;

    /** 是否展示任务列表*/
    private boolean showTaskList;

    /** 客户名称*/
    private String owName;

    /** 订单计划数量*/
    private BigDecimal orderPlanNum;

    /** 自定义字段列表*/
    private List<MicroExtendFieldRelationDTO> extendFieldRelationList;

    /**
     * 逾期标示( ==1时进行判断)
     */
    private String overDateFlag;

    /**
     * 用于过滤工单列表
     *
     * 全部可用标示 ( ==1时显示全部可用的列表)
     *
     * 全部可用: 待生产、生产中 两个状态的工单
     */
    private String availableFlag;

    // 是否已完工(0:是，1:否) 
    private String isComplete;

    /**
     * 工单警示标示:
     *
     * 20 - 逾期
     */
    private String workOrderWarnFlag;
}
