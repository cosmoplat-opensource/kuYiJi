/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.base;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroProcessMultiMixedResultDTO implements Serializable {

    private String productSeq;

    private String operateProcessSeq;

    private Long preProcessId;
    private String preProcessSeq;
    private String preProcessCode;
    private String preProcessName;

    /**
     * 是否为首序
     */
    private Boolean isFirstProcess = false; 

    /**
     * 是否为尾序
     */
    private Boolean isLastProcess = false; 
    /**
     * 前工序未审核数量(默认为0)
     */
    private BigDecimal unapprovedNum;
    /**
     * 前工序总库存数量(默认为0)
     */
    private BigDecimal totalStockNum;
}
