/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.process;

import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 库存变动历史Mapper接口
 *
 * @date 2022-10-11
 */
public interface MicroProcessStorageHistoryMapper {
    /**
     * 查询库存变动历史
     *
     * @param id 库存变动历史ID
     * @return 库存变动历史
     */
    MicroProcessStorageHistory selectMicroProcessStorageHistoryById(Long id);

    /**
     * 查询库存变动历史列表
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 库存变动历史集合
     */
    List<MicroProcessStorageHistory> selectMicroProcessStorageHistoryList(MicroProcessStorageHistory microProcessStorageHistory);

    /**
     * 新增库存变动历史
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 结果
     */
    int insertMicroProcessStorageHistory(MicroProcessStorageHistory microProcessStorageHistory);

    /**
     * 批量新增库存变动历史记录
     *
     * @param microProcessStorageHistoryList
     * @return
     */
    int insertMicroProcessStorageHistoryBatch(@Param("list") List<MicroProcessStorageHistory> microProcessStorageHistoryList);

    /**
     * 修改库存变动历史
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 结果
     */
    int updateMicroProcessStorageHistory(MicroProcessStorageHistory microProcessStorageHistory);

    /**
     * 删除库存变动历史
     *
     * @param id 库存变动历史ID
     * @return 结果
     */
    int deleteMicroProcessStorageHistoryById(Long id);

    /**
     * 批量删除库存变动历史
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroProcessStorageHistoryByIds(Long[] ids);

    /**
     * 清理数据
     *
     * @return
     */
    int deleteAllMicroProcessStorageHistory();
}
