/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.storage;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 用于接受从库存表里面获得的结果Map
 * @date 2022/12/27 11:09
 */
@Data
public class StorageForProductAndProcess {

    private String seqKey;

    private BigDecimal passNum;

    private BigDecimal ngNum;
}
