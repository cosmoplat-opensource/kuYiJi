/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.entity.work;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/4
 */
@Data
public class MicroManufactureWorkOrderParam extends MicroManufactureWorkOrder {

    // 订单或工单号 
    private String orderOrWorkNo;

}
