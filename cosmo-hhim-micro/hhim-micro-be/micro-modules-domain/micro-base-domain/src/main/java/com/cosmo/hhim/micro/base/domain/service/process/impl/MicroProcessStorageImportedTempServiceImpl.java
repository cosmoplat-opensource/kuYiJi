/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.process.impl;

import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageImportedTemp;
import com.cosmo.hhim.micro.base.domain.entity.task.AsyncImportExportTask;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageImportedTempMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.task.AsyncImportExportTaskMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessCommonService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageImportedTempService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import com.cosmo.hhim.micro.infrastructure.enums.AsyncImportExportTaskStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.ImportConfirmFlagEnum;
import com.cosmo.hhim.micro.infrastructure.events.StorageImport2TechEvent;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.google.common.collect.HashMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cosmo.hhim.micro.base.domain.util.CertificateUtil.extractTaskCodeFromCertificate;

/**
 * 期初工序库存导入临时Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-04-12
 */
@Slf4j
@Service
public class MicroProcessStorageImportedTempServiceImpl implements IMicroProcessStorageImportedTempService {

    @Autowired
    private MicroProcessStorageImportedTempMapper microProcessStorageImportedTempMapper;
    @Autowired
    private MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    private MicroProductMapper microProductMapper;
    @Autowired
    private MicroProcessCommonMapper microProcessCommonMapper;
    @Autowired
    private IMicroProductService microProductService;
    @Autowired
    private IMicroProcessCommonService microProcessCommonService;
    @Autowired
    private MicroProcessStorageHistoryMapper microProcessStorageHistoryMapper;
    @Autowired
    private AsyncImportExportTaskMapper asyncImportExportTaskMapper;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IMicroProcessStorageService microProcessStorageService;
    @Autowired
    private ApplicationEventPublisher publisher;


    /**
     * 查询期初工序库存导入临时
     *
     * @param id 期初工序库存导入临时ID
     * @return 期初工序库存导入临时
     */
    @Override
    public MicroProcessStorageImportedTemp selectMicroProcessStorageImportedTempById(Long id) {
        return microProcessStorageImportedTempMapper.selectMicroProcessStorageImportedTempById(id);
    }

    /**
     * 查询期初工序库存导入临时列表
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 期初工序库存导入临时
     */
    @Override
    public List<MicroProcessStorageImportedTemp> selectMicroProcessStorageImportedTempList(MicroProcessStorageImportedTemp microProcessStorageImportedTemp) {

        // 1.查询工序库存导入临时表
        List<MicroProcessStorageImportedTemp> resultList = microProcessStorageImportedTempMapper.selectMicroProcessStorageImportedTempList(microProcessStorageImportedTemp);
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }
        // 2.填充当前工序库存的良品数、不良品数
        List<MicroProcessStorageImportedTemp> assembledList = resultList.stream().peek(e -> {
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductCode(e.getProductCode());
            MicroProcessCommon microProcessCommon = microProcessCommonMapper.selectMicroProcessCommonByProcessCode(e.getProcessCode());
            if (null != microProduct && null != microProcessCommon) {
                MicroProcessStorage microProcessStorage = microProcessStorageMapper.selectProcessStorageByProductAndProcess(microProduct.getProductSeq(), microProcessCommon.getProcessSeq());
                if (null != microProcessStorage && null != microProcessStorage.getPassNum()) {
                    e.setPassNum(microProcessStorage.getPassNum());
                }
                if (null != microProcessStorage && null != microProcessStorage.getNgNum()) {
                    e.setNgNum(microProcessStorage.getNgNum());
                }
            }
        }).collect(Collectors.toList());

        return MicroPageUtils.listToPage(resultList, assembledList);
    }

    /**
     * 新增期初工序库存导入临时
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 结果
     */
    @Override
    public int insertMicroProcessStorageImportedTemp(MicroProcessStorageImportedTemp microProcessStorageImportedTemp) {
        return microProcessStorageImportedTempMapper.insertMicroProcessStorageImportedTemp(microProcessStorageImportedTemp);
    }

    /**
     * 修改期初工序库存导入临时
     *
     * @param microProcessStorageImportedTemp 期初工序库存导入临时
     * @return 结果
     */
    @Override
    public int updateMicroProcessStorageImportedTemp(MicroProcessStorageImportedTemp microProcessStorageImportedTemp) {
        return microProcessStorageImportedTempMapper.updateMicroProcessStorageImportedTemp(microProcessStorageImportedTemp);
    }

    /**
     * 批量删除期初工序库存导入临时
     *
     * @param ids 需要删除的期初工序库存导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessStorageImportedTempByIds(Long[] ids) {
        return microProcessStorageImportedTempMapper.deleteMicroProcessStorageImportedTempByIds(ids);
    }

    /**
     * 删除期初工序库存导入临时信息
     *
     * @param id 期初工序库存导入临时ID
     * @return 结果
     */
    @Override
    public int deleteMicroProcessStorageImportedTempById(Long id) {
        return microProcessStorageImportedTempMapper.deleteMicroProcessStorageImportedTempById(id);
    }

    /**
     * 确认导入
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importConfirm(String flag) { 
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        Long userId = SecurityUtils.getUserId();
        Date nowDate = DateUtils.getNowDate();

        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        MicroProcessStorageImportedTempServiceImpl currentProxyObj = (MicroProcessStorageImportedTempServiceImpl) AopContext.currentProxy();

        // 1.读取当前租户需要导入的数据
        List<MicroProcessStorageImportedTemp> importList = microProcessStorageImportedTempMapper.selectMicroProcessStorageImportedTempList(null);
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

        // 2.按照产品+工序是否存在分类（新增或更新）
        List<MicroProcessStorageImportedTemp> addImportDataList = Lists.newArrayList();
        List<MicroProcessStorageImportedTemp> updateImportDataList = Lists.newArrayList();

        for (MicroProcessStorageImportedTemp importedData : importList) {
            String productCode = importedData.getProductCode();
            String processCode = importedData.getProcessCode();

            // 判断产品+工序是否存在
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductCode(productCode);
            MicroProcessCommon microProcessCommon = microProcessCommonMapper.selectMicroProcessCommonByProcessCode(processCode);
            if (null != microProduct && null != microProcessCommon) {
                MicroProcessStorage microProcessStorage = microProcessStorageMapper.selectProcessStorageByProductAndProcess(microProduct.getProductSeq(), microProcessCommon.getProcessSeq());
                if (null == microProcessStorage) {
                    addImportDataList.add(importedData);
                } else {
                    updateImportDataList.add(importedData);
                }
            } else {
                addImportDataList.add(importedData);
            }
        }
        HashMultimap<MicroSelectEntity, MicroSelectEntity> productAndProcessMap = HashMultimap.create();
        // 3.新增处理
        if (!CollectionUtils.isEmpty(addImportDataList)) {
            for (MicroProcessStorageImportedTemp addData : addImportDataList) {
                String productCode = addData.getProductCode();
                String processCode = addData.getProcessCode();

                // 3.1 新增产品信息表
                MicroProduct microProduct = currentProxyObj.addMicroProduct(addData.getProductName(), productCode);

                // 3.2 新增工序信息表
                MicroProcessCommon microProcessCommon = currentProxyObj.addMicroProcessCommon(addData.getProcessName(), processCode);

                // 3.3 新增工序库存和库存变动历史
                currentProxyObj.addProcessStorageAndHistory(userId, nowDate, addData, microProduct, microProcessCommon);

                // 新增到产品工序工艺链对应map
                this.put2TechMap(productAndProcessMap, microProduct, microProcessCommon);
            }
        }

        // 4.更新处理
        if (!CollectionUtils.isEmpty(updateImportDataList)) {
            for (MicroProcessStorageImportedTemp updateData : updateImportDataList) {
                String productCode = updateData.getProductCode();
                String processCode = updateData.getProcessCode();

                // 查询工序库存信息
                MicroProduct microProduct = microProductMapper.selectMicroProductByProductCode(productCode);
                MicroProcessCommon microProcessCommon = microProcessCommonMapper.selectMicroProcessCommonByProcessCode(processCode);
                if (null == microProduct || null == microProcessCommon) {
                    log.error("确认导入发生异常！更新处理时，未找到产品和工序的基础信息！productCode:{}, processCode:{}", productCode, processCode);
                    throw new CustomException("确认导入发生异常！");
                }
                MicroProcessStorageDto queryStorageParam = new MicroProcessStorageDto();
                queryStorageParam.setProductSeq(microProduct.getProductSeq());
                queryStorageParam.setProcessSeq(microProcessCommon.getProcessSeq());
                MicroProcessStorage microProcessStorage = microProcessStorageService.selectMicroProcessStorageByInfo(queryStorageParam);

                // 更新工序库存，并新增库存变动历史
                currentProxyObj.updateProcessStorageAndHistory(userId, nowDate, updateData, microProcessStorage);
            }
        }

        // 5.删除导入数据临时表
        currentProxyObj.deleteImportedTemp(importList);

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
        publishTechEvent(productAndProcessMap);
    }

    /**
     * 推送绑定工艺事件
     *
     * @param productAndProcessMap
     */
    private void publishTechEvent(HashMultimap<MicroSelectEntity, MicroSelectEntity> productAndProcessMap) {
        publisher.publishEvent(new StorageImport2TechEvent<>(productAndProcessMap));
    }

    /**
     * 组装产品和工序对应关系
     *
     * @param productAndProcessMap
     * @param microProduct
     * @param microProcessCommon
     */
    private void put2TechMap(HashMultimap<MicroSelectEntity, MicroSelectEntity> productAndProcessMap, MicroProduct microProduct, MicroProcessCommon microProcessCommon) {
        try {
            MicroSelectEntity productEntity = new MicroSelectEntity();
            MicroSelectEntity processEntity = new MicroSelectEntity();
            productEntity.setItemSeq(microProduct.getProductSeq());
            productEntity.setItemId(microProduct.getId());
            productEntity.setItemCode(microProduct.getProductCode());
            productEntity.setItemName(microProduct.getProductName());
            processEntity.setItemSeq(microProcessCommon.getProcessSeq());
            processEntity.setItemId(microProcessCommon.getId());
            processEntity.setItemCode(microProcessCommon.getProcessCode());
            processEntity.setItemName(microProcessCommon.getProcessName());
            productAndProcessMap.put(productEntity, processEntity);
        } catch (Exception e) {
            log.warn("Failed to put node data to tech map from storage_import:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
        }
    }

    /**
     * 取消导入
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importCancel() {
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        MicroProcessStorageImportedTempServiceImpl currentProxyObj = (MicroProcessStorageImportedTempServiceImpl) AopContext.currentProxy();
        Date nowDate = DateUtils.getNowDate();

        String taskCode = extractTaskCodeFromCertificate();
        if(!StringUtils.hasText(taskCode)){
            throw new CustomException("任务编码获取失败！");
        }

        // 1.删除导入数据临时表
        List<MicroProcessStorageImportedTemp> importList = microProcessStorageImportedTempMapper.selectMicroProcessStorageImportedTempList(null);
        if (!CollectionUtils.isEmpty(importList)) {
            currentProxyObj.deleteImportedTemp(importList);
        }

        // 2.更新任务处理状态（已取消）
        AsyncImportExportTask asyncImportExportTask = new AsyncImportExportTask();
        asyncImportExportTask.setTaskCode(taskCode);
        asyncImportExportTask.setTaskStatus(AsyncImportExportTaskStatusEnum.CANCELED.getCode());
        asyncImportExportTask.setUpdateTime(nowDate);
        asyncImportExportTaskMapper.updateAsyncImportExportTask(asyncImportExportTask);
    }

    /**
     * 更新工序库存，并新增库存变动历史
     *
     * @param userId
     * @param nowDate
     * @param updateData
     * @param microProcessStorage
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProcessStorageAndHistory(Long userId, Date nowDate, MicroProcessStorageImportedTemp updateData, MicroProcessStorage microProcessStorage) {
        String processSeq = microProcessStorage.getProcessSeq();
        String productSeq = microProcessStorage.getProductSeq();

        // 1. 更新工序库存
        MicroProcessStorage updateStorageParam = new MicroProcessStorage();
        updateStorageParam.setProductSeq(productSeq);
        updateStorageParam.setProcessSeq(processSeq);
        updateStorageParam.setLastUpdBy(userId.toString());
        updateStorageParam.setLastUpdDate(nowDate);
        updateStorageParam.setPassNum(updateData.getPassNumAdjusted());
        updateStorageParam.setNgNum(updateData.getNgNumAdjusted());
        microProcessStorageMapper.updateProcessStorageByProductAndProcess(updateStorageParam);

        // 2. 新增工序库存变动历史表
        MicroProcessStorageHistory insertStorageParam = new MicroProcessStorageHistory();
        insertStorageParam.setProductSeq(productSeq);
        insertStorageParam.setProductCode(microProcessStorage.getProductCode());
        insertStorageParam.setProductName(microProcessStorage.getProductName());
        insertStorageParam.setProcessSeq(processSeq);
        insertStorageParam.setProcessCode(microProcessStorage.getProcessCode());
        insertStorageParam.setProcessName(microProcessStorage.getProcessName());
        insertStorageParam.setOperateNode(CommonConstants.STORAGE_PROCESS_IMPORT);
        insertStorageParam.setPassFromNum(microProcessStorage.getPassNum());
        insertStorageParam.setPassToNum(updateStorageParam.getPassNum());
        insertStorageParam.setNgFromNum(microProcessStorage.getNgNum());
        insertStorageParam.setNgToNum(updateStorageParam.getNgNum());
        insertStorageParam.setCreatedBy(userId.toString());
        insertStorageParam.setCreatedDate(nowDate);
        insertStorageParam.setRemark(updateData.getRemark());
        microProcessStorageHistoryMapper.insertMicroProcessStorageHistory(insertStorageParam);
    }

    /**
     * 新增工序库存和库存变动历史
     *
     * @param userId
     * @param nowDate
     * @param addData
     * @param microProduct
     * @param microProcessCommon
     */
    @Transactional(rollbackFor = Exception.class)
    public void addProcessStorageAndHistory(Long userId, Date nowDate, MicroProcessStorageImportedTemp addData,
                                            MicroProduct microProduct, MicroProcessCommon microProcessCommon) {
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        String productSeq = microProduct.getProductSeq();
        String processSeq = microProcessCommon.getProcessSeq();

        // 1.新增工序库存
        MicroProcessStorage microProcessStorage = new MicroProcessStorage();
        microProcessStorage.setProductSeq(productSeq);
        microProcessStorage.setProcessSeq(processSeq);
        microProcessStorage.setPassNum(addData.getPassNumAdjusted());
        microProcessStorage.setNgNum(addData.getNgNumAdjusted());
        microProcessStorage.setCreatedBy(userId.toString());
        microProcessStorage.setCreatedDate(nowDate);
        microProcessStorage.setTenantCode((String) ThreadContext.get(Constants.TARGET_CUSTOMER));
        microProcessStorageMapper.insertMicroProcessStorage(microProcessStorage);

        // 2.新增工序库存变动历史表
        MicroProcessStorageHistory insertStorageParam = new MicroProcessStorageHistory();
        insertStorageParam.setProductSeq(productSeq);
        insertStorageParam.setProductCode(microProduct.getProductCode());
        insertStorageParam.setProductName(microProduct.getProductName());
        insertStorageParam.setProcessSeq(processSeq);
        insertStorageParam.setProcessCode(microProcessCommon.getProcessCode());
        insertStorageParam.setProcessName(microProcessCommon.getProcessName());
        insertStorageParam.setOperateNode(CommonConstants.STORAGE_PROCESS_IMPORT);
        insertStorageParam.setPassFromNum(BigDecimal.ZERO);
        insertStorageParam.setPassToNum(addData.getPassNumAdjusted());
        insertStorageParam.setNgFromNum(BigDecimal.ZERO);
        insertStorageParam.setNgToNum(addData.getNgNumAdjusted());
        insertStorageParam.setCreatedBy(userId.toString());
        insertStorageParam.setCreatedDate(nowDate);
        insertStorageParam.setRemark(addData.getRemark());
        microProcessStorageHistoryMapper.insertMicroProcessStorageHistory(insertStorageParam);
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
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        MicroProcessCommon microProcessCommon = microProcessCommonMapper.selectMicroProcessCommonByProcessCode(processCode);
        if (null == microProcessCommon) {
            MicroProcessCommon processCommon = new MicroProcessCommon();
            processCommon.setProcessCode(processCode);
            processCommon.setProcessName(processName);
            microProcessCommon = microProcessCommonService.insertMicroProcessCommon(processCommon);
        }
        return microProcessCommon;
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
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());
        MicroProduct microProduct = microProductMapper.selectMicroProductByProductCode(productCode);
        if (null == microProduct) {
            MicroProduct product = new MicroProduct();
            product.setProductCode(productCode);
            product.setProductName(productName);
            microProduct = microProductService.insertMicroProduct(product);
        }
        return microProduct;
    }

    /**
     * 删除导入数据的临时表
     *
     * @param importList
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteImportedTemp(List<MicroProcessStorageImportedTemp> importList) {
        log.info("transactionName:{}", TransactionSynchronizationManager.getCurrentTransactionName());

        List<Long> importedTempIds = importList.stream().map(MicroProcessStorageImportedTemp::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(importedTempIds)) {
            Long[] ids = importedTempIds.toArray(new Long[importedTempIds.size()]);
            microProcessStorageImportedTempMapper.deleteMicroProcessStorageImportedTempByIds(ids);
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
