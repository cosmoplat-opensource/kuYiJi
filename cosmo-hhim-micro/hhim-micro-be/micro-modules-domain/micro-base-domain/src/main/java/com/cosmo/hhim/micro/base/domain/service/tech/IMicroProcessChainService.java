/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.tech;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;

import java.util.List;

/**
 * 工艺链Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
public interface IMicroProcessChainService {
    /**
     * 查询工艺链
     *
     * @param id 工艺链ID
     * @return 工艺链
     */
    public MicroProcessChain selectMicroProcessChainById(Long id);

    /**
     * 查询工艺链列表
     *
     * @param microProcessChain 工艺链
     * @return 工艺链集合
     */
    public List<MicroProcessChain> selectMicroProcessChainList(MicroProcessChain microProcessChain);

    public List<MicroProcessChain> selectMicroProcessChainListByTechId(Long techId); 

    /**
     * 新增工艺链
     *
     * @param microProcessChain 工艺链
     * @return 结果
     */
    public int insertMicroProcessChain(MicroProcessChain microProcessChain);

    /**
     * 修改工艺链
     *
     * @param microProcessChain 工艺链
     * @return 结果
     */
    public int updateMicroProcessChain(MicroProcessChain microProcessChain);

    /**
     * 批量删除工艺链
     *
     * @param ids 需要删除的工艺链ID
     * @return 结果
     */
    public int deleteMicroProcessChainByIds(Long[] ids);

    public int deleteMicroProcessChainByTechIds(Long[] techIds); 

    /**
     * 删除工艺链信息
     *
     * @param id 工艺链ID
     * @return 结果
     */
    public int deleteMicroProcessChainById(Long id);

    /**
     * 批量插入
     *
     * @param chainList
     * @return
     */
    int insertMicroProcessChainBatch(List<MicroProcessChain> chainList);

    List<String> selectStandardProcessChainByProduct(String productCode, String productSeq, Long productId); 

}
