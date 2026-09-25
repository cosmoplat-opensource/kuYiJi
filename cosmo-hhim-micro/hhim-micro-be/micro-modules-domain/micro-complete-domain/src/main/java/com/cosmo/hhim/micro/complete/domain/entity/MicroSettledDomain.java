/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 生成结算实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettledDomain implements Serializable {
    /**
     * 产品序列码
     */
    private String productSeq;
    /**
     * 完工产品数量
     */
    private BigDecimal productNum;

    /**
     * 完工报告的单号数组字符串,逗号隔开
     */
    private String completeReportNoArrayStr;
}
