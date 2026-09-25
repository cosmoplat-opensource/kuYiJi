/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettlementModifyDTO implements Serializable {
    /**
     * 员工ID
     */
    @NotNull(message = "无法获取员工信息")
    private Long employeeId;
    /**
     * 调整前数量
     */
    @DecimalMin(value = "0", message = "无法获取调整前数量")
    @NotNull(message = "无法获取调整前数量")
    private BigDecimal oldNum;
    /**
     * 调整后
     */
    @DecimalMin(value = "0", message = "无法获取调整后数量")
    @NotNull(message = "无法获取调整后数量")
    private BigDecimal adjustedNum;
    /**
     * 产品序列码
     */
    @NotNull(message = "无法获取产品信息")
    private String productSeq;
    @NotNull(message = "无法获取产品信息")
    private String productCode;
    @NotNull(message = "无法获取产品信息")
    private String productName;
    /**
     * 工序序列码
     */
    @NotNull(message = "无法获取工序信息")
    private String operateProcessSeq;
    @NotNull(message = "无法获取工序信息")
    private String operateProcessCode;
    @NotNull(message = "无法获取工序信息")
    private String operateProcessName;
    private String remark;
}
