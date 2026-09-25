/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/20
 */
@Data
public class StaffProcessSeqInfoForProduct {

    // 产品名称 
    private String productName;

    // 产品唯一吗 
    private String productSeq;

    // 产品编码 
    private String productCode;

    // 产品总数 
    private BigDecimal productTotalNum;

    // 工序信息集合 
    private List<ProcessSeqInfo> processSeqInfoList;

    /**
     * 工序信息
     */
    @Data
    public static class ProcessSeqInfo {

        // 工序唯一码
        private String processSeq;

        // 工序名称
        private String processName;

        // 工序编码
        private String processCode;

        // 总良品数
        private BigDecimal totalPassProductNum;

        // 总不良品数
        private BigDecimal totalNgProductNum;
    }

}
