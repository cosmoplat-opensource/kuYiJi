/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description: 工易派 - 按照工单维度展示报工记录信息
 * @date 2023/3/23 09:42
 */
@Data
public class SubmitRecordInfoByWorkOrder {

    /**
     * 工单号
     */
    private String workOrderNo;
    
    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    /**
     * 记工数量
     */
    private int recordNum;

    /**
     * 工序数量
     */
    private int processNum;

    /**
     * 报工记录id汇总
     */
    private String ids;
}
