/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 返修列表展示信息实体
 * @date 2023/4/7 14:58
 */
@Data
public class RepairRecordInfo {

    private String repairNo;

    private String productCode;

    private String productName;

    private String processName;

    /**
     * 维修数量
     */
    private BigDecimal repairNum;

    /**
     * 让步接收数量
     */
    private BigDecimal concessionNum;

    /**
     * 报废数量
     */
    private BigDecimal abandonedNum;

    /**
     * 返修人
     */
    private String repairNickName;

    /**
     * 复核人
     */
    private String reviewNickName;

    /**
     * 返修时间
     */
    private Date reviewDate;
}
