/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.material.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.application.assembler.material.MicroMaterialAssembler;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDTO;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisDetailDTO;
import com.cosmo.hhim.micro.application.dto.material.MicroMaterialAnalysisQueryDTO;
import com.cosmo.hhim.micro.application.service.material.IMicroMaterialFacadeService;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderMaterialEntity;
import com.cosmo.hhim.micro.planning.domain.service.work.IMicroManufactureWorkOrderService;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroMaterialFacadeServiceImpl implements IMicroMaterialFacadeService {

    @Autowired
    private IMicroManufactureWorkOrderService workOrderService;
    @Autowired
    private IMicroFinishedProductStorageService productStorageService;

    /**
     * 1.物料需求分析仅针对有标准BOM的产品
     * 2.分析工单为待生产、生产中工单，已关闭工单不参与分析
     * 3.缺料预警：根据投料工单计划生产数量和标准BOM，计算物料的需求情况，与当前库存数进行对比，需求数大于库存数是即为缺料
     * 4.统计范围：工单的计划开始时间与结束时间在选定时间范围内的工单
     * 5.缺料量 = 实际需求数量  -  当前库存数
     * 实际需求数量 = 工单标准用量  -  实际已投  （多个工单的合计）
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<MicroMaterialAnalysisDTO> selectMicroMaterialAnalysisList(MicroMaterialAnalysisQueryDTO queryDTO) {
//        1.先找到对应工单下产品->BOM下各物料实际需求数量
        List<MicroManufactureWorkOrderMaterialEntity> materialList = workOrderService.selectWorkOrderShortageMaterialByStandardBom(
                BeanUtil.copyProperties(queryDTO, MicroManufactureWorkOrder.class)
        );
        if (CollectionUtils.isEmpty(materialList)) {
            return Collections.emptyList();
        }
        Set<String> productSeqSet = materialList.stream().map(MicroManufactureWorkOrderMaterialEntity::getProductSeq).collect(Collectors.toSet());
//        3.各BOM下物料库存数量
        List<MicroFinishedProductStorage> storageList = productStorageService.selectMicroFinishedProductStorageByProductSeqList(new ArrayList<>(productSeqSet));
        this.calcShortageNum(materialList, storageList);
        return MicroMaterialAssembler.materialEntityToDTO(materialList);
    }

    /**
     * 计算各物料短缺数量并按照缺料数量倒序排
     *
     * @param materialList 物料集合
     * @param storageList  物料与现有库存集合
     */
    private void calcShortageNum(List<MicroManufactureWorkOrderMaterialEntity> materialList, List<MicroFinishedProductStorage> storageList) {
        Map<String, BigDecimal> productStorageMap = new HashMap<>(8);
        if (!CollectionUtils.isEmpty(storageList)) {
            storageList.forEach(m -> productStorageMap.merge(m.getProductSeq(), m.getNum(), BigDecimal::add));
        }
        String productSeq;
        BigDecimal demandNum;
        BigDecimal stockNum = BigDecimal.ZERO;
        Iterator<MicroManufactureWorkOrderMaterialEntity> it = materialList.iterator();
        MicroManufactureWorkOrderMaterialEntity material;
        while (it.hasNext()) {
            material = it.next();
            productSeq = material.getProductSeq();
            demandNum = material.getDemandNum();
            if (productStorageMap.containsKey(productSeq)) {
                stockNum = productStorageMap.get(productSeq);
            }
            material.setStockNum(stockNum);
            BigDecimal shortageNum = demandNum.subtract(stockNum).setScale(4, RoundingMode.UP);
            if (shortageNum.signum() <= 0) {
                it.remove();
                continue;
            }
            material.setShortageNum(shortageNum);
        }
        materialList.sort(Comparator.comparing(MicroManufactureWorkOrderMaterialEntity::getShortageNum)
                .thenComparingLong(MicroManufactureWorkOrderMaterialEntity::getProductId)
                .reversed());
    }

    /**
     * 根据产品获取缺料工单详情
     *
     * @param queryDTO
     * @return
     */
    @Override
    public List<MicroMaterialAnalysisDetailDTO> selectAnalysisDetail(MicroMaterialAnalysisQueryDTO queryDTO) {
        validDetailQueryParam(queryDTO);
        List<MicroManufactureWorkOrderMaterialEntity> materialList = workOrderService.selectShortageDetailList(
                BeanUtil.copyProperties(queryDTO, MicroManufactureWorkOrder.class)
        );
        if (CollectionUtils.isEmpty(materialList)) {
            return Collections.emptyList();
        }
//        3.各BOM下物料库存数量
        List<MicroFinishedProductStorage> storageList = productStorageService.selectMicroFinishedProductStorageByProductSeqList(
                Collections.singletonList(queryDTO.getProductSeq())
        );
        return this.calcDetailInfo(materialList, storageList);
    }

    /**
     * 计算明细详情
     *
     * @param materialList
     * @param storageList
     * @return
     */
    private List<MicroMaterialAnalysisDetailDTO> calcDetailInfo(List<MicroManufactureWorkOrderMaterialEntity> materialList, List<MicroFinishedProductStorage> storageList) {
        BigDecimal stockNum;
        if (CollectionUtils.isEmpty(storageList)) {
            stockNum = BigDecimal.ZERO;
        } else {
            stockNum = storageList.get(0).getNum();
        }
        List<MicroMaterialAnalysisDetailDTO> result = new ArrayList<>();
        MicroMaterialAnalysisDetailDTO detailDTO;
        for (MicroManufactureWorkOrderMaterialEntity material : materialList) {
            detailDTO = new MicroMaterialAnalysisDetailDTO();
            if (material.getDemandNum().signum() <= 0) {
                detailDTO.setDemandNum(BigDecimal.ZERO);
            }
            BeanUtil.copyProperties(material, detailDTO);
            stockNum = stockNum.subtract(detailDTO.getDemandNum()).setScale(4, RoundingMode.UP);
            if (stockNum.signum() >= 0) {
                detailDTO.setShortage(false);
            }
            result.add(detailDTO);
        }
        return result;
    }


    private void validDetailQueryParam(MicroMaterialAnalysisQueryDTO queryDTO) {
        if (StringUtils.isEmpty(queryDTO.getProductSeq())) {
            throw new CustomException("无法获取产品信息");
        }
    }

}
