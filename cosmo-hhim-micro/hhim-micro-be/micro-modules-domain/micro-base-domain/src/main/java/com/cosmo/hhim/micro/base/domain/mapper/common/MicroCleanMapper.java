/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.common;

import com.cosmo.hhim.micro.base.domain.entity.clean.DuplicatedProduct;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @date 2023/1/13 16:02
 */
public interface MicroCleanMapper {

    /**
     * 获取重复产品序列码
     *
     * @return
     */
    List<DuplicatedProduct> obtainedDuplicatedProduct();

    /**
     * 根据productSeq查找报工记录
     *
     * @param productSeqList
     * @return
     */
    List<MicroWorkSubmit> obtainedWorkSubmitListByProductSeq(@Param("list") List<String> productSeqList);

    /**
     * 根据productSeq查询报工历史
     *
     * @param productSeqList
     * @return
     */
    List<MicroWorkSubmitHistory> obtainedWorkSubmitHistoryListByProductSeq(@Param("list") List<String> productSeqList);

    /**
     * 根据productSeq查询库存
     *
     * @param productSeqList
     * @return
     */
    List<MicroProcessStorage> obtainedProcessStorageListByProductSeq(@Param("list") List<String> productSeqList);

    /**
     * 根据productSeq查询库存历史
     *
     * @param productSeqList
     * @return
     */
    List<MicroProcessStorageHistory> obtainedProcessStorageHistoryListByProductSeq(@Param("list") List<String> productSeqList);

    /**
     * 批量更新
     *
     * @param microWorkSubmits
     * @return
     */
    int updateMicroWorkSubmitBatch(@Param("microWorkSubmits") List<MicroWorkSubmit> microWorkSubmits);

    /**
     * 修改数据
     *
     * @param microWorkSubmitHistory 实例对象
     * @return 影响行数
     */
    int updateMicroWorkSubmitHistory(MicroWorkSubmitHistory microWorkSubmitHistory);

    /**
     * 修改库存变动历史
     *
     * @param microProcessStorageHistory 库存变动历史
     * @return 结果
     */
    int updateMicroProcessStorageHistory(MicroProcessStorageHistory microProcessStorageHistory);

    /**
     * 批量更新库存记录
     *
     * @param microProcessStorageList
     * @return
     */
    int updateMicroProcessStorageBatch(@Param("list") List<MicroProcessStorage> microProcessStorageList);

    /**
     * 批量删除库存
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteMicroProcessStorageByIds(Long[] ids);

    /**
     * 批量删除产品
     *
     * @param seqList 需要删除的产品Seq
     * @return 结果
     */
    int deleteMicroProductByProductSeqList(String[] seqList);
}
