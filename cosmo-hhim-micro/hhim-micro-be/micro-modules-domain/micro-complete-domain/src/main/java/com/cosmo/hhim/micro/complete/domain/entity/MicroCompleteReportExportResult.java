/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
public class MicroCompleteReportExportResult {

    // 产品名称 
    @Excel(name = "产品名称", sort = 1)
    private String productName;

    // 产品编码 
    @Excel(name = "产品编码", sort = 2)
    private String productSeq;

    // 完工数量 
    @Excel(name = "完工数量", sort = 3)
    private BigDecimal completeTotalNum;

    // 完工时间 
    @Excel(name = "完工日期", sort = 4, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    // 完工操作人昵称 
    @Excel(name = "操作人", sort = 5)
    private String completeUserNickName;

    // 完工操作人ID 
    private Long completeUser;

    // 完工单号 
    @Excel(name = "完工单号", sort = 6)
    private String reportNo;


}
