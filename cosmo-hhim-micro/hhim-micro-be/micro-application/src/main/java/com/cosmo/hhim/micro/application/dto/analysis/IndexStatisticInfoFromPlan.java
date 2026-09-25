/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派首页信息
 * @date 2023/5/4 14:25
 */
@Data
public class IndexStatisticInfoFromPlan {

    /**
     * 审核数量
     */
    private BigDecimal checkNum = BigDecimal.ZERO;

    /**
     * 未审核数量
     */
    private BigDecimal waitCheckNum = BigDecimal.ZERO;

    /**
     * 报工总数
     */
    private BigDecimal totalSubmitNum = BigDecimal.ZERO;

    /**
     * 入库数量
     */
    private BigDecimal inboundNum = BigDecimal.ZERO;

    /**
     * 涉及产品数量
     */
    private Long inBoundProductCategoryNum = 0L;
}
