/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.export;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 导出的质量趋势信息类
 * @date 2023/1/9 15:20
 */
@Data
public class QualityTrendsInfo {

    /**
     * 产品编码
     */
    @Excel(name = "产品编码")
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称")
    private String productName;

    /**
     * 工序名称
     */
    @Excel(name = "工序")
    private String operateProcessName;

    /**
     * 良品数
     */
    @Excel(name = "良品数", cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal passNum;

    /**
     * 不良品数
     */
    @Excel(name = "不良品数", cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal ngNum;

    /**
     * 良品率, 用于计算
     */
    private BigDecimal passRate;

    /**
     * 良品率字符串, 用于展示
     */
    @Excel(name = "良品率")
    private String passRateString;

    /**
     * 统计时间范围
     */
    @Excel(name = "统计时间范围", width = 30)
    private String timeRange;
}
