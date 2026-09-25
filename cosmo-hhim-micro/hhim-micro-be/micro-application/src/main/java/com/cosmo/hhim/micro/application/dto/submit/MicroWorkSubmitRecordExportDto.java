/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工记录导出的实体对象
 * @date 2023/4/17 11:19
 */
@Data
public class MicroWorkSubmitRecordExportDto {

    /**
     * 报工人
     */
    @Excel(name = "姓名")
    private String submitNickName;

    @Excel(name = "产品编码")
    private String productCode;

    @Excel(name = "产品名称")
    private String productName;

    @Excel(name = "工序编码")
    private String processCode;

    @Excel(name = "工序名称")
    private String processName;

    /**
     * 良品数
     *
     * 如果没有审核或者质检，就是报工的数量
     * 否则，就是审核或者质检后的数量
     */
    @Excel(name = "良品数", cellType = Excel.ColumnType.NUMERIC)
    private String passNum;

    /**
     * 不良品数
     *
     * 如果没有审核或者质检，就是报工的数量
     * 否则，就是审核或者质检后的数量
     */
    @Excel(name = "不良品数", cellType = Excel.ColumnType.NUMERIC)
    private String ngNum;

    @Excel(name = "备注")
    private String remark;

    /**
     * 报工时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "报工时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitDate;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private String submitStatus;

    /**
     * 审核人
     */
    @Excel(name = "审核人")
    private String checkNickName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date checkDate;

    /**
     * 质检状态
     */
    @Excel(name = "质检状态")
    private String checkStatus;

    /**
     * 质检人
     */
    @Excel(name = "质检人")
    private String qcNickName;

    /**
     * 质检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "质检时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date qcDate;

    /**
     * 返修数量
     */
    @Excel(name = "返修数量", cellType = Excel.ColumnType.NUMERIC)
    private String repairNum;

    /**
     * 结算的数量
     */
    @Excel(name = "结算数量", cellType = Excel.ColumnType.NUMERIC)
    private String settledNum;

    /**
     * 结算日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "结算日期", width = 30, dateFormat = "yyyy-MM-dd")
    private Date settledDate;
}
