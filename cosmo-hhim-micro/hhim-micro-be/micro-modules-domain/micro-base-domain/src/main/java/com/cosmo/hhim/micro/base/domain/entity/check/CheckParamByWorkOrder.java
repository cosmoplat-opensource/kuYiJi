/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派 - 工单维度，接口入参
 * @date 2023/3/23 09:48
 */
@Data
public class CheckParamByWorkOrder {

    /**
     * 生产订单号， 工单编号， 产品编码， 名称
     */
    private String seqKey;

    /**
     * 审核状态
     */
    private Long submitStatus;
}
