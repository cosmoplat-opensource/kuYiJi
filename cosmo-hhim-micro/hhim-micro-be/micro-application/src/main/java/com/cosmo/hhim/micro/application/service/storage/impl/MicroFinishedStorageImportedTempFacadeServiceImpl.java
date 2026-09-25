/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage.impl;

import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.storage.MicroPlanFinishProductStorage;
import com.cosmo.hhim.micro.application.service.storage.IMicroFinishedStorageImportedTempFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.task.AsyncImportExportTask;
import com.cosmo.hhim.micro.base.domain.mapper.task.AsyncImportExportTaskMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.AsyncImportExportTaskStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ImportConfirmFlagEnum;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageAdjustParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedStorageImportedTemp;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedStorageImportedTempService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.base.domain.util.CertificateUtil.extractTaskCodeFromCertificate;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroFinishedStorageImportedTempFacadeServiceImpl implements IMicroFinishedStorageImportedTempFacadeService {

    private final IMicroFinishedProductStorageService microFinishedProductStorageService;
    private final IMicroProductService microProductService;
    private final IMicroFinishedStorageImportedTempService microFinishedStorageImportedTempService;
    private final AsyncImportExportTaskMapper asyncImportExportTaskMapper;
    private final RedisCache redisCache;

    /**
     * 查询产成品库存导入模版数据
     *
     * @return
     */
    @Override
    public List<MicroPlanFinishProductStorage> selectFinishedStorageImportTemplateInfo() {
        List<MicroPlanFinishProductStorage> resultList = Lists.newArrayList();

        // 查询产成品库存信息
        List<MicroFinishedProductStorage> storageList = microFinishedProductStorageService.selectMicroFinishedProductStorageList(null);

        // 相关产品基础信息赋值
        for (MicroFinishedProductStorage productStorage : storageList) {

            MicroPlanFinishProductStorage result = new MicroPlanFinishProductStorage();
            result.setProductName(productStorage.getProductName());
            result.setNum(productStorage.getNum());
            MicroProduct microProduct = microProductService.selectMicroProductByProductSeq(productStorage.getProductSeq());
            if (null != microProduct) {
                result.setProductCode(microProduct.getProductCode());
                result.setProductName(microProduct.getProductName());
                result.setProductType(microProduct.getProductType());
                result.setUnit(microProduct.getUnit());
                result.setStockLowerLimit(microProduct.getStockLowerLimit());
                result.setStockUpperLimit(microProduct.getStockUpperLimit());
                result.setStandards(microProduct.getStandards());
            }
            resultList.add(result);
        }

        return resultList;
    }

    /**
     * 确认导入
     * flag(0:错误数据修复，1:确认导入)
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importConfirm(String flag) { 
        Long userId = SecurityUtils.getUserId();
        Date nowDate = DateUtils.getNowDate();

        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        // 1.读取当前租户需要导入的数据
        List<MicroFinishedStorageImportedTemp> importList = microFinishedStorageImportedTempService.selectMicroFinishedStorageImportedTempList(null);
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

        // 2.按照产品是否存在分类（新增或更新）
        List<MicroFinishedStorageImportedTemp> addImportDataList = Lists.newArrayList();
        List<MicroFinishedStorageImportedTemp> updateImportDataList = Lists.newArrayList();

        for (MicroFinishedStorageImportedTemp importedData : importList) {
            String productCode = importedData.getProductCode();
            MicroProduct microProduct = microProductService.selectMicroProductByProductCode(productCode);
            if (null != microProduct) {
                updateImportDataList.add(importedData);
            } else {
                addImportDataList.add(importedData);
            }
        }

        // 3.新增处理
        if (!CollectionUtils.isEmpty(addImportDataList)) {
            for (MicroFinishedStorageImportedTemp addData : addImportDataList) {
                // 3.1 新增产品信息
                MicroProduct product = new MicroProduct();
                product.setProductCode(addData.getProductCode());
                product.setProductName(addData.getProductName());
                product.setProductType(addData.getProductType());
                product.setStandards(addData.getStandards());
                product.setStockUpperLimit(addData.getStockUpperLimit());
                product.setStockLowerLimit(addData.getStockLowerLimit());
                product.setUnit(addData.getUnit());
                product.setCreatedBy(userId.toString());
                product.setCreatedDate(nowDate);
                MicroProduct microProduct = microProductService.insertMicroProduct(product);

                // 3.2 新增产成品库存和产成品库存变动历史
                MicroFinishProductStorageAdjustParam adjustParam = new MicroFinishProductStorageAdjustParam();
                adjustParam.setProductSeq(microProduct.getProductSeq());
                adjustParam.setProductName(microProduct.getProductName());
                adjustParam.setChangeNum(addData.getStorageNum());
                adjustParam.setChangeType(FinishStorageChangeTypeEnum.STORAGE_IMPORTED.getCode());
                adjustParam.setChangeReason(addData.getRemark());
                microFinishedProductStorageService.updateMicroFinishedProductStorage(adjustParam);
            }

        }

        // 4.更新处理
        if (!CollectionUtils.isEmpty(updateImportDataList)) {
            for (MicroFinishedStorageImportedTemp updateData : updateImportDataList) {
                // 更新产成品库存（新增产成品库存变动历史）
                MicroProduct microProduct = microProductService.selectMicroProductByProductCode(updateData.getProductCode());
                if (null != microProduct) {
                    MicroFinishedProductStorage finishedProductStorage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(microProduct.getProductSeq());
                    BigDecimal changeNum = updateData.getStorageNum().subtract(finishedProductStorage.getNum());

                    MicroFinishProductStorageAdjustParam adjustParam = new MicroFinishProductStorageAdjustParam();
                    adjustParam.setProductSeq(microProduct.getProductSeq());
                    adjustParam.setProductName(microProduct.getProductName());
                    adjustParam.setChangeNum(changeNum);
                    adjustParam.setChangeType(FinishStorageChangeTypeEnum.STORAGE_IMPORTED.getCode());
                    adjustParam.setChangeReason(updateData.getRemark());
                    microFinishedProductStorageService.updateMicroFinishedProductStorage(adjustParam);
                }
            }
        }

        // 5.删除导入数据临时表
        deleteImportedTemp(importList);

        // 6.更新任务处理状态（已确认）
        AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
        asyncImportExportTask.setTaskCode(taskCode);
        asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CONFIRMED.getCode());
        asyncImportExportTask.setUpdateTime(nowDate);
        asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);

        // 7.删除PC请求临时凭证
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
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        // 1.删除导入数据临时表
        List<MicroFinishedStorageImportedTemp> importList = microFinishedStorageImportedTempService.selectMicroFinishedStorageImportedTempList(null);
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
     * 删除导入数据的临时表
     *
     * @param importList
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteImportedTemp(List<MicroFinishedStorageImportedTemp> importList) {
        List<Long> importedTempIds = importList.stream().map(MicroFinishedStorageImportedTemp::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(importedTempIds)) {
            Long[] ids = importedTempIds.toArray(new Long[importedTempIds.size()]);
            microFinishedStorageImportedTempService.deleteMicroFinishedStorageImportedTempByIds(ids);
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
