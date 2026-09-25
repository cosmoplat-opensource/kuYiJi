/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.bom;

import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBomHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 产品BOMMapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-08
 */
public interface MicroProductBomHistoryMapper 
{
    /**
     * 查询产品BOM
     * 
     * @param id 产品BOMID
     * @return 产品BOM
     */
    public MicroProductBomHistory selectMicroProductBomHistoryById(Long id);

    /**
     * 查询产品BOM列表
     * 
     * @param microProductBomHistory 产品BOM
     * @return 产品BOM集合
     */
    public List<MicroProductBomHistory> selectMicroProductBomHistoryList(MicroProductBomHistory microProductBomHistory);

    /**
     * 新增产品BOM
     * 
     * @param microProductBomHistory 产品BOM
     * @return 结果
     */
    public int insertMicroProductBomHistory(MicroProductBomHistory microProductBomHistory);

    /**
     * 批量新增产品BOM历史
     *
     * @param list 产品BOM集合
     * @return 结果
     */
    int insertMicroProductBomHistoryList(List<MicroProductBomHistory> list);

    /**
     * 修改产品BOM
     * 
     * @param microProductBomHistory 产品BOM
     * @return 结果
     */
    public int updateMicroProductBomHistory(MicroProductBomHistory microProductBomHistory);

    /**
     * 删除产品BOM
     * 
     * @param id 产品BOMID
     * @return 结果
     */
    public int deleteMicroProductBomHistoryById(Long id);

    /**
     * 批量删除产品BOM
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProductBomHistoryByIds(Long[] ids);

    int getMaxVersion(String productSeq); 

    int deleteMicroProductBomHistoryByProductSeq(@Param("productSeqList") List<String> seqList); 
}
