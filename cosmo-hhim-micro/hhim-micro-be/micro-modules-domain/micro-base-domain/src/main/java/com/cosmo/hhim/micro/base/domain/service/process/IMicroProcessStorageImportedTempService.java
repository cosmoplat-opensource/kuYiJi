/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process;


import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageImportedTemp;

import java.util.List;

/**
 * 期初工序库存导入临时Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-12
 */
public interface IMicroProcessStorageImportedTempService {

    /**
     * 查询期初工序库存导入临时
     *
     * @param id 期初工序库存导入临时ID
     * @return 期初工序库存导入临时
     */
    public MicroProcessStorageImportedTemp selectMicroProcessStorageImportedTempById(Long id);

    /**
     * 查询期初工序库存导入临时列表
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 期初工序库存导入临时集合
     */
    public List<MicroProcessStorageImportedTemp> selectMicroProcessStorageImportedTempList(MicroProcessStorageImportedTemp microProcessStorageImportedTemp);

    /**
     * 新增期初工序库存导入临时
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 结果
     */
    public int insertMicroProcessStorageImportedTemp(MicroProcessStorageImportedTemp microProcessStorageImportedTemp);

    /**
     * 修改期初工序库存导入临时
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 结果
     */
    public int updateMicroProcessStorageImportedTemp(MicroProcessStorageImportedTemp microProcessStorageImportedTemp);

    /**
     * 批量删除期初工序库存导入临时
     *
     * @param ids 需要删除的期初工序库存导入临时ID
     * @return 结果
     */
    public int deleteMicroProcessStorageImportedTempByIds(Long[] ids);

    /**
     * 删除期初工序库存导入临时信息
     *
     * @param id 期初工序库存导入临时ID
     * @return 结果
     */
    public int deleteMicroProcessStorageImportedTempById(Long id);

    /**
     * 确认导入
     */
    void importConfirm(String flag); 

    /**
     * 取消导入
     */
    void importCancel();
}
