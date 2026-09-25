/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportGroupResult {

    // 完工单号 
    private String reportNo;

    // 完工时间 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date completeTime;

    // 产品款数 
    private Long productCategoryTotalNum;

    // 产品总数 
    private BigDecimal productTotalNum;

    // 完工单来源渠道 
    private String sourceChannel;

    // 完工操作人ID 
    private Long completeUser;

    // 完工操作人昵称 
    private String completeUserNickName;

}
