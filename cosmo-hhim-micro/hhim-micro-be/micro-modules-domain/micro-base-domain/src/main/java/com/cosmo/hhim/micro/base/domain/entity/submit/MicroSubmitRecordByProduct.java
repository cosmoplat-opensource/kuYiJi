/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.submit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 产品维度列表返回对象
 * @date 2022/12/30 11:24
 */
@Data
public class MicroSubmitRecordByProduct {

    /**
     * 汇总的多条记录id
     */
    private String ids;

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品单位
     */
    private String unit;

    /**
     * 操作工序序列码
     */
    private String operateProcessSeq;

    /**
     * 操作工序名称
     */
    private String operateProcessName;

    /**
     * 操作工序编码
     */
    private String operateProcessCode;

    /**
     * 汇总之后总的良品数量
     */
    private BigDecimal passNum;

    /**
     * 汇总之后总的不良品数量
     */
    private BigDecimal ngNum;

    /**
     * 报工日
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date submitDay;

    /**
     * 报工状态（待审核、审核完毕）
     */
    private Long submitStatus;

    /**
     * 是否最后一道工序
     */
    private String isLastProcess;

    /**
     * 是否为首序
     */
    private String isFirstProcess;

    /**
     * 警示标示
     */
    private String warnFlag;

    /**
     * 详细的报工明细记录
     */
    private List<MicroWorkSubmitDto> microWorkSubmitDtoList;
}
