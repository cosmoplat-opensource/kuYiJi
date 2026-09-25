/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.integration.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/6
 */
@Data
public class WeekReportMessageContent extends NotifyMessageContent {

    // 产品款数 
    private Long productModelNum;

    // 已完工数量 
    private BigDecimal completeFinishedNum;

    // 待完工数量 
    private BigDecimal completeWaitNum;

    // 良品率 
    private String goodRatio;

    // 记工人数 
    private Long submitUserNum;

    // 记工总数 
    private BigDecimal submitTotalNum;

    // 未审核记工总数 
    private BigDecimal waitCheckTotalNum;

    // 审核及时率 
    private String timelyRatio;

}
