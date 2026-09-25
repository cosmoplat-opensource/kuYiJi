/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import com.cosmo.hhim.micro.ng.domain.entity.NgProductDetailInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检操作前端传递的参数
 * @date 2023/4/3 15:11
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QualityControlOfSubmitRecordDto {

    /**
     * 报工记录id
     */
    private Long submitId;

    /**
     * 质检员确定的良品数量
     */
    private BigDecimal checkPassNum;

    /**
     * 质检员确定的不良品数量
     */
    private BigDecimal checkNgNum;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 不良品详细信息
     */
    List<NgProductDetailInfo> ngProductDetailInfos;
}
