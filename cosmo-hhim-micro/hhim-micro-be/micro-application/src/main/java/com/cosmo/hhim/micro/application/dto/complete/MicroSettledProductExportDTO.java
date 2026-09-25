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
 * 产品维度-已结算清单
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettledProductExportDTO implements Serializable {

    @Excel(name = "产品编码")
    private String productCode;
    @Excel(name = "产品名称")
    private String productName;
    /**
     * 工序数量
     */
    @Excel(name = "工序（道）")
    private Long processNum;
    /**
     * 员工数量
     */
    @Excel(name = "员工（人）")
    private Long employeeNum;
    /**
     * 完工数量
     */
    @Excel(name = "已结算数量", type = 10)
    private BigDecimal storageNum;
    @Excel(name = "产品单位")
    private String productUnit;
}
