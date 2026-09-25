/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.service.impl;

import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.entity.StorageChangedIndexResult;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageHistoryMapper;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 成品库存变更历史Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Service
public class MicroFinishedProductStorageHistoryServiceImpl implements IMicroFinishedProductStorageHistoryService {
    @Autowired
    private MicroFinishedProductStorageHistoryMapper microFinishedProductStorageHistoryMapper;

    /**
     * 查询成品库存变更历史
     *
     * @param id 成品库存变更历史ID
     * @return 成品库存变更历史
     */
    @Override
    public MicroFinishedProductStorageHistory selectMicroFinishedProductStorageHistoryById(Long id) {
        return microFinishedProductStorageHistoryMapper.selectMicroFinishedProductStorageHistoryById(id);
    }

    /**
     * 查询成品库存变更历史列表
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 成品库存变更历史
     */
    @Override
    public List<MicroFinishedProductStorageHistory> selectMicroFinishedProductStorageHistoryList(MicroFinishedProductStorageHistory microFinishedProductStorageHistory) {
        return microFinishedProductStorageHistoryMapper.selectMicroFinishedProductStorageHistoryList(microFinishedProductStorageHistory);
    }

    /**
     * 查询成品库存变更历史列表
     *
     * @param param
     * @return
     */
    @Override
    public List<MicroFinishedProductStorageHistory> selectMicroFinishedStorageChangeHistoryList(MicroFinishStorageChangeHistoryParam param) {
        return microFinishedProductStorageHistoryMapper.selectMicroFinishedStorageChangeHistoryList(param);
    }

    /**
     * 统计库存变动相关指标（入库数、出库数、产品数）
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public StorageChangedIndexResult selectStorageChangedIndexInfo(Date startDate, Date endDate) {
        StorageChangedIndexResult result = new StorageChangedIndexResult();

        // 1.查询库存历史变动记录
        MicroFinishStorageChangeHistoryParam queryParam = new MicroFinishStorageChangeHistoryParam();
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);
        queryParam.setChangeTypeList(Arrays.asList(FinishStorageChangeTypeEnum.FINISH_INBOUND.getCode(),
                FinishStorageChangeTypeEnum.FINISH_CANCEL.getCode(), FinishStorageChangeTypeEnum.STORAGE_CHANGE.getCode(),
                FinishStorageChangeTypeEnum.STORAGE_INBOUND.getCode(), FinishStorageChangeTypeEnum.STORAGE_OUTBOUND.getCode())); // 期初导入的不计算在内 
        List<MicroFinishedProductStorageHistory> storageHistoryList = microFinishedProductStorageHistoryMapper.selectMicroFinishedStorageChangeHistoryList(queryParam);
        if (CollectionUtils.isEmpty(storageHistoryList)) {
            return result;
        }

        // 2.统计相关指标
        BigDecimal inBoundNum = BigDecimal.ZERO;
        BigDecimal outBoundNum = BigDecimal.ZERO;
        long inBoundProductCategoryNum, outBoundProductCategoryNum;

        // 统计出入库总数
        for (MicroFinishedProductStorageHistory storageHistory : storageHistoryList) {
            if (storageHistory.getChangeNum().signum() >= 0) {
                inBoundNum = inBoundNum.add(storageHistory.getChangeNum());
            } else {
                outBoundNum = outBoundNum.add(storageHistory.getChangeNum());
            }
        }

        // 统计产品款数
        inBoundProductCategoryNum = storageHistoryList.stream()
                .filter(e -> e.getChangeNum().signum() >= 0)
                .map(MicroFinishedProductStorageHistory::getProductSeq)
                .distinct()
                .count();
        outBoundProductCategoryNum = storageHistoryList.stream()
                .filter(e -> e.getChangeNum().signum() < 0)
                .map(MicroFinishedProductStorageHistory::getProductSeq)
                .distinct()
                .count();

        result.setInBoundProductCategoryNum(inBoundProductCategoryNum);
        result.setOutBoundProductCategoryNum(outBoundProductCategoryNum);
        result.setInBoundNum(inBoundNum);
        result.setOutBoundNum(outBoundNum);
        return result;
    }

    /**
     * 新增成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    @Override
    public int insertMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory) {
        return microFinishedProductStorageHistoryMapper.insertMicroFinishedProductStorageHistory(microFinishedProductStorageHistory);
    }

    /**
     * 修改成品库存变更历史
     *
     * @param microFinishedProductStorageHistory 成品库存变更历史
     * @return 结果
     */
    @Override
    public int updateMicroFinishedProductStorageHistory(MicroFinishedProductStorageHistory microFinishedProductStorageHistory) {
        return microFinishedProductStorageHistoryMapper.updateMicroFinishedProductStorageHistory(microFinishedProductStorageHistory);
    }

    /**
     * 批量删除成品库存变更历史
     *
     * @param ids 需要删除的成品库存变更历史ID
     * @return 结果
     */
    @Override
    public int deleteMicroFinishedProductStorageHistoryByIds(Long[] ids) {
        return microFinishedProductStorageHistoryMapper.deleteMicroFinishedProductStorageHistoryByIds(ids);
    }

    /**
     * 删除成品库存变更历史信息
     *
     * @param id 成品库存变更历史ID
     * @return 结果
     */
    @Override
    public int deleteMicroFinishedProductStorageHistoryById(Long id) {
        return microFinishedProductStorageHistoryMapper.deleteMicroFinishedProductStorageHistoryById(id);
    }
}
