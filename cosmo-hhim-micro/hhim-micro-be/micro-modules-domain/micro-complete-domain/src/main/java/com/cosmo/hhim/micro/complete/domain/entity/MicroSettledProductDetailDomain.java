/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 产品维度-详情
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroSettledProductDetailDomain implements Serializable {
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    private String operateProcessName;
    private String operateProcessSeq;
    private String operateProcessCode;
    private List<MicroSettlementReportEmployeeDomain> detailList;

}
