/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.tech.impl;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.tech.IMicroTechImportedFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.task.AsyncImportExportTask;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainImportedTemp;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroSaveChainNodeEntity;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroSingleTechChainEntity;
import com.cosmo.hhim.micro.base.domain.mapper.task.AsyncImportExportTaskMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroProcessChainImportedTempService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.AsyncImportExportTaskStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ImportConfirmFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsFirstProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsLastProcessEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.ProcessTypesEnum;
import com.cosmo.hhim.micro.infrastructure.enums.importexport.ProduceModeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.TechPatternEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.base.domain.util.CertificateUtil.extractTaskCodeFromCertificate;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroTechImportedFacadeServiceImpl implements IMicroTechImportedFacadeService {

    private final IMicroProcessChainImportedTempService microProcessChainImportedTempService;
    private final AsyncImportExportTaskMapper asyncImportExportTaskMapper;
    private final RedisCache redisCache;
    private final IMicroProductService microProductService;
    private final IMicroProcessCommonService microProcessCommonService;
    private final IMicroTechnologyService microTechnologyService;

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importConfirm(String flag) {
        Date nowDate = DateUtils.getNowDate();
        Long userId = SecurityUtils.getUserId();

        String taskCode = extractTaskCodeFromCertificate();
        if (!StringUtils.hasText(taskCode)) {
            throw new CustomException("任务编码获取失败！");
        }

        // 1.读取当前租户需要导入的数据
        List<MicroProcessChainImportedTemp> importList = microProcessChainImportedTempService.selectMicroProcessChainImportedTempList(null);
        if (CollectionUtils.isEmpty(importList)) {
            // 更新任务处理状态（已确认）
            AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
            asyncImportExportTask.setTaskCode(taskCode);
            asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CONFIRMED.getCode());
            asyncImportExportTask.setUpdateTime(nowDate);
            asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);

            // 删除PC请求临时凭证
            if (ImportConfirmFlagEnum.IMPORTED.getCode().equals(flag)) {
                deletePcRequestCertificate();
            }
            return;
        }

        // 2.新增产品+工序
        for (MicroProcessChainImportedTemp importedTemp : importList) {
            String productCode = importedTemp.getProductCode();
            // 判断产品是否存在，不存在则新增
            addMicroProduct(importedTemp.getProductName(), productCode);

            // 判读工序是否存在，不存在则新增
            addMicroProcessCommon(importedTemp.getProcessName(), null);
        }

        // 3.新增工艺+工艺链
        addTechAndChain(importList);

        // 4.删除导入数据临时表
        deleteImportedTemp(importList);

        // 5.更新任务处理状态（已确认）
        AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
        asyncImportExportTask.setTaskCode(taskCode);
        asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CONFIRMED.getCode());
        asyncImportExportTask.setUpdateTime(nowDate);
        asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);

        // 6.删除PC请求临时凭证
        if (ImportConfirmFlagEnum.IMPORTED.getCode().equals(flag)) {
            deletePcRequestCertificate();
        }
    }

    /**
     * 取消导入
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCancel() {
        Date nowDate = DateUtils.getNowDate();

        String taskCode = extractTaskCodeFromCertificate();
        if (!StringUtils.hasText(taskCode)) {
            throw new CustomException("任务编码获取失败！");
        }

        // 1.删除导入数据临时表
        List<MicroProcessChainImportedTemp> importList = microProcessChainImportedTempService.selectMicroProcessChainImportedTempList(null);
        if (!CollectionUtils.isEmpty(importList)) {
            deleteImportedTemp(importList);
        }

        // 2.更新任务处理状态（已取消）
        AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
        asyncImportExportTask.setTaskCode(taskCode);
        asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CANCELED.getCode());
        asyncImportExportTask.setUpdateTime(nowDate);
        asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);
    }

    /**
     * 新增工艺和工艺链信息
     *
     * @param importList
     */
    public void addTechAndChain(List<MicroProcessChainImportedTemp> importList) {
        // 按照产品编码分组
        Map<String, List<MicroProcessChainImportedTemp>> groupByProductMap = importList.stream().collect(Collectors.groupingBy(MicroProcessChainImportedTemp::getProductCode));
        for (Map.Entry<String, List<MicroProcessChainImportedTemp>> entry : groupByProductMap.entrySet()) {
            String productCode = entry.getKey();
            List<MicroProcessChainImportedTemp> entryValue = entry.getValue();

            // 1. 封装工艺+工艺链数据
            MicroSingleTechChainEntity techChain = new MicroSingleTechChainEntity();

            // 产品基础信息设置
            MicroProduct microProduct = microProductService.selectMicroProductByProductCode(productCode);
            if (null != microProduct) {
                techChain.setProductId(microProduct.getId());
                techChain.setProductSeq(microProduct.getProductSeq());
                techChain.setProductName(microProduct.getProductName());
            }
            techChain.setTechType(BomAndTechTypeEnum.STANDARD.getCode());

            List<MicroSaveChainNodeEntity> chainNodeEntityList = Lists.newArrayList();
            MicroProcessChainImportedTemp importedTemp = entryValue.get(0);
            switch (ProduceModeEnum.getEnum(importedTemp.getProduceMode())) {

                case SEQUENCE: // 顺序 
                    techChain.setTechPattern(TechPatternEnum.SERIAL.getCode());

                    for (MicroProcessChainImportedTemp chainImportedTemp : entryValue) {
                        MicroProcessCommon microProcessCommon = microProcessCommonService.selectMicroProcessCommonByName(chainImportedTemp.getProcessName());
                        if (null == microProcessCommon) {
                            throw new CustomException("工序名称不存在！");
                        }

                        MicroSaveChainNodeEntity chainNodeEntity = new MicroSaveChainNodeEntity();
                        chainNodeEntity.setProcessId(microProcessCommon.getId());
                        chainNodeEntity.setProcessSeq(microProcessCommon.getProcessSeq());
                        chainNodeEntity.setProcessCode(microProcessCommon.getProcessCode());
                        chainNodeEntity.setProcessName(microProcessCommon.getProcessName());
                        chainNodeEntity.setSort(chainImportedTemp.getSort().intValue());
                        chainNodeEntityList.add(chainNodeEntity);
                    }

                    break;

                case PARALLEL: // 并序 
                    techChain.setTechPattern(TechPatternEnum.UN_ORDER.getCode());

                    for (MicroProcessChainImportedTemp chainImportedTemp : entryValue) {
                        MicroProcessCommon microProcessCommon = microProcessCommonService.selectMicroProcessCommonByName(chainImportedTemp.getProcessName());
                        if (null == microProcessCommon) {
                            throw new CustomException("工序名称不存在！");
                        }

                        MicroSaveChainNodeEntity chainNodeEntity = new MicroSaveChainNodeEntity();
                        chainNodeEntity.setProcessId(microProcessCommon.getId());
                        chainNodeEntity.setProcessSeq(microProcessCommon.getProcessSeq());
                        chainNodeEntity.setProcessCode(microProcessCommon.getProcessCode());
                        chainNodeEntity.setProcessName(microProcessCommon.getProcessName());
                        ProcessTypesEnum processTypesEnum = ProcessTypesEnum.getEnum(chainImportedTemp.getFirstOrLastProcess());
                        if (null != processTypesEnum) {
                            switch (processTypesEnum) {
                                case FIRST_PROCESS:
                                    chainNodeEntity.setIsFirstProcess(IsFirstProcessEnum.YES.getCode());
                                    break;
                                case LAST_PROCESS:
                                    chainNodeEntity.setIsLastProcess(IsLastProcessEnum.YES.getCode());
                                    break;
                                default:
                                    break;
                            }
                        }
                        chainNodeEntityList.add(chainNodeEntity);
                    }

                    break;
                default:
                    break;
            }
            techChain.setProcessChainList(chainNodeEntityList);

            // 2. 判断工艺是否存在，不存在则新增 + 判断工艺链是否存在，存在则更新，不存在新增
            microTechnologyService.saveTechAndChain(techChain);
        }
    }

    /**
     * 新增产品基础信息
     *
     * @param productName
     * @param productCode
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MicroProduct addMicroProduct(String productName, String productCode) {
        MicroProduct microProduct = microProductService.selectMicroProductByProductCode(productCode);
        if (null == microProduct) {
            MicroProduct product = new MicroProduct();
            product.setProductCode(productCode);
            product.setProductName(productName);
            microProduct = microProductService.insertMicroProduct(product);
        }
        return microProduct;
    }

    /**
     * 新增工序基础信息
     *
     * @param processName
     * @param processCode
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public MicroProcessCommon addMicroProcessCommon(String processName, String processCode) {
        MicroProcessCommon microProcessCommon = microProcessCommonService.selectMicroProcessCommonByName(processName);
        if (null == microProcessCommon) {
            MicroProcessCommon processCommon = new MicroProcessCommon();
            processCommon.setProcessCode(processCode);
            processCommon.setProcessName(processName);
            microProcessCommon = microProcessCommonService.insertMicroProcessCommon(processCommon);
        }
        return microProcessCommon;
    }

    /**
     * 删除导入数据的临时表
     *
     * @param importList
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteImportedTemp(List<MicroProcessChainImportedTemp> importList) {
        List<Long> importedTempIds = importList.stream().map(MicroProcessChainImportedTemp::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(importedTempIds)) {
            Long[] ids = importedTempIds.toArray(new Long[importedTempIds.size()]);
            microProcessChainImportedTempService.deleteMicroProcessChainImportedTempByIds(ids);
        }
    }

    /**
     * 删除PC请求临时凭证
     */
    private void deletePcRequestCertificate() {
        String certificate = (String) ThreadContext.get(CommonConstants.CERTIFICATE);
        if (StringUtils.hasText(certificate)) {
            String redisKey = RedisKeys.MicroRegion.BASIC_MODULE.value(RedisKeys.IMPORT_CERTIFICATE_CODE + certificate);
            redisCache.deleteObject(redisKey);
        }
    }

}
