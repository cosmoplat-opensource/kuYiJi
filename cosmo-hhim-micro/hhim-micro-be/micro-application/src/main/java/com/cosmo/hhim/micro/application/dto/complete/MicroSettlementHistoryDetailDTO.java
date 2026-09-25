/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 计件结算调整历史对象 micro_settlement_history
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
public class MicroSettlementHistoryDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String employeeName;
    private String employeeUserName;

    /**
     * 调整后可结数量
     */
    private BigDecimal adjustedNum;

    /**
     * 产品名称
     */
    private String productName;
    private String productCode;
    private String productUnit;

    /**
     * 操作工序名称
     */
    private String operateProcessName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人
     */
    private String createdBy;
    private String createdByName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdDate;
}
