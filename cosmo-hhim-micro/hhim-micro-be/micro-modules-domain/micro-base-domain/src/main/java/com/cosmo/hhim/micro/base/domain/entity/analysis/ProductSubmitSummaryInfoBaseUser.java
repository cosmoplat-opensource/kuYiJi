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
 * @description: 员工维度下 某个产品的报工情况
 * @date 2023/6/5 15:29
 */
@Data
public class ProductSubmitSummaryInfoBaseUser {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    /**
     * 记工数量
     */
    private BigDecimal totalNum;

    /**
     * 产品下面工序的报工数据
     */
    List<ProcessSummaryInfoByProductBaseUser> processSummaryInfoByProductBaseUserList;
}
