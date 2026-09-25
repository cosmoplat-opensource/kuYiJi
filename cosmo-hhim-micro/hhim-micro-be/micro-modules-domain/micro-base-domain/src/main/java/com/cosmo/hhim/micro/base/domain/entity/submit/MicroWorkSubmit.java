/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.cosmo.hhim.micro.infrastructure.annotation.ChangeField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 报工记录对象 micro_work_submit
 *
 * @author cosmo-hhim-open Team
 * @date 2022-10-12
 */
@Data
public class MicroWorkSubmit implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 租户编码
     */
    private String tenantCode;

    /**
     * 生产订单号
     */
    private String orderNo;

    /**
     * 工单号
     */
    private String workOrderNo;

    /**
     * 任务单号
     */
    private String taskNo;

    /**
     * 报工方式
     *
     * 1 - 普通报工， 2 - 工单报工
     */
    private Long submitType;

    /**
     * 产品唯一码
     */
    @ChangeField
    private String productSeq;

    /**
     * 前工序唯一码
     */
    @ChangeField
    private String preProcessSeq;

    /**
     * 操作工序唯一码
     */
    @ChangeField
    private String operateProcessSeq;

    /**
     * 前工序分组
     */
    private String preProcessGroup;

    /**
     * 操作工序分组
     */
    private String operateProcessGroup;

    /**
     * 是否最后一道工序（0:是，1:否）
     */
    @ChangeField
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
     * 报工人
     */
    private String submitUser;

    /**
     * 报工日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date submitDay;

    /**
     * 报工状态（待审核、审核完毕、审核驳回）
     */
    private Long submitStatus;

    /**
     * 报工号(每次生成一个)
     */
    private String submitNo;

    /**
     * 备注
     */
    @ChangeField
    @Excel(name = "备注", sort = 10)
    private String remark;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "报工时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 9)
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    /**
     * 审产人
     */
    private String checkUser;

    /**
     * 审产时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkDate;

    /**
     * 良品审产数
     */
    @ChangeField
    @Excel(name = "良品数", sort = 6)
    private BigDecimal checkPassNum;

    /**
     * 不良品审产数
     */
    @ChangeField
    @Excel(name = "不良品数", sort = 7)
    private BigDecimal checkNgNum;

    /**
     * 最后修改人
     */
    private String lastUpdBy;

    /**
     * 最后修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastUpdDate;

    /**
     * 数据状态(0初始,1修改), 单据是否变更
     */
    private Long dataStatus;

    /**
     * 补录标示(0是, 1否)
     */
    private Long expiredRecordFlag;

    /**
     * 报工图片url
     * 使用分号进行分割
     */
    @ChangeField
    private String submitPictures;

    /**
     * 是否为首序
     */
    @ChangeField
    private String isFirstProcess;

    /**
     * 报工总数（良品 + 不良品）
     */
    private BigDecimal totalNum;

    /**
     * 是否已完工（0:是，1:否）
     */
    private String isComplete;

    /**
     * 是否送检
     *
     * 0 - 是, 1 - 否
     */
    private Long checkStatus;

    /**
     * 返修完成数量
     */
    private BigDecimal repairNum;

    /**
     * 报废数量
     */
    private BigDecimal abandonedNum;

    /**
     * 质检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date qcDate;

    /**
     * 质检人
     */
    private String qcUser;

    /** **********************
     *       冗余字段
     * ***********************/

    @Excel(name = "产品编码", sort = 2)
    private String productCode;

    @Excel(name = "产品名称", sort = 3)
    private String productName;

    private String preProcessCode;

    private String preProcessName;

    @Excel(name = "工序名称", sort = 4)
    private String operateProcessName;

    @Excel(name = "工序编码", sort = 5)
    private String operateProcessCode;

    /**
     * 提报人中文名称
     */
    @Excel(name = "记工人", sort = 1)
    private String submitNickName;

    /**
     * 审产人中文名称
     */
    @Excel(name = "审产人", sort = 8)
    private String checkNickName;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 产品id
     */
    private Long productId;

    /** **********************/
}
