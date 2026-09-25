/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检列表信息展示实体
 * @date 2023/4/5 15:48
 */
@Data
public class QualityControlOfSubmitRecordInfo {

    /**
     * 报工记录id
     */
    private Long id;

    private String productSeq;

    private String productCode;

    private String productName;

    private String productUnit;

    private String processSeq;

    private String processName;

    private String preProcessSeq;

    private String preProcessName;

    private String isLastProcess;

    private String isFirstProcess;

    private BigDecimal passNum;

    private BigDecimal ngNum;

    /**
     * 记工时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date submitDate;

    /**
     * 记工人昵称
     */
    private String submitNickName;

    /**
     * 质检时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date qcDate;

    /**
     * 质检人昵称
     */
    private String qcNickName;

    /**
     * 是否已经质检
     */
    private Long checkStatus;

    /**
     * 备注
     *
     * 待检测 - 显示记工的备注
     * 已检测 - 显示质检的备注
     */
    private String remark;
}
