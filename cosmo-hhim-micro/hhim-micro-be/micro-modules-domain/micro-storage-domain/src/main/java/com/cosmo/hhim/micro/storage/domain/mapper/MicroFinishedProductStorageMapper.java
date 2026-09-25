/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.mapper;

import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.storage.domain.entity.CountInDiffWarnStatus;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.WarnFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.WarnStorageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 成品库存Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface MicroFinishedProductStorageMapper {
    /**
     * 查询成品库存
     *
     * @return 成品库存
     */
    MicroFinishedProductStorage selectMicroFinishedProductStorageByProductSeq(String productSeq); 

    /**
     * 查询成品库存列表
     *
     * @param microFinishedProductStorage 成品库存
     * @return 成品库存集合
     */
    public List<MicroFinishedProductStorage> selectMicroFinishedProductStorageList(MicroFinishedProductStorage microFinishedProductStorage);

    /**
     * 新增成品库存
     *
     * @param microFinishedProductStorage 成品库存
     * @return 结果
     */
    public int insertMicroFinishedProductStorage(MicroFinishedProductStorage microFinishedProductStorage);

    /**
     * 批量新增
     *
     * @param list
     * @return
     */
    int insertBatch(List<MicroFinishedProductStorage> list);

    /**
     * 修改成品库存
     *
     * @param microFinishedProductStorage 成品库存
     * @return 结果
     */
    public int updateMicroFinishedProductStorage(MicroFinishedProductStorage microFinishedProductStorage);

    /**
     * 根据产品编码更新产成品库存信息
     *
     * @param microFinishedProductStorage
     * @return
     */
    int updateStorageByProductSeq(MicroFinishedProductStorage microFinishedProductStorage);

    /**
     * 批量更新
     *
     * @param list
     */
    void updateBatch(List<MicroFinishedProductStorage> list);

    /**
     * 不在安全范围内的产品数量
     *
     * @return
     */
    List<MicroFinishedProductStorage> selectMicroFinishedProductStorageFromOverSafetyStock();

    /**
     * 库存预警列表
     *
     * @param warnStorageParam
     * @return
     */
    List<WarnFinishedProductStorage> selectMicroFinishedProductStorageForWarn(WarnStorageParam warnStorageParam);

    /**
     * 根据物料列表查询库存
     *
     * @param productSeqList
     * @return
     */
    List<MicroFinishedProductStorage> selectMicroFinishedProductStorageByProductSeqList(@Param("productSeqList") List<String> productSeqList);

    /**
     * 预警列表中不同预警装爱
     *
     * @param productNameOrCode
     * @return
     */
    CountInDiffWarnStatus countProductNumInDiffWarnStatus(String productNameOrCode);

    /**
     * 根据产品ids查询关联的产品数据
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProductByProductIds(Long[] ids);
}
