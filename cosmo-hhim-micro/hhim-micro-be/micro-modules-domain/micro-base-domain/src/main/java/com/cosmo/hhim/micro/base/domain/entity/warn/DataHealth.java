/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.warn;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author cosmo-hhim-open Team
 * @description: 数据健康度
 * @date 2022/12/26 15:16
 */
@Data
public class DataHealth {

    /**
     * 总的待审核报工记录数
     */
    private int totalWaitCheckSubmitRecords;

    /**
     * 异常的报工记录数
     */
    private int abnormalSubmitRecords;

    /**
     * 数据健康度
     */
    BigDecimal dataHealth;

    /* ****************
     *  具体异常的条数
     * ****************/

    /**
     * 负库存异常条数
     */
    private int negativeStockRecords;

    /**
     * 负库存异常对应的记录id汇总
     */
    private String negativeStockRecordIds;

    /**
     * 低良品率异常条数
     */
    private int lowPassRateRecords;

    /**
     * 低良品率异常对应的记录id汇总
     */
    private String lowPassRateRecordIds;

    /**
     * 超产能异常条数
     */
    private int overProductiveCapacityRecords;

    /**
     * 超产能异常对应的记录id汇总
     */
    private String overProductiveCapacityRecordIds;
}
