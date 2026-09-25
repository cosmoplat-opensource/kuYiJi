/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检操作时具体的不良类型信息
 * @date 2023/4/3 15:15
 */
@Data
public class NgProductDetailInfo {

    /**
     * 不良类型
     */
    private String ngType;

    /**
     * 不良数量
     */
    private BigDecimal ngNum;
}
