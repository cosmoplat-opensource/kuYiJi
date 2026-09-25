/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.ng;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/4/12 09:18
 */
@Data
public class NgNumFromProductAndUser {

    /**
     * 不良品涉及的人数
     */
    private int userNum;

    /**
     * 不良品涉及的待处理数量
     */
    private BigDecimal totalNgNum;
}
