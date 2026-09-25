/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import com.cosmo.hhim.micro.ng.domain.entity.QualityControlInfoWhenRepair;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 返修复核页面的详细信息
 * @date 2023/4/6 13:35
 */
@Data
public class DetailQualityControlInfoWhenRepair {

    private String submitNickName;

    private String productName;

    private String productCode;

    private String productUnit;

    private String processName;

    /**
     * 不良品清单列表的数量
     */
    private BigDecimal exceptionNum;

    /**
     * 返修复核时会用到
     */
    private String ids;

    /**
     * 质检记录信息
     */
    private List<QualityControlInfoWhenRepair> qualityControlInfoWhenRepairList;
}
