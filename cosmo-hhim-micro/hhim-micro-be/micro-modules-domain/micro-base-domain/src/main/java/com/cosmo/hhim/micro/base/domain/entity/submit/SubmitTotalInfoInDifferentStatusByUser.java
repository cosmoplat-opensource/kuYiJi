/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不同状态下到人的报工记录信息
 * @date 2023/6/9 10:43
 */
@Data
public class SubmitTotalInfoInDifferentStatusByUser {

    /**
     * 报工记录数量
     */
    private int recordNum = 0;

    /**
     * 数量
     */
    private BigDecimal totalNum = BigDecimal.ZERO;
}
