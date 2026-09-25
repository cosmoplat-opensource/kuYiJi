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
 * @description: 良品 只有良品数
 * @date 2023/2/21 13:30
 */
@Data
public class PassProduct {

    private String productSeq;

    private String productName;

    private String productCode;

    private String productUnit;

    /**
     * 总的良品数量
     */
    private BigDecimal totalPassNum;

    /**
     * 到工序维度的各数量情况
     */
    private List<PassProductAndProcess> passProductAndProcessList;
}
