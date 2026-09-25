/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 员工维度
 * @date 2023/4/5 10:35
 */
@Data
public class NgProductAndProcessByUserFromAlreadyCheck {

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private String processSeq;

    private String processName;

    private Long submitUser;

    private BigDecimal ngNum;
}
