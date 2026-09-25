/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.mapper;


import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.entity.StorageChangedIndexResult;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 成品库存变更历史Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface MicroFinishedProductStorageHistoryMapper {
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
     * 新增成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    public int insertMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory);

    /**
     * 批量新增
     * @param storageHistoryList
     * @return
     */
    int insertBatch(List<MicroFinishedProductStorageHistory> storageHistoryList);

    /**
     * 修改成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    public int updateMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory);

    /**
     * 删除成品库存变更历史
     *
     * @param id 成品库存变更历史ID
     * @return 结果
     */
    public int deleteMicroFinishedProductStorageHistoryById(Long id);

    /**
     * 批量删除成品库存变更历史
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroFinishedProductStorageHistoryByIds(Long[] ids);
}
