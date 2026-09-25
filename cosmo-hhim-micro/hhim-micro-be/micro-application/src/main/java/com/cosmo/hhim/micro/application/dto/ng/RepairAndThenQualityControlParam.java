/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 返修复核接口入参
 * @date 2023/4/6 10:50
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepairAndThenQualityControlParam {

    /* ************* *
     * 用于查询报工记录
     * ************* */
    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 工序序列码
     */
    private String processSeq;

    /**
     * 报工人
     */
    private Long submitUser;

    /** *********
     *   前端传递
     * *********/

    /**
     * 报工记录id汇总
     */
    private String ids;

    /**
     * 维修人id
     */
    private Long repairUser;

    /**
     * 返修完成数量
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
     * 报废类型
     */
    private String abandonedType;

    /**
     * 备注
     */
    private String remark;
}
