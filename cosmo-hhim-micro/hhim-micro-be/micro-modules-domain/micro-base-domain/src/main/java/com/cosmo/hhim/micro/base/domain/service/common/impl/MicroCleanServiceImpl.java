/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.micro.base.domain.entity.clean.DuplicatedProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessChain;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroCleanMapper;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroProcessChainMapper;
import com.cosmo.hhim.micro.base.domain.mapper.tech.MicroTechnologyMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroCleanService;
import com.cosmo.hhim.micro.base.domain.service.tech.IMicroTechnologyService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description: 数据清理接口实现
 * @date 2023/1/13 16:00
 */
@Service
@Slf4j
public class MicroCleanServiceImpl implements IMicroCleanService {

    @Autowired
    MicroCleanMapper microCleanMapper;
    @Autowired
    MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    @Autowired
    MicroProcessStorageMapper microProcessStorageMapper;
    @Autowired
    MicroProductMapper microProductMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cleanDuplicatedProduct() { 

        // 获取重复产品
        List<DuplicatedProduct> duplicatedProducts = microCleanMapper.obtainedDuplicatedProduct();

        for (DuplicatedProduct duplicatedProduct : duplicatedProducts) {

            List<MicroWorkSubmit> workSubmitListForUpdate = new ArrayList<>();
            List<MicroWorkSubmitHistory> workSubmitHistoryListForUpdate = new ArrayList<>();
            List<MicroProcessStorageHistory> storageHistoryListForUpdate = new ArrayList<>();
            List<MicroProcessStorage> storageListForUpdate = new ArrayList<>();
            List<Long> storageListForDelete = new ArrayList<>();

            // 选中一个productSeq来进行更新
            List<String> duplicatedProductSeqList = duplicatedProduct.getDuplicatedProductSeqList();
            List<String> duplicatedProductCodeList = duplicatedProduct.getDuplicatedProductCodeList();
            String chooseProductSeq = duplicatedProductSeqList.get(0);
            String chooseProductCode = duplicatedProductCodeList.get(0);

            /** ********************
             *  更新报工表 - 产品序列码
             * *********************/
            List<MicroWorkSubmit> workSubmits = microCleanMapper.obtainedWorkSubmitListByProductSeq(duplicatedProductSeqList);
            for (MicroWorkSubmit obj : workSubmits) {
                if (!obj.getProductSeq().equals(chooseProductSeq)) {
                    obj.setProductSeq(chooseProductSeq);

                    MicroWorkSubmit submitTemp = new MicroWorkSubmit();
                    submitTemp.setId(obj.getId());
                    submitTemp.setProductSeq(obj.getProductSeq());
                    workSubmitListForUpdate.add(submitTemp);
                }
            }
            log.info("要更新的报工记录信息为:{}", JSONObject.toJSONString(workSubmitListForUpdate));

            /** ****************************
             *  更新报工历史表 - 产品序列码和编码
             * *****************************/
            List<MicroWorkSubmitHistory> workSubmitHistories = microCleanMapper.obtainedWorkSubmitHistoryListByProductSeq(duplicatedProductSeqList);
            for (MicroWorkSubmitHistory obj : workSubmitHistories) {
                if (!obj.getProductSeq().equals(chooseProductSeq)) {
                    obj.setProductSeq(chooseProductSeq);
                    obj.setProductCode(chooseProductCode);

                    MicroWorkSubmitHistory submitHistoryTemp = new MicroWorkSubmitHistory();
                    submitHistoryTemp.setId(obj.getId());
                    submitHistoryTemp.setProductCode(obj.getProductCode());
                    submitHistoryTemp.setProductSeq(obj.getProductSeq());
                    workSubmitHistoryListForUpdate.add(submitHistoryTemp);
                }
            }
            log.info("要更新的报工历史记录信息为:{}", JSONObject.toJSONString(workSubmitHistoryListForUpdate));

            /** **************
             *  更新库存变动历史表
             * ***************/
            List<MicroProcessStorageHistory> processStorageHistories = microCleanMapper.obtainedProcessStorageHistoryListByProductSeq(duplicatedProductSeqList);
            processStorageHistories.forEach(obj -> {
                if (!obj.getProductSeq().equals(chooseProductSeq)) {
                    obj.setProductSeq(chooseProductSeq);
                    obj.setProductCode(chooseProductCode);
                }
            });
            // 第二次循环想要重置变动历史记录中的数量
            Map<String, List<MicroProcessStorageHistory>> collect = processStorageHistories.stream().collect(Collectors.groupingBy(MicroProcessStorageHistory::getProcessSeq));
            for (String s : collect.keySet()) {
                List<MicroProcessStorageHistory> processStorageHistoriesTemp = collect.get(s);

                if (processStorageHistoriesTemp.size() > 1) {
                    // 按照创建时间和id进行排序,升序
                    processStorageHistoriesTemp.sort(Comparator.comparing(MicroProcessStorageHistory::getId));
                    // 时间窗口
                    for (int i = 1; i < processStorageHistoriesTemp.size(); i++) {
                        MicroProcessStorageHistory firstProcessStorageHistory = processStorageHistoriesTemp.get(i - 1);
                        MicroProcessStorageHistory secondProcessStorageHistory = processStorageHistoriesTemp.get(i);

                        // 第二次如果是报工、工序流转或者撤销
                        if (secondProcessStorageHistory.getOperateNode().equals(CommonConstants.STORAGE_SUBMIT_INBOUND)
                                || secondProcessStorageHistory.getOperateNode().equals(CommonConstants.STORAGE_PROCESS_FLOW_MODIFY)
                                || secondProcessStorageHistory.getOperateNode().equals(CommonConstants.STORAGE_UNDO_FALLBACK)) {
                            BigDecimal passNumTemp = secondProcessStorageHistory.getPassToNum().subtract(secondProcessStorageHistory.getPassFromNum());
                            BigDecimal ngNumTemp = secondProcessStorageHistory.getNgToNum().subtract(secondProcessStorageHistory.getNgFromNum());

                            // 良品数
                            secondProcessStorageHistory.setPassFromNum(firstProcessStorageHistory.getPassToNum());
                            secondProcessStorageHistory.setPassToNum(firstProcessStorageHistory.getPassToNum().add(passNumTemp));
                            // 不良品数
                            secondProcessStorageHistory.setNgFromNum(firstProcessStorageHistory.getNgToNum());
                            secondProcessStorageHistory.setNgToNum(firstProcessStorageHistory.getNgToNum().add(ngNumTemp));
                        }
                    }
                }
            }
            for (MicroProcessStorageHistory obj : processStorageHistories) {
                MicroProcessStorageHistory processStorageHistoryTemp = new MicroProcessStorageHistory();
                processStorageHistoryTemp.setId(obj.getId());
                processStorageHistoryTemp.setProductSeq(chooseProductSeq);
                processStorageHistoryTemp.setProductCode(chooseProductCode);
                processStorageHistoryTemp.setPassToNum(obj.getPassToNum());
                processStorageHistoryTemp.setPassFromNum(obj.getPassFromNum());
                processStorageHistoryTemp.setNgFromNum(obj.getNgFromNum());
                processStorageHistoryTemp.setNgToNum(obj.getNgToNum());
                storageHistoryListForUpdate.add(processStorageHistoryTemp);
            }
            log.info("要更新的库存变动历史数据为:{}", JSONObject.toJSONString(storageHistoryListForUpdate));

            /** **************
             *   更新并删除库存表
             * ***************/
            List<MicroProcessStorage> microProcessStorages = microCleanMapper.obtainedProcessStorageListByProductSeq(duplicatedProductSeqList);
            microProcessStorages.forEach(obj -> {
                if (!obj.getProductSeq().equals(chooseProductSeq)) {
                    obj.setProductSeq(chooseProductSeq);
                }
            });

            Map<String, List<MicroProcessStorage>> storageByProcess = microProcessStorages.stream().collect(Collectors.groupingBy(MicroProcessStorage::getProcessSeq));
            for (String s : storageByProcess.keySet()) {
                List<MicroProcessStorage> microProcessStorageList = storageByProcess.get(s);

                BigDecimal passNum = microProcessStorageList.stream().map(MicroProcessStorage::getPassNum).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal ngNum = microProcessStorageList.stream().map(MicroProcessStorage::getNgNum).reduce(BigDecimal.ZERO, BigDecimal::add);

                for (int i = 0; i < microProcessStorageList.size(); i++) {
                    // 取产品 + 工序的第一个作为真实库存，其余删除掉
                    if (i == 0) {
                        MicroProcessStorage processStorageUpdate = new MicroProcessStorage();
                        processStorageUpdate.setPassNum(passNum);
                        processStorageUpdate.setNgNum(ngNum);
                        processStorageUpdate.setId(microProcessStorageList.get(i).getId());
                        processStorageUpdate.setProductSeq(microProcessStorageList.get(i).getProductSeq());
                        storageListForUpdate.add(processStorageUpdate);
                    } else {
                        storageListForDelete.add(microProcessStorageList.get(i).getId());
                    }
                }
            }

            log.info("要删除的库存数据id为:{}", storageListForDelete.toString());
            log.info("要更新的库存数据为:{}", JSONObject.toJSONString(storageListForUpdate));

            /** **************
             *   删除产品表
             * ***************/

            List<String> productForDelete = duplicatedProductSeqList.stream().filter(obj -> !obj.equals(chooseProductSeq))
                    .collect(Collectors.toList());
            log.info("要删除的产品序列码为:{}", productForDelete.toString());

            /** **************
             *   数据库操作
             * ***************/
            if (!CollectionUtils.isEmpty(workSubmitListForUpdate)) {
                microCleanMapper.updateMicroWorkSubmitBatch(workSubmitListForUpdate);
            }

            if (!CollectionUtils.isEmpty(workSubmitHistoryListForUpdate)) {
                for (MicroWorkSubmitHistory microWorkSubmitHistory : workSubmitHistoryListForUpdate) {
                    microCleanMapper.updateMicroWorkSubmitHistory(microWorkSubmitHistory);
                }
            }

            if (!CollectionUtils.isEmpty(storageHistoryListForUpdate)) {
                for (MicroProcessStorageHistory processStorageHistory : storageHistoryListForUpdate) {
                    microCleanMapper.updateMicroProcessStorageHistory(processStorageHistory);
                }
            }

            if (!CollectionUtils.isEmpty(storageListForDelete)) {
                Long[] ids = storageListForDelete.stream().toArray(Long[]::new);
                microCleanMapper.deleteMicroProcessStorageByIds(ids);
            }

            if (!CollectionUtils.isEmpty(storageListForUpdate)) {
                microCleanMapper.updateMicroProcessStorageBatch(storageListForUpdate);
            }

            if (!CollectionUtils.isEmpty(productForDelete)) {
                String[] productSeqList = productForDelete.stream().toArray(String[]::new);
                microCleanMapper.deleteMicroProductByProductSeqList(productSeqList);
            }
        }
        return true;
    }

    @Autowired
    private IMicroTechnologyService technologyService;
    @Autowired
    private MicroTechnologyMapper technologyMapper;
    @Autowired
    private MicroProcessChainMapper processChainMapper;

    /**
     * 初始化工艺链顺序
     *
     * @param customers
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean initSortChain(List<String> customers) {
        List<MicroProcessChainBindEntity> bindEntityList = technologyMapper.selectMicroTechChainByProductIdsOrSeqs(null, null, null);
        Map<String, List<MicroProcessChainBindEntity>> collect = bindEntityList.stream().collect(Collectors.groupingBy(key -> key.getProductId() + "&&" + key.getTechType()));
        for (Map.Entry<String, List<MicroProcessChainBindEntity>> entry : collect.entrySet()) {
            List<MicroProcessChainBindEntity> value = entry.getValue();
            value = technologyService.sortChainByParallelProcess(value);
            MicroProcessChain chain;
            for (MicroProcessChainBindEntity entity : value) {
                chain = new MicroProcessChain();
                chain.setId(entity.getId());
                chain.setSort(entity.getSort());
                try {
                    processChainMapper.updateMicroProcessChain(chain);
                } catch (Exception e) {
                    log.error("Failed to update process chain:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
                    log.warn(chain.toString());
                    log.warn(JSONObject.toJSONString(value));
                }
            }
        }
        return true;
    }
}
