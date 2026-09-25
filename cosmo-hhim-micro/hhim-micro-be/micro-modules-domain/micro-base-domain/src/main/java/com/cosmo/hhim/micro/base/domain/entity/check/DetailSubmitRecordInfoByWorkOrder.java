/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.check;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 工单维度进去之后的详细报工记录信息
 * @date 2023/3/23 13:05
 */
@Data
public class DetailSubmitRecordInfoByWorkOrder {

    /**
     * 产品序列码 用于查找工艺
     */
    private String productSeq;

    private String operateProcessSeq;

    private String operateProcessCode;

    private String operateProcessName;

    /**
     * 是否为最后一道工序
     */
    private String isLastProcess;

    private String isFirstProcess;

    /**
     * 报工记录数量
     */
    private int recordNum;

    /**
     * 记工数量
     */
    private BigDecimal totalNum;

    /**
     * 单位
     */
    private String productUnit;

    /**
     * 到人的报工记录信息
     */
    List<UserSubmitRecordInfoByWorkOrder> userSubmitRecordInfoByWorkOrderList;
}
