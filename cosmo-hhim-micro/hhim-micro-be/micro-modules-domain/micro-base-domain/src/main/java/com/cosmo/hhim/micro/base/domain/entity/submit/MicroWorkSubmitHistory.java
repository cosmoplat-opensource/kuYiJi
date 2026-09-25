/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 报工记录历史实体类
 *
 * @author cosmo-hhim-open Team
 * @since 2022-10-26 13:56:29
 */
@Data
public class MicroWorkSubmitHistory implements Serializable {
    private static final long serialVersionUID = 228527328085285484L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 前工序唯一码
     */
    private String preProcessSeq;

    /**
     * 前工序编码
     */
    private String preProcessCode;

    /**
     * 前工序名称
     */
    private String preProcessName;

    /**
     * 前工序分组
     */
    private String preProcessGroup;

    /**
     * 操作工序唯一码
     */
    private String operateProcessSeq;

    /**
     * 操作工序编码
     */
    private String operateProcessCode;

    /**
     * 操作工序名称
     */
    private String operateProcessName;

    /**
     * 操作工序分组
     */
    private String operateProcessGroup;

    /**
     * 是否最后一道工序
     */
    private String isLastProcess;

    /**
     * 良品数量
     */
    private BigDecimal passNum;

    /**
     * 不良品数量
     */
    private BigDecimal ngNum;

    /**
     * 维修数量
     */
    private BigDecimal repairNum;

    /**
     * 让步接收数量
     */
    private BigDecimal concessionNum;

    /**
     * 报废数量
     */
    private BigDecimal abandonedNum;

    /**
     * 结算数量
     */
    private BigDecimal settledNum;

    /**
     * 报工人
     */
    private String submitUser;

    /**
     * 报工日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date submitDay;

    /**
     * 报工状态（待审核、审核完毕、审核驳回）
     */
    private Long submitStatus;

    /**
     * 报工号
     */
    private String submitNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 数据状态(0初始,1修改)
     */
    private Long dataStatus;

    /**
     * 操作节点
     */
    private String operateNode;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;

    /**
     * 报工图片（流转）
     */
    private String submitPictures;

    /**
     * 是否为首序
     */
    private String isFirstProcess;

    // 是否已完工（0:是，1:否） 
    private String isComplete;
}

