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
 * @description: 产品维度详细的审核报工记录信息
 * @date 2023/2/9 11:21
 */
@Data
public class DetailCheckSubmitRecordByProduct {

    /**
     * 工序序列码
     */
    private String processSeq;

    /**
     * 工序编码
     */
    private String processCode;

    /**
     * 工序名称
     */
    private String processName;

    /**
     * 产品序列码
     */
    private String productSeq;

    /**
     * 产品编码
     */
    private String productCode;

    /**
     * 产品名称
     */
    private String productName;

    /**
     * 产品单位
     */
    private String productUnit;

    /**
     * 库存数量
     */
    private BigDecimal stockNum;

    /**
     * 记工数量
     */
    private BigDecimal submitNum;

    /**
     * 流转数量
     */
    private BigDecimal processFlowNum;

    /**
     * 记录条数
     */
    private int recordNum;

    /**
     * 尾序标示
     */
    private String isLastProcess;

    /**
     * 首序
     */
    private String isFirstProcess;

    /**
     * 前工序序列码, 用于判断流转数量
     */
    private String preProcessSeq;

    /**
     * 审产后的数量
     */
    private BigDecimal checkNum;

    /**
     * 通过预警标示
     * 0 - 是， 1 - 否
     */
    private String commonWarnFlag;

    /**
     * 预警提示文案
     */
    private String commonWarnMessage;

    /**
     * 超报预警标示
     * 0 - 是, 1 - 否
     */
    private String overSubmitWarnFlag;

    /**
     * 超报预警提示文案
     */
    private String overSubmitWarnMessage;

    /**
     * 该产品下面用户的详细报工记录信息
     */
    private List<UserDetailSubmitRecordInfo> userDetailSubmitRecordInfos;

    /**
     * 流转工序列表
     */
    List<ProcessFlow> processFlowList;
}