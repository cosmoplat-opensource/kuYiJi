/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 */
public class MicroWorkOrderSubmitDTO implements Serializable { 
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 产品唯一码
     */
    private String productSeq;

    /**
     * 操作工序唯一码
     */
    private String operateProcessSeq;

    /**
     * 良品数量
     */
    private BigDecimal passNum;

    /**
     * 不良品数量
     */
    private BigDecimal ngNum;

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
     * 备注
     */
    private String remark;

    /**
     * 审产人
     */
    private String checkUser;

    /**
     * 审产时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkDate;

    /**
     * 良品审产数
     */
    private BigDecimal checkPassNum;

    /**
     * 不良品审产数
     */
    private BigDecimal checkNgNum;

    /**
     * 报工图片url
     * 使用分号进行分割
     */
    private String submitPictures;

    /**
     * 工单号
     */
    private String workOrderNo;
    /**
     * 任务单号
     */
    private String taskNo;
    /**
     * 报工类型10工序报工20工单报工
     */
    private String submitType;
}
