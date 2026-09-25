/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MicroSettlementReportDomain extends MicroSettlementReportEmployeeDomain {
    List<MicroSettlementDetailDomain> detailList;

}
