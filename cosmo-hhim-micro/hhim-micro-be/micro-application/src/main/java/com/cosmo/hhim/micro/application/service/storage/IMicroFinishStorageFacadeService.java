/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage;

import com.cosmo.hhim.micro.application.dto.storage.FinishStorageChangeHistoryGpByDayResult;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageBoundBatchParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
public interface IMicroFinishStorageFacadeService {

    /**
     * 查询产成品库存列表
     * @param microFinishedProductStorage
     * @return
     */
    List<MicroFinishedProductStorage> selectMicroFinishedProductStorageList(MicroFinishedProductStorage microFinishedProductStorage);

    /**
     * 邮件导出完工库存信息
     * @param productCodeOrName
     * @param receivedBy
     * @return
     */
    String exportFinishStorageInfos(String productCodeOrName, String receivedBy);

    /**
     * 邮件导出完工库存变动信息
     * @param productSeq
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportFinishStorageHistoryInfos(Date startDate, Date endDate, String productSeq, String receivedBy);

    /**
     * 查询产成品库存变动列表
     * @param param
     * @return
     */
    List<FinishStorageChangeHistoryGpByDayResult> changeHistoryList(MicroFinishStorageChangeHistoryParam param);


    /**
     * 批量出入库
     * @param param
     */
    void batchInOrOutBound(MicroFinishProductStorageBoundBatchParam param);

}
