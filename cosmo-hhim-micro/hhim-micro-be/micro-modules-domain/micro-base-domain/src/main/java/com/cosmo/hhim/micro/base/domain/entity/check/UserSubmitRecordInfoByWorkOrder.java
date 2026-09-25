/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 工单维度到工序到人的记录信息
 * @date 2023/3/23 13:11
 */
@Data
public class UserSubmitRecordInfoByWorkOrder {

    private String userId;

    private String nickName;

    private BigDecimal passNum;

    private BigDecimal ngNum;

    /**
     * 报工记录id汇总, 用于获取到工序层面的报工记录条数
     */
    private String ids;
}
