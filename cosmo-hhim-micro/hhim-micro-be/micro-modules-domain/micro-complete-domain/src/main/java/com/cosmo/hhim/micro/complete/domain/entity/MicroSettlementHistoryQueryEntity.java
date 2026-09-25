/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 计件结算调整历史对象 micro_settlement_history
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Data
public class MicroSettlementHistoryQueryEntity implements Serializable {
    private Long employeeId;
    private String employeeName;
    private String employeeUserName;
    List<MicroSettlementHistoryQuerySubEntity> detailList;
}
