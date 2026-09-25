/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 工序流转的信息
 * @date 2023/3/7 10:25
 */
@Data
public class ProcessFlow {

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 流转数量
     */
    private BigDecimal flowNum;
}
