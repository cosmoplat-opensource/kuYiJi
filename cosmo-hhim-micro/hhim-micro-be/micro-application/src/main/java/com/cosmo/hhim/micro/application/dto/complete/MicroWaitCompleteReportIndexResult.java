/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.complete;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/27
 */
@Data
public class MicroWaitCompleteReportIndexResult {

    // 待处理报工记录数 
    private Long waitDealSubmitTotalNum;

    // 产品款数 
    private Long productCategoryNum;

}
