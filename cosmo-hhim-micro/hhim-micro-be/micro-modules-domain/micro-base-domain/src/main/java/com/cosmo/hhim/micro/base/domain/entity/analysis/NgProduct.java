/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品对象 - 只有不良品数
 * @date 2023/2/13 13:51
 */
@Data
public class NgProduct {

    private String productSeq;

    private String productName;

    private String productCode;

    private String productUnit;

    private BigDecimal totalNgNum;

    private List<NgProductAndProcess> ngProductAndProcessList;
}
