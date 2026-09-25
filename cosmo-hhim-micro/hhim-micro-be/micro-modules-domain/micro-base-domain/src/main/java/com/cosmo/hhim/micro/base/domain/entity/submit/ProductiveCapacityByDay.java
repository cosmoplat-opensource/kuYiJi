/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author cosmo-hhim-open Team
 * @description: 到天到人的日产能
 * @date 2022/12/19 09:32
 */
@Data
public class ProductiveCapacityByDay {

    /**
     * 报工人
     */
    private String submitUser;

    /**
     * 日良品数量
     */
    private BigDecimal totalPassNum;

    /**
     * 日不良品数量
     */
    private BigDecimal totalNgNum;

    /**
     * 日产能
     */
    private BigDecimal totalNum;

    /**
     * 报工日
     */
    private LocalDate submitDay;
}
