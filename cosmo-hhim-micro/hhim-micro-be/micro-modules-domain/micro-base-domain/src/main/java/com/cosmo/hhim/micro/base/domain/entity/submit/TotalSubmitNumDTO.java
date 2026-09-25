/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 存储报工记录的数量
 * @date 2023/5/11 10:28
 */
@Data
public class TotalSubmitNumDTO {

    /**
     * 待审核数量
     */
    private BigDecimal waitCheckNum;

    /**
     * 审核数量
     */
    private BigDecimal checkNum;

    /**
     * 工序任务的计划数量
     */
    private BigDecimal taskPlanNum;
}
