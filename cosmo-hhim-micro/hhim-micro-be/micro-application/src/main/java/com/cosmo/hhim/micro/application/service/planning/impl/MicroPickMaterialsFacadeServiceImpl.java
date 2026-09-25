/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.planning.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.cosmo.hhim.micro.application.dto.material.MicroPickingMaterialDetailInfoDTO;
import com.cosmo.hhim.micro.application.dto.planning.MicroPickMaterialsDto;
import com.cosmo.hhim.micro.application.service.planning.IMicroPickMaterialsFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.service.bom.IMicroProductBomService;
import com.cosmo.hhim.micro.infrastructure.enums.planning.MaterialsTypeEnum;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroPickMaterials;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroPickMaterialsMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroPickMaterialsService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
public class MicroPickMaterialsFacadeServiceImpl implements IMicroPickMaterialsFacadeService {

    @Autowired
    private MicroPickMaterialsMapper microPickMaterialsMapper;

    @Autowired
    private IMicroProductBomService microProductBomService;

    @Autowired
    private IMicroPickMaterialsService microPickMaterialsService;

    /**
     * 查询投料单/退料单列表
     *
     * bom + 物料单的bom取并集
     *
     * @param microPickMaterialsDto 投料单/退料单
     * @return 投料单/退料单
     */
    @Override
    public MicroPickingMaterialDetailInfoDTO getListByWorkOrder(MicroPickMaterialsDto microPickMaterialsDto) { 
        // 存储结果
        MicroPickingMaterialDetailInfoDTO result = new MicroPickingMaterialDetailInfoDTO();
        // 存储投料单和退料单信息
        List<MicroPickMaterialsDto> pickMaterialsDtoList = new ArrayList<>();
        MicroProductBom microProductBom = microProductBomService.getSonBomList(microPickMaterialsDto.getProductSeq(), microPickMaterialsDto.getProductName());
        result.setMicroProductBom(microProductBom);

        MicroPickMaterials microPickMaterials = new MicroPickMaterials();
        BeanUtils.copyProperties(microPickMaterialsDto, microPickMaterials);
        List<MicroPickMaterials> microPickMaterialsList = microPickMaterialsService.getListByWorkOrder(microPickMaterials);

        // 判断是投料还是退料展示信息
        if (MaterialsTypeEnum.PICKING.getCode().equals(microPickMaterials.getMaterialsType())) {
            // 只展示投料单信息
            if (CollectionUtil.isEmpty(microPickMaterialsList) && null == microProductBom) {
                pickMaterialsDtoList = null;
            } else if (CollectionUtil.isNotEmpty(microPickMaterialsList) && null == microProductBom) {
                pickMaterialsDtoList = microPickMaterialsList.stream().map(e -> {
                    MicroPickMaterialsDto entity = new MicroPickMaterialsDto();
                    BeanUtils.copyProperties(e, entity);
                    return entity;
                }).collect(Collectors.toList());
            } else {
                result.setBomType(microProductBom.getBomType());
                // bom不为null的情况
                List<String> productSeqList = microProductBom.getSecondBomList().stream().map(MicroProductBom::getProductSeq).collect(Collectors.toList());
                // bom 相关的投料单
                List<MicroPickMaterialsDto> pickMaterialsDtoListFromBom = microProductBom.getSecondBomList().stream().map(e -> {
                    MicroPickMaterialsDto entity = new MicroPickMaterialsDto();
                    entity.setPickingNumber(BigDecimal.ZERO);
                    entity.setReturnNumber(BigDecimal.ZERO);
                    if (CollectionUtils.isNotEmpty(microPickMaterialsList)) {
                        MicroPickMaterials tempEntity = microPickMaterialsList.stream()
                                .filter(temp -> temp.getProductSeq().equals(e.getProductSeq()))
                                .findFirst().orElse(null);
                        if (null != tempEntity) {
                            entity.setPickingNumber(tempEntity.getPickingNumber());
                            entity.setReturnNumber(tempEntity.getReturnNumber());
                        }
                    }
                    entity.setProductSeq(e.getProductSeq());
                    entity.setProductCode(e.getProductCode());
                    entity.setProductName(e.getProductName());
                    entity.setProductType(e.getProductType());
                    entity.setProductionMode(e.getProductionMode());
                    entity.setBomType(microProductBom.getBomType());
                    return entity;
                }).collect(Collectors.toList());
                pickMaterialsDtoList.addAll(pickMaterialsDtoListFromBom);
                // 除去bom外的投料单信息(新增物料)
                if (CollectionUtil.isNotEmpty(microPickMaterialsList)) {
                    List<MicroPickMaterials> pickMaterialsListFromNewAdd = microPickMaterialsList.stream()
                            .filter(obj -> !productSeqList.contains(obj.getProductSeq()))
                            .collect(Collectors.toList());
                    if (CollectionUtil.isNotEmpty(pickMaterialsListFromNewAdd)) {
                        List<MicroPickMaterialsDto> pickMaterialsDtoListFromNewAdd = pickMaterialsListFromNewAdd.stream().map(obj -> {
                            MicroPickMaterialsDto entity = new MicroPickMaterialsDto();
                            entity.setPickingNumber(obj.getPickingNumber());
                            entity.setReturnNumber(obj.getReturnNumber());
                            entity.setProductSeq(obj.getProductSeq());
                            entity.setProductCode(obj.getProductCode());
                            entity.setProductName(obj.getProductName());
                            entity.setProductType(obj.getProductType());
                            entity.setProductionMode(obj.getProductionMode());
                            return entity;
                        }).collect(Collectors.toList());
                        pickMaterialsDtoList.addAll(pickMaterialsDtoListFromNewAdd);
                    }
                }
            }
        } else {
            Map<String, MicroProductBom> bomMap = new HashMap<>(16);
            if (microProductBom != null && CollectionUtil.isNotEmpty(microProductBom.getSecondBomList())) {
                bomMap = microProductBom.getSecondBomList()
                        .stream().collect(Collectors.groupingBy(MicroProductBom::getProductSeq, Collectors.collectingAndThen(Collectors.toList(), v -> v.get(0))));
                result.setBomType(microProductBom.getBomType());
            }
            // 退料展示信息, 展示相关信息就行了
            if (CollectionUtil.isNotEmpty(microPickMaterialsList)) {
                for (MicroPickMaterials pickMaterials : microPickMaterialsList) {
                    MicroPickMaterialsDto entity = new MicroPickMaterialsDto();
                    BeanUtils.copyProperties(pickMaterials, entity);
                    if (bomMap != null && bomMap.get(pickMaterials.getProductSeq()) != null) {
                        entity.setBomType(bomMap.get(pickMaterials.getProductSeq()).getBomType());
                    }
                    pickMaterialsDtoList.add(entity);
                }
            }
        }
        result.setPickMaterialsDtoList(pickMaterialsDtoList);
        return result;
    }

    /**
     * 查询投料单/退料单明细列表
     *
     * @param microPickMaterialsDto 投料单/退料单
     * @return 投料单/退料单
     */
    @Override
    public List<MicroPickMaterialsDto> getList(MicroPickMaterialsDto microPickMaterialsDto) {
        List<MicroPickMaterialsDto> resultList = new ArrayList<>();
        MicroPickMaterials microPickMaterials = new MicroPickMaterials();
        BeanUtils.copyProperties(microPickMaterialsDto, microPickMaterials);
        List<MicroPickMaterials> microPickMaterialsList = microPickMaterialsMapper.selectMicroPickMaterialsList(microPickMaterials);
        resultList = microPickMaterialsList.stream().map(e -> {
            MicroPickMaterialsDto entity = new MicroPickMaterialsDto();
            BeanUtils.copyProperties(e, entity);
            return entity;
        }).collect(Collectors.toList());
        return resultList;
    }

}
