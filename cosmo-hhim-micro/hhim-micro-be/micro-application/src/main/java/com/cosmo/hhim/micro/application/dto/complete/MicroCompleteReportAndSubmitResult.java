/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import com.cosmo.hhim.common.core.annotation.Excel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/4/23
 */
@Data
public class MicroCompleteReportAndSubmitResult {

    // 报工人 
    @Excel(name = "姓名", sort = 1)
    private String submitUserNickName;

    // 产品编码 
    @Excel(name = "产品编码", sort = 2)
    private String productCode;

    // 产品名称 
    @Excel(name = "产品名称", sort = 3)
    private String productName;

    // 完工数量 
    @Excel(name = "完工数量", sort = 4)
    private BigDecimal completeNum;

    // 完工备注 
    @Excel(name = "备注", sort = 5)
    private String remark;

    // 审核人 
    @Excel(name = "审核人", sort = 6)
    private String checkUserNickName;

    // 审核时间 
    @Excel(name = "审核时间", sort = 7, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkDate;

    // 质检状态(0=未送检,1=待质检,2=质检完成) 
    @Excel(name = "质检状态", sort = 8, readConverterExp = "0=未送检,1=待质检,2=质检完成")
    private Long checkStatus;

    // 质检人 
    @Excel(name = "质检人", sort = 9)
    private String qcUserNickName;

    // 质检时间 
    @Excel(name = "质检时间", sort = 10, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date qcDate;

    // 完工操作人 
    @Excel(name = "完工操作人", sort = 11)
    private String completeUserNickName;

    // 完工时间 
    @Excel(name = "操作时间", sort = 12, dateFormat = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

}
