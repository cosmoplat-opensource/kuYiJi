/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.tech.impl;


import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroProcessChainMapper;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroProcessChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 工艺链Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
@Service
public class MicroProcessChainServiceImpl implements IMicroProcessChainService {
    @Autowired
    private MicroProcessChainMapper microProcessChainMapper;

    /**
     * 查询工艺链
     *
     * @param id 工艺链ID
     * @return 工艺链
     */
    @Override
    public MicroProcessChain selectMicroProcessChainById(Long id) {
        return microProcessChainMapper.selectMicroProcessChainById(id);
    }

    /**
     * 查询工艺链列表
     *
     * @param microProcessChain 工艺链
     * @return 工艺链
     */
    @Override
    public List<MicroProcessChain> selectMicroProcessChainList(MicroProcessChain microProcessChain) {
        return microProcessChainMapper.selectMicroProcessChainList(microProcessChain);
    }

    @Override
    public List<MicroProcessChain> selectMicroProcessChainListByTechId(Long techId) {
        return microProcessChainMapper.selectMicroProcessChainListByTechId(techId);
    }

    /**
     * 新增工艺链
     *
     * @param microProcessChain 工艺链
     * @return 结果
     */
    @Override
    public int insertMicroProcessChain(MicroProcessChain microProcessChain) {
        return microProcessChainMapper.insertMicroProcessChain(microProcessChain);
    }

    /**
     * 修改工艺链
     *
     * @param microProcessChain 工艺链
     * @return 结果
     */
    @Override
    public int updateMicroProcessChain(MicroProcessChain microProcessChain) {
        return microProcessChainMapper.updateMicroProcessChain(microProcessChain);
    }

    /**
     * 先将数据归档到历史表中
     * 再批量删除工艺链
     *
     * @param ids 需要删除的工艺链ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessChainByIds(Long[] ids) {
        microProcessChainMapper.saveProcessChainHistory(ids);
        return microProcessChainMapper.deleteMicroProcessChainByIds(ids);
    }

    /**
     * 先将数据归档到历史表中
     * 再根据工艺ID集合批量删除工艺链
     */
    @Override
    public int deleteMicroProcessChainByTechIds(Long[] techIds) {
        microProcessChainMapper.saveProcessChainHistoryByTech(techIds);
        return microProcessChainMapper.deleteByTechIds(techIds);
    }

    /**
     * 先将数据归档到历史表中
     * 再批量删除工艺链
     *
     * @param id 工艺链ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessChainById(Long id) {
        microProcessChainMapper.saveProcessChainHistory(new Long[]{id});
        return microProcessChainMapper.deleteMicroProcessChainById(id);
    }

    @Override
    public int insertMicroProcessChainBatch(List<MicroProcessChain> chainList) {
        return microProcessChainMapper.insertBatch(chainList);
    }

    /**
     * 根据产品获取标准的工序
     */
    @Override
    public List<String> selectStandardProcessChainByProduct(String productCode, String productSeq, Long productId) {
        Assert.notNull(productId + String.valueOf(productSeq) + productCode, "产品信息数据不能为空");
        return microProcessChainMapper.selectStandardProcessChainByProduct(productCode, productSeq, productId);
    }

}
