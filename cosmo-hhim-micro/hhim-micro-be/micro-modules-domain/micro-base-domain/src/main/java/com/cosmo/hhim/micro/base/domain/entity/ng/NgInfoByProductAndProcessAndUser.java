/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品清单导出信息（全量），到产品到工序到人
 * @date 2023/4/12 11:07
 */
@Data
public class NgInfoByProductAndProcessAndUser {

    private String productSeq;

    private String productCode;

    private String productName;

    private String processSeq;

    private String processCode;

    private String processName;

    private Long submitUser;

    private String submitNickName;

    private BigDecimal ngNum;
}
