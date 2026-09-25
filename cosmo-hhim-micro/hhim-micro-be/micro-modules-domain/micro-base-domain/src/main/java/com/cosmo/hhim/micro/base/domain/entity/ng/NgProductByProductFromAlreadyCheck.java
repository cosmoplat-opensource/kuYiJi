/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品清单展示列表实体
 * @date 2023/4/4 16:10
 */
@Data
public class NgProductByProductFromAlreadyCheck {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private BigDecimal totalNgNum;

    private List<NgProductAndProcessByProductFromAlreadyCheck> ngProductAndProcessCheckList;
}
