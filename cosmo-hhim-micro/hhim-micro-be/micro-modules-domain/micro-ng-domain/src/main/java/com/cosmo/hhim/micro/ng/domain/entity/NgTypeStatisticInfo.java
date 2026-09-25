/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良类型统计分析
 * @date 2023/4/11 17:34
 */
@Data
public class NgTypeStatisticInfo {

    private String ngType;

    private BigDecimal ngNum;
}
