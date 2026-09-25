/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.storage;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/4
 */
@Data
public class MicroPlanFinishProductStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 物料编码
     */
    @Excel(name = "物料编码", orderNum = "1")
    private String productCode;

    /**
     * 物料名称
     */
    @Excel(name = "物料名称", orderNum = "2")
    private String productName;

    /**
     * 物料类别
     */
    @Excel(name = "类别", orderNum = "3", replace = {"成品_CP", "半成品_BCP", "原材料_YCL"})
    private String productType;

    /**
     * 安全库存下限
     */
    @Excel(name = "库存下限", orderNum = "4", groupName = "安全库存范围")
    private BigDecimal stockLowerLimit;

    /**
     * 安全库存上限
     */
    @Excel(name = "库存上限", orderNum = "5", groupName = "安全库存范围")
    private BigDecimal stockUpperLimit;

    /**
     * 单位
     */
    @Excel(name = "单位", orderNum = "6")
    private String unit;

    /**
     * 规格
     */
    @Excel(name = "规格", orderNum = "7")
    private String standards;

    /**
     * 当前库存
     */
    @Excel(name = "当前库存", orderNum = "8")
    private BigDecimal num;

}
