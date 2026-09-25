/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.submit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroWorkSubmitMultiMixedDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 产品唯一码
     */
    private String productSeq;
    private String productCode;
    private String productName;

    /**
     * 操作工序唯一码
     */
    private String operateProcessSeq;
    private String operateProcessCode;
    private String operateProcessName;
    /**
     * 前工序工序唯一码
     */
    private String preProcessSeq;
    private String preProcessCode;
    private String preProcessName;
    private String isFirstProcess;
    private String isLastProcess;

    /**
     * 良品数量
     */
    private BigDecimal passNum;

    /**
     * 不良品数量
     */
    private BigDecimal ngNum;
    /**
     * 报工日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date submitDay;
}
