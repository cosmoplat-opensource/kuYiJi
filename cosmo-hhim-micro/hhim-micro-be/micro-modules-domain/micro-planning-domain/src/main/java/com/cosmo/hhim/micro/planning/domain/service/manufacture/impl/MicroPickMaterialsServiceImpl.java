/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.planning.domain.service.manufacture.impl;

import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroPickMaterialsMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroPickMaterialsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 投料单/退料单Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-10
 */
@Service
public class MicroPickMaterialsServiceImpl implements IMicroPickMaterialsService {
    @Autowired
    private MicroPickMaterialsMapper microPickMaterialsMapper;

    /**
     * 查询投料单/退料单
     *
     * @param id 投料单/退料单ID
     * @return 投料单/退料单
     */
    @Override
    public MicroPickMaterials selectMicroPickMaterialsById(Long id)
    {
        return microPickMaterialsMapper.selectMicroPickMaterialsById(id);
    }

    /**
     * 查询投料单/退料单列表
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 投料单/退料单
     */
    @Override
    public List<MicroPickMaterials> selectMicroPickMaterialsList(MicroPickMaterials microPickMaterials) {
        return microPickMaterialsMapper.selectMicroPickMaterialsList(microPickMaterials);
    }

    /**
     * 新增投料单/退料单
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    @Override
    public int insertMicroPickMaterials(MicroPickMaterials microPickMaterials)
    {
        return microPickMaterialsMapper.insertMicroPickMaterials(microPickMaterials);
    }

    /**
     * 修改投料单/退料单
     * 
     * @param microPickMaterials 投料单/退料单
     * @return 结果
     */
    @Override
    public int updateMicroPickMaterials(MicroPickMaterials microPickMaterials)
    {
        return microPickMaterialsMapper.updateMicroPickMaterials(microPickMaterials);
    }

    /**
     * 批量删除投料单/退料单
     * 
     * @param ids 需要删除的投料单/退料单ID
     * @return 结果
     */
    @Override
    public int deleteMicroPickMaterialsByIds(Long[] ids)
    {
        return microPickMaterialsMapper.deleteMicroPickMaterialsByIds(ids);
    }

    /**
     * 删除投料单/退料单信息
     * 
     * @param id 投料单/退料单ID
     * @return 结果
     */
    @Override
    public int deleteMicroPickMaterialsById(Long id)
    {
        return microPickMaterialsMapper.deleteMicroPickMaterialsById(id);
    }

    /**
     * 查询投料单/退料单列表
     */
    @Override
    public List<MicroPickMaterials> getListByWorkOrder(MicroPickMaterials microPickMaterials) {
        MicroPickMaterials query = new MicroPickMaterials();
        query.setWorkOrderNo(microPickMaterials.getWorkOrderNo());
        query.setProductSeqList(microPickMaterials.getProductSeqList());
        List<MicroPickMaterials> microPickMaterialsList = microPickMaterialsMapper.getMicroPickMaterialsList(query);
        return microPickMaterialsList;
    }

    /**
     * 根据工单+物料获取对应投退料数量总和
     */
    @Override
    public Map<String, MicroPickMaterials> selectMaterialShortageInfo(List<MicroManufactureWorkOrderMaterialEntity> materialDemandList) {
        List<MicroPickMaterials> pickMaterialsList = microPickMaterialsMapper.selectMaterialShortageInfo(materialDemandList);
        if (CollectionUtils.isEmpty(pickMaterialsList)) {
            return Collections.emptyMap();
        }
        return pickMaterialsList.stream().collect(Collectors.groupingBy(k -> k.getWorkOrderNo() + "&" + k.getProductSeq(),
                Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))));
    }

}
