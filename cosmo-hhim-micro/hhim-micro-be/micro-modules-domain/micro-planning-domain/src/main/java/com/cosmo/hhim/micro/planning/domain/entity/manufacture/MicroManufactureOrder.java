/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.entity.manufacture;

import com.cosmo.hhim.common.core.web.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 生产订单对象 micro_manufacture_order
 * 
 * @date 2023-03-06
 */
@Data
public class MicroManufactureOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 生产订单号 */
    private String orderNo;

    /** 客户编码 */
    private String owCode;

    /** 客户编码 */
    private String owName;

    /** 产品编码 */
    private String productSeq;

    /** 产品编码 */
    private String productName;

    /** 计划产量*/
    private BigDecimal planNum;

    /** 交付日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date deliveryDate;

    /** 生产开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** 生产完工时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** 订单状态：10待排期、20待生产、30生产中、40已完成、50 关闭 */
    private String orderStatus;

    /** 原状态（上一状态） */
    private String beforeStatus;

    /** 完成数量 */
    private BigDecimal finishNum;

    /** 不良品数量 */
    private BigDecimal ngNum;

    /** 关单原因 */
    private String closeReason;

    /** 租户编码 */
    private String tenantCode;

    /** 扩展字段*/
    private String extendContent;

    /** 创建人 */
    private String createdBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createdDate;

    /** 最后修改人 */
    private String lastUpdBy;

    /** 最后修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date lastUpdDate;

    /** 激活标记1是0否 */
    private String activeFlag;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品单位
     */
    private String unit;

    /** 完成率*/
    private String completionRate;

    /** 待完成数*/
    private BigDecimal waitProduceNum;

    /** 是否展示任务列表*/
    private boolean showWorkOrderList;

    /** 状态数量*/
    private int orderStatusCount;

    /**
     * 工艺编码
     */
    private String techCode;

    /**
     * 工艺名称
     */
    private String techName;

    /**
     * 用于过滤生产订单列表
     *
     * 全部可用标示 ( ==1时显示全部可用的列表)
     *
     * 全部可用: 待排期、待生产、生产中三个状态的生产订单
     */
    private String availableFlag;

    /**
     * 生产订单的警示标示
     *
     * 10 - 交付预警
     * 20 - 逾期预警
     *
     */
    private String orderWarnFlag;

    /**
     * 用于查询警示的列表信息
     *
     *  == 1 时查询
     *
     */
    private String orderWarnQueryFlag;

    /**
     * 订单状态集合
     */
    private List<String> orderStatusList;
}
