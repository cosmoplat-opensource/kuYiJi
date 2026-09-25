/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.bom;

import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 产品BOMMapper接口
 * 
 * @author cosmo-hhim-open Team
 * @date 2023-03-07
 */
public interface MicroProductBomMapper 
{
    /**
     * 查询产品BOM
     * 
     * @param id 产品BOMID
     * @return 产品BOM
     */
    public MicroProductBom selectMicroProductBomById(Long id);

    /**
     * 查询产品BOM列表
     * 
     * @param microProductBom 产品BOM
     * @return 产品BOM集合
     */
    public List<MicroProductBom> selectMicroProductBomList(MicroProductBom microProductBom);

    /**
     * 新增产品BOM
     * 
     * @param microProductBom 产品BOM
     * @return 结果
     */
    public int insertMicroProductBom(MicroProductBom microProductBom);

    /**
     * 批量新增产品BOM
     *
     * @param list 产品BOM集合
     * @return 结果
     */
    int insertMicroProductBomList(List<MicroProductBom> list);

    /**
     * 更新子BOM
     *
     * @param map
     * @return
     */
    void updateSonMicroProductBom(Map<String,Object> map);

    /**
     * 根据产品SEQ查询产品BOM
     *
     * @param map
     * @return 产品BOM
     */
    int getIfChildren(Map<String,Object> map);

    /**
     * 修改产品BOM
     * 
     * @param microProductBom 产品BOM
     * @return 结果
     */
    public int updateMicroProductBom(MicroProductBom microProductBom);

    /**
     * 修改产品BOM
     *
     * @param microProductBom 产品BOM
     * @return 结果
     */
    public int updateBomByProductSeq(MicroProductBom microProductBom);

    /**
     * 批量修改产品BOM
     *
     * @param bomList 产品BOM集合
     * @return 结果
     */
    int updateMicroProductBomList(List<MicroProductBom> bomList);

    /**
     * 删除产品BOM
     * 
     * @param id 产品BOMID
     * @return 结果
     */
    public int deleteMicroProductBomById(Long id);

    /**
     * 批量删除产品BOM
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProductBomByIds(Long[] ids);

    /**
     * 删除产品BOM以及下级BOM
     *
     * @param id 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroProductBomByParentId(Long id);

    MicroProductBom getMicroProductBomByProductSeq(@Param("productSeq") String productSeq, @Param("bomType") String bomType); 

    List<MicroProductBom> getMicroProductBomByParentId(MicroProductBom microProductBom); 

    List<MicroProductBom> selectMicroProductBomListByProducts(@Param("condition") MicroProductBom condition, @Param("productSeqList") List<String> productSeqList); 

    /**
     * productSeq
     *
     * @param productSeq
     * @return
     */
    String getAllParentProductSeqList(String productSeq);

    /**
     * 根据产品序列码list查询上一级的bom信息，在代码中通过迭代的方式获取所有父辈bom
     *
     * @param productSeqList
     * @return
     */
    List<String> selectFirstParentBomNodeList(@Param("productSeqList") List<String> productSeqList);

    int deleteMicroProductBomByProductSeq(@Param("productSeqList") List<String> seqList); 
}
