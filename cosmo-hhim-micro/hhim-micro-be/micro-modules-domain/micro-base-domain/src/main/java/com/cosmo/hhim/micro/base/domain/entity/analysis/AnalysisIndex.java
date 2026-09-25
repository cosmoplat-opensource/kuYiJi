/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description:
 * @date 2022/11/2 11:15 上午
 */
@Data
public class AnalysisIndex {

    /**
     * 记工人数量
     */
    private Integer submitUserNum;

    /**
     * 记功产品数量
     */
    private Integer productNum;

    /**
     * 审产进度
     */
    private BigDecimal checkProgress;

    /**
     * 待审核数量(报工的数量)
     */
    private BigDecimal waitCheckNum;

    /**
     * 总的记工数量(报工的数量)
     */
    private BigDecimal totalNum;

    /**
     * 审产进度环比
     */
    private BigDecimal momCheckProgress;

    /**
     * 审产的数量
     */
    private BigDecimal checkNum;

    /**
     * 良品率
     */
    private BigDecimal passRate;

    /**
     * 良品数
     */
    private BigDecimal passNum;

    /**
     * 不良品数
     */
    private BigDecimal ngNum;

    /**
     * 良品率环比
     */
    private BigDecimal momPassRate;
}
