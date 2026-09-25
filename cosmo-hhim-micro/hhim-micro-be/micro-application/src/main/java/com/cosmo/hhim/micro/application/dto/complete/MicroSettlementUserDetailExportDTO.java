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
public class MicroSettlementUserDetailExportDTO implements Serializable {

    @Excel(name = "员工名")
    private String employeeName;
    @Excel(name = "员工用户名")
    private String employeeUserName;
    @Excel(name = "产品编码")
    private String productCode;
    @Excel(name = "产品名称")
    private String productName;
    @Excel(name = "工序编码")
    private String operateProcessCode;
    @Excel(name = "工序名称")
    private String operateProcessName;
    @Excel(name = "数量", type = 10)
    private BigDecimal adjustedNum;
    @Excel(name = "产品单位")
    private String productUnit;
    private String remark;
}
