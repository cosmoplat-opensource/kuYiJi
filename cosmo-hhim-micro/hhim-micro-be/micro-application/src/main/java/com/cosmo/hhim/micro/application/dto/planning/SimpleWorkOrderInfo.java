/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 工单信息的简要实体
 * @date 2023/6/9 17:40
 */
@Data
public class SimpleWorkOrderInfo {

    private String workOrderNo;

    private String productCode;

    private String productName;

    private String productSeq;

    private BigDecimal planNum;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
}
