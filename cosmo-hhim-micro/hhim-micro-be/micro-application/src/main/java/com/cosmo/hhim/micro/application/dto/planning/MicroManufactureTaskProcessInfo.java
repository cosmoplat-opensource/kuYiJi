/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.planning;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 生产任务进度
 * @date 2023/5/8 15:23
 */
@Data
public class MicroManufactureTaskProcessInfo {

    private String processName;

    /**
     * 未审核良品数
     */
    private BigDecimal passNum;

    private BigDecimal ngNum;

    /**
     * 已审核良品数
     */
    private BigDecimal checkPassNum;

    private BigDecimal checkNgNum;

    /** 生产完成进度 已产/计划*/
    private BigDecimal completionRate;

    /**
     * 工序任务计划数量
     */
    private BigDecimal taskPlanNum;

    /**
     * 首序标示
     */
    private String isFirstProcessFlag;

    /**
     * 尾序标示
     */
    private String isLastProcessFlag;
}
