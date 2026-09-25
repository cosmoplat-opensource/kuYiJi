/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import com.cosmo.hhim.common.core.exception.CustomException;
import lombok.Data;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroFinishProductStorageBoundParam {

    // 产品唯一码 
    @NotEmpty(message = "产品唯一码不允许为空！")
    private String productSeq;

    // 出入库数量 
    @NotNull(message = "数量不允许为空！")
    @Digits(integer = 12, fraction = 4, message = "整数位不能超过{integer}位，小数位不能超过{fraction}位！")
    private BigDecimal changeNum;


    /**
     * 参数合法性校验
     */
    public void checkParam() {
        if (null != changeNum && changeNum.signum() < 0) {
            throw new CustomException("数量不允许为负数！");
        }
    }
}
