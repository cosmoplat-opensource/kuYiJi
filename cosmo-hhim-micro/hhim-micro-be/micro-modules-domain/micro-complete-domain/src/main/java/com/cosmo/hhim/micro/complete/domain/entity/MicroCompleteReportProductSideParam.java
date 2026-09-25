/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.entity;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Data
public class MicroCompleteReportProductSideParam extends MicroCompleteReportBasicParam {

    // 产品编码或名称 
    private String productCodeOrName;

}
