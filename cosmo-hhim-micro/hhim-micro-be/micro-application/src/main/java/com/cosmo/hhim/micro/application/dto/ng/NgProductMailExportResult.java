/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品清单邮件导出实体
 * @date 2023/4/12 10:16
 */
@Data
public class NgProductMailExportResult {

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
     * 工序编码
     */
    @Excel(name = "工序编码", sort = 3)
    private String processCode;

    /**
     * 工序名称
     */
    @Excel(name = "工序名称", sort = 4)
    private String processName;

    /**
     * 人员名称
     */
    @Excel(name = "报工人", sort = 5)
    private String submitNickName;

    /**
     * 不良品数量
     */
    @Excel(name = "不良品数量", sort = 6, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal totalNgNum;

    /**
     * 返修完成数量
     */
    @Excel(name = "返修完成数量", sort = 7, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal repairNum;

    /**
     * 让步接收数量
     */
    @Excel(name = "让步接收数量", sort = 8, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal concessionNum;

    /**
     * 报废数量
     */
    @Excel(name = "报废数量", sort = 9, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal abandonedNum;

    /**
     * 待处理数量
     */
    @Excel(name = "待处理数量", sort = 10, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal waitToDoNum;
}
