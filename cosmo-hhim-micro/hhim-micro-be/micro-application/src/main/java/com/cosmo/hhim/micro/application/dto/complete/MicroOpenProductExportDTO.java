/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroOpenProductExportDTO implements Serializable {

    /**
     * 产品名称
     */
    @Excel(name = "产品名称")
    private String productName;

    /**
     * 产品编码
     */
    @Excel(name = "产品编码")
    private String productCode;


    /**
     * 完工数量
     */
    @Excel(name = "完工数量", type = 10)
    private BigDecimal completeTotalNum;
    /**
     * 产品单位
     */
    @Excel(name = "产品单位")
    private String productUnit;

}
