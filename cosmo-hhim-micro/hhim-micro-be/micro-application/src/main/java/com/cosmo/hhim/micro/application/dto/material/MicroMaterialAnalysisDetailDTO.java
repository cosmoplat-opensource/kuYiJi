/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.material;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 物料需求分析传输实体
 *
 * @author cosmo-hhim-open Team
 */
@Data
public class MicroMaterialAnalysisDetailDTO implements Serializable {
    /**
     * 工单号
     */
    private String workOrderNo;
    private String productSeq;
    private String productCode;
    private String productName;
    private String productUnit;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planStartDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date planEndDate;
    /**
     * 计划数量
     */
    private BigDecimal workOrderNum;
    /**
     * 实际需求数量
     */
    private BigDecimal demandNum;
    /**
     * 是否缺料
     */
    private Boolean shortage = true; 

}

