/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportDetailInfo {

    // 产品编码 
    private String productSeq;

    // 产品名称 
    private String productName;

    // 完工数量 
    private BigDecimal completeNum;

    // 报工人昵称 
    private String submitWorkNickName;

    // 报工时间 
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitWorkTime;
}
