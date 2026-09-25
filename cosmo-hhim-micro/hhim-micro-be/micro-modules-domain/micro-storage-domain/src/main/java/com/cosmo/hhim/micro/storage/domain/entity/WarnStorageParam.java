/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.entity;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 预警库存查询参数
 * @date 2023/5/6 11:16
 */
@Data
public class WarnStorageParam {

    private String productNameOrCode;

    /**
     * 预警标示
     *
     * 1 - 查询负库存产品
     * 2 - 低于安全库存下限的产品
     * 3 - 高于安全库存上限的产品
     */
    private String stockWarnFlag;
}
