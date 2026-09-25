/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 到产品、工序、人的返修数量信息
 * @date 2023/4/12 11:30
 */
@Data
public class RepairNumInfoByProductAndProcessAndUser {

    private String productSeq;

    private String processSeq;

    private String submitUser;

    private BigDecimal repairNum;

    private BigDecimal concessionNum;

    private BigDecimal abandonedNum;
}
