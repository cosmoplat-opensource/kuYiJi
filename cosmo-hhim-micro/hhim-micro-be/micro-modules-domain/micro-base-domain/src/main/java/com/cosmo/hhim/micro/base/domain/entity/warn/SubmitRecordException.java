/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.warn;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工记录的异常
 * @date 2023/1/5 15:05
 */
@Data
public class SubmitRecordException {

    /**
     * 负库存风险标示 - 0：yes  1：no
     */
    private String negativeStockFlag;

    /**
     * 低良品率标示 - 0：yes 1:no
     */
    private String lowPassRateFlag;

    /**
     * 超产能标示 - 0: yes 1:no
     */
    private String overProductiveCapacityFlag;

    /**
     * 日均良品率
     */
    private BigDecimal avgPassRateByDay;

    /**
     * 日均产能
     */
    private BigDecimal avgProductionCapacityByDay;

    /**
     * 前工序名称
     */
    private String preProcessName;
}
