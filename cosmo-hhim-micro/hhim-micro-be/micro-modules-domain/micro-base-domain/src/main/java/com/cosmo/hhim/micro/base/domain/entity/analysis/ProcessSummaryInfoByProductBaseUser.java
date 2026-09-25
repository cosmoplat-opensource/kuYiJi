/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description:  员工维度下 - 某个产品的工序报工数据
 * @date 2023/6/5 15:31
 */
@Data
public class ProcessSummaryInfoByProductBaseUser {

    private String processSeq;

    private String processCode;

    private String processName;

    private BigDecimal passNum;

    private BigDecimal ngNum;
}
