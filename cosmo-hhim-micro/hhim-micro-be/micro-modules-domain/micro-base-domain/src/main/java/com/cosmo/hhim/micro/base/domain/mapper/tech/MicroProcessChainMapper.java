/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.tech;

import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工艺链Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-20
 */
public interface MicroProcessChainMapper {
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
     * 删除工艺链
     *
     * @param id 工艺链ID
     * @return 结果
     */
    public int deleteMicroProcessChainById(Long id);

    /**
     * 批量删除工艺链
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProcessChainByIds(Long[] ids);

    int insertBatch(@Param("dataList") List<MicroProcessChain> chainList); 

    int deleteByTechIds(Long[] ids); 

    List<MicroProcessChain> selectMicroProcessChainListByTechId(Long techId); 

    /**
     * 根据产品获取标准的工序
     *
     * @param productCode
     * @param productSeq
     * @param productId
     * @return
     */
    List<String> selectStandardProcessChainByProduct(@Param("productCode") String productCode, @Param("productSeq") String productSeq, @Param("productId") Long productId);

    /**
     * 保存历史工艺链信息
     *
     * @param techIds
     * @return
     */
    int saveProcessChainHistoryByTech(Long[] techIds);

    /**
     * 保存历史工艺链信息
     *
     * @param ids
     * @return
     */
    int saveProcessChainHistory(Long[] ids);

    /**
     * 根据工序ID获取工艺路线记录
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids);
}
