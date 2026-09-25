/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品 + 工序在报工人维度实体
 * @date 2023/4/5 09:07
 */
@Data
public class NgProductAndProcessAndUserByProduct {

    private String productSeq;

    private String productCode;

    private String productName;

    private String processSeq;

    private String processName;

    private Long submitUser;

    private String submitNickName;

    private BigDecimal ngNum;
}
