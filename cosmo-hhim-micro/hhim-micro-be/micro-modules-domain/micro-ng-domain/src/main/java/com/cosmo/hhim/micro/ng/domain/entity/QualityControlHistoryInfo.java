/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.ng.domain.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检历史实体
 * @date 2023/4/10 16:57
 */
@Data
public class QualityControlHistoryInfo {

    private Long submitId;

    /**
     * 质检记录号
     */
    private String qualityControlRecordNo;

    private String submitNickName;

    private String submitDate;

    private String productName;

    private String productCode;

    private String productUnit;

    private String processName;

    /**
     * 质检人
     */
    private String qcNickName;

    /**
     * 质检时间
     */
    private String qcDate;

    private BigDecimal passNum;

    private BigDecimal ngNum;

    /**
     * 不良类型
     */
    private List<NgProductDetailInfo> ngProductDetailInfoList;
}
