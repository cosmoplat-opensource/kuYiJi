/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.enums;

import com.cosmo.hhim.micro.infrastructure.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author cosmo-hhim-open Team
 */
@Getter
@AllArgsConstructor
public enum MicroSettlementStatusEnum implements BaseEnum<String> {
    /**
     * 未结算10
     */
    OPEN("10", "未结算"),

    /**
     * 已结算20
     */
    SETTLED("20", "已结算");

    private String code;
    private String desc;
}
