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
 * @description: 返修复核明细实体
 * @date 2023/4/7 17:02
 */
@Data
public class DetailRepairAndThenQualityControlInfo {

    private String submitNickName;

    private String productName;

    private String productCode;

    private String processName;

    private String repairNickName;

    private BigDecimal repairNum;

    private BigDecimal concessionNum;

    private BigDecimal abandonedNum;

    private String abandonedType;

    private String remark;

    /**
     * 质检记录信息
     */
    private List<QualityControlInfoWhenRepair> qualityControlInfoWhenRepairList;
}