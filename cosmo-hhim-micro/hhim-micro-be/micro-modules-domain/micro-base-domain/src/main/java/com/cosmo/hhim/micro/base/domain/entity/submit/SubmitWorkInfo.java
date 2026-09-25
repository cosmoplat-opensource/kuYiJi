/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitWorkInfo { 
    // 记工人 
    private String submitUser;

    // 记工总数 
    private BigDecimal workSubmitTotalNum;
}