/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.ng;

import com.cosmo.hhim.micro.ng.domain.entity.NgProductDetailInfo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 质检明细信息
 * @date 2023/4/3 18:03
 */
@Data
public class DetailQualityControlInfo {

    private String productCode;

    private String productName;

    private String productUnit;

    private String processName;

    private String preProcessName;

    /**
     * 报工良品数量
     */
    private BigDecimal passNum;

    /**
     * 报工不良品数量
     */
    private BigDecimal ngNum;

    /**
     * 报工人昵称
     */
    private String submitNickName;

    /**
     * 报工日期
     */
    private Date submitDate;

    /**
     * 质检人昵称
     */
    private String qcNickName;

    /**
     * 质检时间
     */
    private Date qcDate;

    /**
     * 质检之后良品数量
     */
    private BigDecimal checkPassNum;

    /**
     * 质检之后不良品数量
     */
    private BigDecimal checkNgNum;

    /**
     * 报工记录的备注
     */
    private String submitRecordRemark;

    /**
     * 质检时的备注
     */
    private String remark;

    /**
     * 质检之后详细的不良品信息
     */
    private List<NgProductDetailInfo> ngProductDetailInfoList;

    /**
     * 返修数量
     */
    private BigDecimal repairNum;

    /**
     * 报废数量
     */
    private BigDecimal abandonedNum;
}
