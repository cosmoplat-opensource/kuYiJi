/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.dto.storage;

import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Data
public class FinishStorageChangeHistoryGpByDayResult {

    // 日期（格式：yyyy-MM-dd） 
    private String date;

    // 入库数量 
    private BigDecimal inBoundNum;

    // 出库数量 
    private BigDecimal outBoundNum;

    // 库存结余数量 
    private BigDecimal finishChangeNum;

    // 库存变动详细信息列表 
    private List<MicroFinishedProductStorageHistory> storageHistoryList;

}
