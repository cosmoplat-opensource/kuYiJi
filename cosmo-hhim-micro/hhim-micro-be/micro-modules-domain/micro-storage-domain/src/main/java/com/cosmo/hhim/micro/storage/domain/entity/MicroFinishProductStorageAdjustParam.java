/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroFinishProductStorageAdjustParam {

    // 产品唯一码 
    @NotEmpty(message = "产品唯一码不允许为空！")
    private String productSeq;

    // 产品名称 
    private String productName;

    // 变动类型（10:完工入库，20:完工撤销，30:库存变动） 
    private String changeType;

    // 变动原因 
    private String changeReason;

    // 变更数量 
    @NotNull(message = "变更数量不允许为空！")
    @Digits(integer = 12, fraction = 4, message = "整数位不能超过{integer}位，小数位不能超过{fraction}位！")
    private BigDecimal changeNum;

}
