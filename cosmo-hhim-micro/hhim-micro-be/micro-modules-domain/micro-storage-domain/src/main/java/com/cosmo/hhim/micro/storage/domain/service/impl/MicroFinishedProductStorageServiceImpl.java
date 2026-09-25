/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.storage.domain.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.storage.StockWarnFlagEnum;
import com.cosmo.hhim.micro.infrastructure.events.ProductNameChangeEvent;
import com.cosmo.hhim.micro.storage.domain.entity.ChangeFinishedStorageParam;
import com.cosmo.hhim.micro.storage.domain.entity.CountInDiffWarnStatus;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageAdjustParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.entity.WarnFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.WarnStorageParam;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageHistoryMapper;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageMapper;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 成品库存Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Slf4j
@Service
public class MicroFinishedProductStorageServiceImpl implements IMicroFinishedProductStorageService {
    @Autowired
    private MicroFinishedProductStorageMapper microFinishedProductStorageMapper;
    @Autowired
    private MicroFinishedProductStorageHistoryMapper microFinishedProductStorageHistoryMapper;
    @Autowired
    private RedisLockHelper redisLockHelper;

    /**
     * 查询成品库存
     *
     * @return 成品库存
     */
    @Override
    public MicroFinishedProductStorage selectMicroFinishedProductStorageByProductSeq(String productSeq) {
        return microFinishedProductStorageMapper.selectMicroFinishedProductStorageByProductSeq(productSeq);
    }

    /**
     * 查询成品库存列表
     *
     * @param microFinishedProductStorage 成品库存
     * @return 成品库存
     */
    @Override
    public List<MicroFinishedProductStorage> selectMicroFinishedProductStorageList(MicroFinishedProductStorage microFinishedProductStorage) {
        return microFinishedProductStorageMapper.selectMicroFinishedProductStorageList(microFinishedProductStorage);
    }


    /**
     * 新增成品库存
     *
     * @param microFinishedProductStorage 成品库存
     * @return 结果
     */
    @Override
    public int insertMicroFinishedProductStorage(MicroFinishedProductStorage microFinishedProductStorage) {
        return microFinishedProductStorageMapper.insertMicroFinishedProductStorage(microFinishedProductStorage);
    }

    /**
     * 修改成品库存
     *
     * @param param 成品库存
     * @return 结果
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateMicroFinishedProductStorage(MicroFinishProductStorageAdjustParam param) {
        Date nowDate = DateUtils.getNowDate();
        Long userId = SecurityUtils.getUserId();

        // 0.TODO 是否需要负库存校验

        // 按照租户+产品编码加分布式控制锁
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String lockKey = RedisKeys.MicroRegion.FINISH_STORAGE_MODULE.value(RedisKeys.LOCK) + tenantCode + "：" + param.getProductSeq();
        String uuid = UUID.randomUUID().toString();
        try {
            final boolean success = redisLockHelper.lock(lockKey, uuid, 30L, TimeUnit.SECONDS);
            if (!success) {
                // 加锁失败
                throw new CustomException("该产品库存正在处理中，请勿重复操作！");
            }

            // 1.根据库存信息存在与否新增或更新库存信息
            // 查询产成品库中是否存在此产品信息
            MicroFinishedProductStorage finishedProductStorage = this.selectMicroFinishedProductStorageByProductSeq(param.getProductSeq());
            BigDecimal changeFinishNum = BigDecimal.ZERO;
            if (null == finishedProductStorage) {
                changeFinishNum = param.getChangeNum();

                // 新增库存信息
                MicroFinishedProductStorage storage = new MicroFinishedProductStorage();
                storage.setProductSeq(param.getProductSeq());
                storage.setProductName(param.getProductName());
                storage.setNum(param.getChangeNum());
                storage.setCreatedDate(nowDate);
                storage.setCreatedBy(userId.toString());
                this.insertMicroFinishedProductStorage(storage);
            } else {
                changeFinishNum = finishedProductStorage.getNum().add(param.getChangeNum());

                // 修改库存信息
                MicroFinishedProductStorage updateParam = new MicroFinishedProductStorage();
                updateParam.setId(finishedProductStorage.getId());
                updateParam.setNum(changeFinishNum);
                updateParam.setLastUpdDate(nowDate);
                updateParam.setLastUpdBy(userId.toString());
                microFinishedProductStorageMapper.updateMicroFinishedProductStorage(updateParam);
            }


            // 2.新增库存变动记录
            MicroFinishedProductStorageHistory insertParam = new MicroFinishedProductStorageHistory();
            insertParam.setProductSeq(param.getProductSeq());
            insertParam.setProductName(param.getProductName());
            insertParam.setChangeType(param.getChangeType());
            insertParam.setChangeNum(param.getChangeNum());
            insertParam.setFinishChangeNum(changeFinishNum);
            insertParam.setChangeTime(nowDate);
            insertParam.setChangeUser(userId);
            insertParam.setChangeReason(param.getChangeReason());
            insertParam.setCreatedBy(userId.toString());
            insertParam.setCreatedDate(nowDate);
            microFinishedProductStorageHistoryMapper.insertMicroFinishedProductStorageHistory(insertParam);
        } finally {
            // 业务处理完成后不再主动释放锁
            redisLockHelper.unlock(lockKey, uuid);
        }
    }

    /**
     * 调整成品库存
     *
     * @param param
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeBound(ChangeFinishedStorageParam param) {
        List<MicroFinishedProductStorage> storageAddList = param.getStorageAddList();
        List<MicroFinishedProductStorage> storageUpdateList = param.getStorageUpdateList();
        List<MicroFinishedProductStorageHistory> storageHistoryList = param.getStorageHistoryList();

        // 按照租户加分布式控制锁
        String tenantCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String lockKey = RedisKeys.MicroRegion.FINISH_STORAGE_MODULE.value(RedisKeys.LOCK) + tenantCode;
        String uuid = UUID.randomUUID().toString();
        try {
            final boolean success = redisLockHelper.lock(lockKey, uuid, 30L, TimeUnit.SECONDS);
            if (!success) {
                // 加锁失败
                throw new CustomException("出库正在处理中，请勿重复操作！");
            }

            // 1.新增库存信息
            if (!CollectionUtils.isEmpty(storageAddList)) {
                microFinishedProductStorageMapper.insertBatch(storageAddList);
            }

            // 2.修改库存信息
            if (!CollectionUtils.isEmpty(storageUpdateList)) {
                microFinishedProductStorageMapper.updateBatch(storageUpdateList);
            }

            // 3.新增库存变动记录
            if (!CollectionUtils.isEmpty(storageHistoryList)) {
                microFinishedProductStorageHistoryMapper.insertBatch(storageHistoryList);
            }

        } finally {
            // 释放锁
            redisLockHelper.unlock(lockKey, uuid);
        }

    }

    /**
     * 监听产品名称变更
     *
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, value = {ProductNameChangeEvent.class})
    public void productNameChangedListener(ProductNameChangeEvent event) {
        Map<String, String> changeMapData = (Map<String, String>) event.getSource();
        String productSeq = changeMapData.get("productSeq");
        String productName = changeMapData.get("productName");

        MicroFinishedProductStorage updateParam = new MicroFinishedProductStorage();
        updateParam.setProductSeq(productSeq);
        updateParam.setProductName(productName);
        microFinishedProductStorageMapper.updateStorageByProductSeq(updateParam);
    }

    /**
     * 不在安全库存范围内的产品
     *
     * @return
     */
    @Override
    public List<MicroFinishedProductStorage> selectMicroFinishedProductStorageFromOverSafetyStock() {
        return microFinishedProductStorageMapper.selectMicroFinishedProductStorageFromOverSafetyStock();
    }

    /**
     * 库存预警展示列表
     *
     * @param warnStorageParam
     * @return
     */
    @Override
    public List<WarnFinishedProductStorage> selectMicroFinishedProductStorageForWarn(WarnStorageParam warnStorageParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(warnStorageParam));
        List<WarnFinishedProductStorage> warnFinishedProductStorageList = microFinishedProductStorageMapper.selectMicroFinishedProductStorageForWarn(warnStorageParam);
        if (CollectionUtils.isEmpty(warnFinishedProductStorageList)) {
            return Collections.emptyList();
        }
        // 展示全部内容时，需要判断设置警示标示
        if (StringUtils.isEmpty(warnStorageParam.getStockWarnFlag())) {
            for (WarnFinishedProductStorage warnFinishedProductStorage : warnFinishedProductStorageList) {
                // 负库存风险
                if (warnFinishedProductStorage.getStockNum().signum() < 0) {
                    warnFinishedProductStorage.setStockWarnFlag(StockWarnFlagEnum.NEGATIVE_STOCK.getCode());
                } else {
                    // 低于安全库存下限
                    if (warnFinishedProductStorage.getStockLowerLimit() != null
                            && warnFinishedProductStorage.getStockNum().compareTo(warnFinishedProductStorage.getStockLowerLimit()) < 0) {
                        warnFinishedProductStorage.setStockWarnFlag(StockWarnFlagEnum.LOWER_SAFETY_STOCK.getCode());
                    }
                    // 高于安全库存上限
                    if (warnFinishedProductStorage.getStockUpperLimit() != null
                            && warnFinishedProductStorage.getStockNum().compareTo(warnFinishedProductStorage.getStockUpperLimit()) > 0) {
                        warnFinishedProductStorage.setStockWarnFlag(StockWarnFlagEnum.UPPER_SAFETY_STOCK.getCode());
                    }
                }
            }
        }
        return warnFinishedProductStorageList;
    }

    /**
     * 根据物料列表查询库存
     *
     * @param productSeqList
     * @return
     */
    @Override
    public List<MicroFinishedProductStorage> selectMicroFinishedProductStorageByProductSeqList(List<String> productSeqList) {
        return microFinishedProductStorageMapper.selectMicroFinishedProductStorageByProductSeqList(productSeqList);
    }

    /**
     * 预警列表中不同预警装爱
     *
     * @param productNameOrCode
     * @return
     */
    @Override
    public CountInDiffWarnStatus countProductNumInDiffWarnStatus(String productNameOrCode) {
        return microFinishedProductStorageMapper.countProductNumInDiffWarnStatus(productNameOrCode);
    }
}
