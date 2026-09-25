/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.mail;

import com.cosmo.hhim.common.core.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 库存变动导出实体
 * @date 2023/2/21 09:43
 */
@Data
public class StockChangeHistoryRecord {

    /**
     * 操作时间
     */
    @Excel(name = "操作时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", sort = 1)
    private Date createdDate;

    /**
     * 产品编码
     */
    @Excel(name = "产品编码", sort = 2)
    private String productCode;

    /**
     * 产品名称
     */
    @Excel(name = "产品名称", sort = 3)
    private String productName;

    /**
     * 工序编码
     */
    @Excel(name = "工序编码", sort = 4)
    private String processCode;

    /**
     * 工序名称
     */
    @Excel(name = "工序名称", sort = 5)
    private String processName;

    /**
     * 变动类型
     */
    @Excel(name = "变动类型", sort = 6)
    private String operateNode;

    /**
     * 操作人
     */
    @Excel(name = "操作人", sort = 7)
    private String operatorName;

    /**
     * 良品变动数量
     */
    @Excel(name = "良品变动数量", sort = 8, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal changePassNum;

    /**
     * 变动后良品数量
     */
    @Excel(name = "变动后良品数量", sort = 9, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal passNum;

    /**
     * 不良品变动数量
     */
    @Excel(name = "不良品变动数量", sort = 10, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal changeNgNUm; 

    /**
     * 变动后不良品数量
     */
    @Excel(name = "变动后不良品数量", sort = 11, cellType = Excel.ColumnType.NUMERIC)
    private BigDecimal ngNum;

    /**
     * 备注
     */
    @Excel(name = "备注", sort = 12)
    private String remark;
}
