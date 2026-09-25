/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 待审核或者驳回列表
 * @date 2023/6/9 11:17
 */
@Data
public class SubmitInfoInDifferentStatusByUser {

    private Long id;

    private String processCode;

    private String processName;

    private String processSeq;

    private String productCode;

    private String productName;

    private String productSeq;

    private Long submitStatus;

    private BigDecimal passNum;

    private BigDecimal ngNum;

    private BigDecimal totalNum;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
}
