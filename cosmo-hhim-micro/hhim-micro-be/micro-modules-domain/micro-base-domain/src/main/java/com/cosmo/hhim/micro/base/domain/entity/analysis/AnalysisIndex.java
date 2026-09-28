/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description:
 * @date 2022/11/2 11:15 上午
 */
@Data
public class AnalysisIndex {

    /**
     * 记工人数量
     */
    private Integer submitUserNum;

    /**
     * 记功产品数量
     */
    private Integer productNum;

    /**
     * 审产进度
     */
    private BigDecimal checkProgress;

    /**
     * 待审核数量(报工的数量)
     */
    private BigDecimal waitCheckNum;

    /**
     * 总的记工数量(报工的数量)
     */
    private BigDecimal totalNum;

    /**
     * 审产进度环比
     */
    private BigDecimal momCheckProgress;

    /**
     * 环比对比的"上期值"（报产总数）。前端据此决定是否显示百分比：
     * 上期=0 → 显示"上期无数据"；上期<10 → 标注"样本不足"。
     * （此前上期为 0 时会显示 +100%、上期很小时显示 +658%，用户会误以为系统出错）
     */
    private BigDecimal momBase;

    /**
     * 环比可用性：ok（正常）/ no_base（上期无数据）/ small_base（上期样本不足，<10）
     */
    private String momLevel;

    /**
     * 审产的数量
     */
    private BigDecimal checkNum;

    /**
     * 良品率
     */
    private BigDecimal passRate;

    /**
     * 良品数
     */
    private BigDecimal passNum;

    /**
     * 不良品数
     */
    private BigDecimal ngNum;

    /**
     * 良品率环比
     */
    private BigDecimal momPassRate;
}
