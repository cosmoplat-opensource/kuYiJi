/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.service;

import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.entity.StorageChangedIndexResult;

import java.util.Date;
import java.util.List;

/**
 * 成品库存变更历史Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface IMicroFinishedProductStorageHistoryService {
    /**
     * 查询成品库存变更历史
     *
     * @param id 成品库存变更历史ID
     * @return 成品库存变更历史
     */
    public MicroFinishedProductStorageHistory selectMicroFinishedProductStorageHistoryById(Long id);

    /**
     * 查询成品库存变更历史列表
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 成品库存变更历史集合
     */
    public List<MicroFinishedProductStorageHistory> selectMicroFinishedProductStorageHistoryList(MicroFinishedProductStorageHistory microFinishedProductStorageHistory);

    /**
     * 查询成品库存变更历史列表
     * @param param
     * @return
     */
    List<MicroFinishedProductStorageHistory> selectMicroFinishedStorageChangeHistoryList(MicroFinishStorageChangeHistoryParam param);

    /**
     * 统计库存变动相关指标（入库数、出库数、产品数）
     * @param startDate
     * @param endDate
     * @return
     */
    StorageChangedIndexResult selectStorageChangedIndexInfo(Date startDate, Date endDate);

    /**
     * 新增成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    public int insertMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory);

    /**
     * 修改成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    public int updateMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory);

    /**
     * 批量删除成品库存变更历史
     *
     * @param ids 需要删除的成品库存变更历史ID
     * @return 结果
     */
    public int deleteMicroFinishedProductStorageHistoryByIds(Long[] ids);

    /**
     * 删除成品库存变更历史信息
     *
     * @param id 成品库存变更历史ID
     * @return 结果
     */
    public int deleteMicroFinishedProductStorageHistoryById(Long id);
}
