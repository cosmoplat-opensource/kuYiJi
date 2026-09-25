/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.micro.application.assembler.tech.MicroTechnologyAssembler;
import com.cosmo.hhim.micro.application.service.storage.IMicroStorageFacadeService;
import com.cosmo.hhim.micro.application.util.TechChainUtils;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.enums.IsFirstProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TechPatternEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroStorageFacadeServiceImpl implements IMicroStorageFacadeService {

    @Autowired
    private IMicroProcessStorageService microProcessStorageService;
    @Autowired
    private IMicroTechnologyService microTechnologyService;
    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;

    /**
     * 工序库存列表
     */
    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageListBySeq(String seqKey, String productSeq, String processSeq) {

        List<MicroProcessStorage> processStorageList = microProcessStorageService.selectMicroProcessStorageListBySeq(seqKey, productSeq, processSeq);
        if (CollectionUtils.isEmpty(processStorageList)) {
            return Collections.emptyList();
        }
        List<String> productSeqList = processStorageList.stream().map(MicroProcessStorage::getProductSeq).distinct().collect(Collectors.toList());
        List<MicroProcessChainBindEntity> chainBindEntityList = microTechnologyService.selectMicroTechChainByProductIdsOrSeqs(null, productSeqList, BomAndTechTypeEnum.STANDARD.getCode());
        if (CollectionUtils.isEmpty(chainBindEntityList)) {
            return processStorageList;
        }

        // 不为空，则根据工艺中的工序进行设置
        Map<Long, List<MicroProcessChainBindEntity>> map = chainBindEntityList.stream().collect(Collectors.groupingBy(MicroProcessChainBindEntity::getProductId));
        for (MicroProcessStorage storage : processStorageList) {
            List<MicroProcessChainBindEntity> chainBindEntityListTemp = map.get(storage.getProductId());
            if (!CollectionUtils.isEmpty(chainBindEntityListTemp)) {
                List<MicroProcessChainBindEntity> chainBindEntityListTempByProcess = chainBindEntityListTemp.stream().filter(obj -> obj.getProcessSeq().equals(storage.getProcessSeq()))
                        .collect(Collectors.toList());
                // 可能存在并序的情况, 就存在多个工序
                if (!CollectionUtils.isEmpty(chainBindEntityListTempByProcess)) {
                    MicroProcessChainBindEntity bindEntity = chainBindEntityListTempByProcess.get(0);
                    storage.setIsLastProcess(bindEntity.getIsLastProcess());
                    // 首序
                    if (CommonConstants.YES.equals(bindEntity.getParentProcessSeq())) {
                        storage.setIsFirstProcess(CommonConstants.YES);
                    } else {
                        storage.setIsFirstProcess(CommonConstants.NO);
                    }
                }
            }
        }
        return processStorageList;
    }

    /**
     * 产品库存查询 （产品 + 工序）
     */
    @Override
    public List<MicroProcessStorage> selectMicroProcessStorageListByCondition(MicroProcessStorageDto microProcessStorageDto) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(microProcessStorageDto));
        List<MicroProcessStorage> microProcessStorageList = microProcessStorageService.selectMicroProcessStorageList(microProcessStorageDto);
        if (CollectionUtils.isEmpty(microProcessStorageList)) {
            return microProcessStorageList;
        }

        // 查询是否有工艺链
        List<MicroProcessChainBindEntity> chainBindEntityList = microTechnologyService.selectMicroTechChain(MicroTechnologyAssembler
                .toTechQuery(ThreadContext.get(Constants.TARGET_CUSTOMER).toString(),
                        null,
                        microProcessStorageDto.getProductSeq()));
        // 没有标准工艺 直接返回
        if (CollectionUtils.isEmpty(chainBindEntityList)) {
            this.setFirstOrLastFlagInfo(microProcessStorageList);
            return microProcessStorageList;
        } else {
            int techPattern = chainBindEntityList.get(0).getTechPattern();
            // 存储最终的结果
            List<MicroProcessStorage> cacheChain;
            // 判断标准工艺中是顺序还是乱序
            if (techPattern == TechPatternEnum.SERIAL.getCode()) {
                cacheChain = this.storageSortBySequentialStandardTech(chainBindEntityList, microProcessStorageList);
            } else {
                cacheChain = this.storageSortByNonSequentialSStandardTech(chainBindEntityList, microProcessStorageList);
            }
            return cacheChain;
        }
    }

    /**
     * 排序
     */
    @Deprecated
    public void sortByStandardTech(List<MicroProcessChainBindEntity> currentNodeList,
                                   List<MicroProcessStorage> microProcessStorageList,
                                   List<MicroProcessChainBindEntity> chainBindEntityList,
                                   List<MicroProcessStorage> cacheChain) {
        for (MicroProcessChainBindEntity currentNode : currentNodeList) {
            microProcessStorageList.forEach(obj -> {
                if (currentNode.getProcessSeq().contains(obj.getProcessSeq())) {
                    obj.setIsLastProcess(currentNode.getIsLastProcess());
                    // 首序
                    if (!StringUtils.isEmpty(currentNode.getParentProcessSeq()) && CommonConstants.ROOT_PROCESS_SEQ.equals(currentNode.getParentProcessSeq())) {
                        obj.setIsFirstProcess(CommonConstants.YES);
                    } else {
                        obj.setIsFirstProcess(CommonConstants.NO);
                    }
                    cacheChain.add(obj);
                }
            });
        }

        // 找到当前节点的上一层节点
        List<MicroProcessChainBindEntity> nextNodeList = new ArrayList<>();
        for (MicroProcessChainBindEntity microProcessChainBind : chainBindEntityList) {
            for (MicroProcessChainBindEntity currentNode : currentNodeList) {
                if (!CommonConstants.ROOT_PROCESS_SEQ.equals(currentNode.getParentProcessSeq()) && currentNode.getParentProcessSeq().contains(microProcessChainBind.getProcessSeq())) {
                    nextNodeList.add(microProcessChainBind);
                }
            }
        }

        if (!CollectionUtils.isEmpty(nextNodeList)) {
            // 并序情况，nextNodeList去重，如果有工序相同的则合并前工序为一个字段
            nextNodeList = TechChainUtils.mergeNextNodeList(nextNodeList);
            sortByStandardTech(nextNodeList, microProcessStorageList, chainBindEntityList, cacheChain);
        }
    }

    /**
     * 库存按照标准工艺的顺序进行展示，标准工艺之外的放在后面
     */
    public List<MicroProcessStorage> storageSortBySequentialStandardTech(List<MicroProcessChainBindEntity> chainBindEntityList, List<MicroProcessStorage> microProcessStorageList) {
        // 存储排好序的结果
        LinkedList<MicroProcessStorage> cacheChain = new LinkedList<>();
        // 过滤掉并序的多条记录, 需要再次排序一下
        List<MicroProcessChainBindEntity> sortTech = chainBindEntityList.stream().collect(Collectors
                .collectingAndThen(Collectors.toCollection(() ->
                        new TreeSet<>(Comparator.comparing(MicroProcessChainBindEntity::getProcessSeq))), ArrayList::new))
                .stream().sorted(Comparator.comparing(MicroProcessChainBindEntity::getSort)).collect(Collectors.toList());

        for (MicroProcessChainBindEntity tech : sortTech) {
            for (MicroProcessStorage microProcessStorage : microProcessStorageList) {
                if (tech.getProcessSeq().equals(microProcessStorage.getProcessSeq())) {
                    // 设置符合标准工艺的工序库存，根据标准工艺的首尾序信息
                    microProcessStorage.setIsLastProcess(tech.getIsLastProcess());
                    // 首序
                    if (!StringUtils.isEmpty(tech.getParentProcessSeq()) && CommonConstants.ROOT_PROCESS_SEQ.equals(tech.getParentProcessSeq())) {
                        microProcessStorage.setIsFirstProcess(CommonConstants.YES);
                    } else {
                        microProcessStorage.setIsFirstProcess(CommonConstants.NO);
                    }
                    cacheChain.add(microProcessStorage);
                }
            }
        }

        // 标准工艺之外的工序库存
        List<MicroProcessStorage> resultExcludeTech = this.notBelongToStandardTechStorage(cacheChain, microProcessStorageList);
        cacheChain.addAll(resultExcludeTech);

        return cacheChain;
    }

    /**
     * 库存按照标准工艺的乱序进行展示（主要是确定首序和尾序），标准工艺之外的放在后面
     */
    public List<MicroProcessStorage> storageSortByNonSequentialSStandardTech(List<MicroProcessChainBindEntity> chainBindEntityList, List<MicroProcessStorage> microProcessStorageList) { 
        // 存储排好序的结果
        LinkedList<MicroProcessStorage> cacheChain = new LinkedList<>();

        // 顺序是排好的, 只需要确定首序和尾序, 找到中间工序 (前端做了控制，至少有首尾序两个工序)

        // 中间序
        List<MicroProcessChainBindEntity> middleProcessChainList = chainBindEntityList.subList(1, chainBindEntityList.size() - 1);
        if (!CollectionUtils.isEmpty(middleProcessChainList)) {
            for (MicroProcessChainBindEntity chainBindEntity : middleProcessChainList) {
                for (MicroProcessStorage storage : microProcessStorageList) {
                    if (storage.getProcessSeq().equals(chainBindEntity.getProcessSeq())) {
                        cacheChain.add(storage);
                    }
                }
            }
        }
        // 设置中间序的首尾序，与报工记录有关
        this.setFirstOrLastFlagInfo(cacheChain);

        // 首序
        MicroProcessChainBindEntity firstProcess = chainBindEntityList.get(0);
        for (MicroProcessStorage storage : microProcessStorageList) {
            if (storage.getProcessSeq().equals(firstProcess.getProcessSeq())) {
                storage.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
                storage.setIsLastProcess(firstProcess.getIsLastProcess());
                cacheChain.addFirst(storage);
            }
        }

        // 尾序
        MicroProcessChainBindEntity lastProcess = chainBindEntityList.get(chainBindEntityList.size() - 1);
        for (MicroProcessStorage storage : microProcessStorageList) {
            if (storage.getProcessSeq().equals(lastProcess.getProcessSeq())) {
                storage.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
                storage.setIsLastProcess(lastProcess.getIsLastProcess());
                cacheChain.add(storage);
            }
        }

        // 标准工艺之外的工序库存
        List<MicroProcessStorage> resultExcludeTech = this.notBelongToStandardTechStorage(cacheChain, microProcessStorageList);
        cacheChain.addAll(resultExcludeTech);

        return cacheChain;
    }

    /**
     * 标准工艺之外的工序库存
     */
    public List<MicroProcessStorage> notBelongToStandardTechStorage(List<MicroProcessStorage> cacheChain, List<MicroProcessStorage> microProcessStorageList) {
        // 针对没有工艺链中不存在工序的放在尾巴后面, 剔除processSeq相同
        Set<String> processSeqFromCraft = cacheChain.stream().map(MicroProcessStorage::getProcessSeq).collect(Collectors.toSet());
        List<MicroProcessStorage> resultExcludeTech = microProcessStorageList.stream().filter(obj -> !processSeqFromCraft.contains(obj.getProcessSeq())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(resultExcludeTech)) {
            // 从报工记录中设置首尾序信息
            this.setFirstOrLastFlagInfo(resultExcludeTech);
        }
        return resultExcludeTech;
    }

    /**
     * 非标准工艺下的工序 根据报工记录设置首尾序信息
     */
    public void setFirstOrLastFlagInfo(List<MicroProcessStorage> microProcessStorageList) {
        microProcessStorageList.forEach(obj -> {
            FirstOrLastProcess firstOrLastProcessFlag = microWorkSubmitService.getFirstOrLastProcessFlagByRecords(obj.getProductSeq(), obj.getProcessSeq());
            if (firstOrLastProcessFlag == null) {
                obj.setIsLastProcess(IsLastProcessEnum.NO.getCode());
                obj.setIsFirstProcess(IsFirstProcessEnum.NO.getCode());
            } else {
                obj.setIsLastProcess(firstOrLastProcessFlag.getIsLastProcess());
                obj.setIsFirstProcess(firstOrLastProcessFlag.getIsFirstProcess());
            }
        });
    }
}
