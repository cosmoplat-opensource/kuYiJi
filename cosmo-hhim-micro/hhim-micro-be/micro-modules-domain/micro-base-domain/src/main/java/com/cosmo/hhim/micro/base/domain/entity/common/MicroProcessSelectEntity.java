/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 选择实体
 *
 * @author cosmo-hhim-open Team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroProcessSelectEntity extends MicroSelectEntity {
    /**
     * 未审核数量(默认为0)
     */
    private BigDecimal unapprovedNum = BigDecimal.valueOf(0.0000);
    /**
     * 总库存数量(默认为0)
     */
    private BigDecimal totalStockNum = BigDecimal.valueOf(0.0000);
}
