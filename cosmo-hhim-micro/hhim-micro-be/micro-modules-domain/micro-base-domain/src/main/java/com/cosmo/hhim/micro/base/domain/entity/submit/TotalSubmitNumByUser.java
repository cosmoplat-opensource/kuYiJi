/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 记录到员工（产品、工序、报工日）的报工总数信息
 * @date 2023/3/2 15:40
 */
@Data
public class TotalSubmitNumByUser {

    private BigDecimal totalSubmitNum;

    /**
     * 报工记录id汇总，用于记录条数
     */
    private String ids;
}
