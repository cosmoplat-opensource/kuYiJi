/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.service;

import com.cosmo.hhim.micro.storage.domain.entity.*;

import java.util.List;

/**
 * 成品库存Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface IMicroFinishedProductStorageService {
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
     * 修改成品库存
     *
     * @param param
     * @return 结果
     */
    void updateMicroFinishedProductStorage(MicroFinishProductStorageAdjustParam param);

    /**
     * 调整成品库存
     * @param param
     */
    void changeBound(ChangeFinishedStorageParam param);

    /**
     * 不在安全范围内的产品数量
     *
     * @return
     */
    List<MicroFinishedProductStorage> selectMicroFinishedProductStorageFromOverSafetyStock();

    /**
     * 库存预警展示列表
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
    List<MicroFinishedProductStorage> selectMicroFinishedProductStorageByProductSeqList(List<String> productSeqList);

    /**
     * 预警列表中不同预警装爱
     *
     * @param productNameOrCode
     * @return
     */
    CountInDiffWarnStatus countProductNumInDiffWarnStatus(String productNameOrCode);
}
