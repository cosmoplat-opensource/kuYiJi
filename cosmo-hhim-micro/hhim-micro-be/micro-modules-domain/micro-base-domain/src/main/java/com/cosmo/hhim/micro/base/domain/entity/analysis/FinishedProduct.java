/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.analysis;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 完工产品实体
 * @date 2022/10/25 1:55 下午
 */
@Data
public class FinishedProduct {

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 产品编码
     */
    @Excel(name = "产品编码", sort = 1)
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称", sort = 2)
    private String productName;

    /**
     * 良品数量
     */
    @Excel(name = "良品数", sort = 4, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal passNum;

    /**
     * 不良品数量
     */
    @Excel(name = "不良品数", sort = 5, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal ngNum;

    /**
     * 完工数量
     */
    @Excel(name = "完工总数", sort = 6, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal finishedNum;

    /**
     * 尾序工序序列码
     */
    private String processSeq;

    /**
     * 工序名称
     */
    @Excel(name = "工序", sort = 3)
    private String processName;

    /**
     * 单位
     */
    private String unit;

    /**
     * 统计时间范围
     */
    @Excel(name = "统计时间范围", width = 30, sort = 7)
    private String timeRange;
}
