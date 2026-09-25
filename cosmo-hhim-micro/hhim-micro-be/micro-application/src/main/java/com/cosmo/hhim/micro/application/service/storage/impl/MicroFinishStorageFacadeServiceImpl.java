/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.storage.impl;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.storage.FinishStorageChangeHistoryGpByDayResult;
import com.cosmo.hhim.micro.application.dto.storage.MicroPlanFinishProductStorage;
import com.cosmo.hhim.micro.application.service.storage.IMicroFinishStorageFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.storage.StockWarnFlagEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroEmailUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import com.cosmo.hhim.micro.storage.domain.entity.ChangeFinishedStorageParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageBoundBatchParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageBoundParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageHistoryService;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroFinishStorageFacadeServiceImpl implements IMicroFinishStorageFacadeService {

    private final IMicroFinishedProductStorageService microFinishedProductStorageService;
    private final IMicroFinishedProductStorageHistoryService microFinishedProductStorageHistoryService;
    private final IMicroEmailService microEmailService;
    private final IMicroUserService microUserService;
    private final IMicroProductService microProductService;
    private final RedisCache redisCache;
    private final MicroSupportUtil microSupportUtil;

    /**
     * 查询产成品库存列表
     *
     * @param microFinishedProductStorage
     * @return
     */
    @Override
    public List<MicroFinishedProductStorage> selectMicroFinishedProductStorageList(MicroFinishedProductStorage microFinishedProductStorage) {
        // 查询产成品库存信息
        List<MicroFinishedProductStorage> resultList = microFinishedProductStorageService.selectMicroFinishedProductStorageList(microFinishedProductStorage);
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }
        for (MicroFinishedProductStorage result : resultList) {
            MicroProduct microProduct = microProductService.selectMicroProductByProductSeq(result.getProductSeq());
            if (null != microProduct) {
                // 产品基本信息赋值
                result.setProductType(microProduct.getProductType());
                result.setProductUnit(microProduct.getUnit());
                result.setProductStandards(microProduct.getStandards());

                // 库存预警信息赋值
                if (null != microProduct.getStockLowerLimit()) {
                    result.setStockLowerLimit(microProduct.getStockLowerLimit());
                    if (result.getNum().compareTo(microProduct.getStockLowerLimit()) < 0) {
                        result.setWarningInfo(StockWarnFlagEnum.LOWER_SAFETY_STOCK.getDesc());
                    }
                }
                if (null != microProduct.getStockUpperLimit()) {
                    result.setStockUpperLimit(microProduct.getStockUpperLimit());
                    if (result.getNum().compareTo(microProduct.getStockUpperLimit()) > 0) {
                        result.setWarningInfo(StockWarnFlagEnum.UPPER_SAFETY_STOCK.getDesc());
                    }
                }
            }
        }
        return resultList;
    }

    /**
     * 邮件导出完工库存信息
     *
     * @param productCodeOrName
     * @param receivedBy
     * @return
     */
    @Override
    public String exportFinishStorageInfos(String productCodeOrName, String receivedBy) {
        log.info("请求参数为:productCodeOrName:{}, receivedBy:{}", productCodeOrName, receivedBy);
        // 校验邮箱格式
        if (!MicroEmailUtils.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.FINISH_PRODUCT_STORAGE_EXCEL_NAME);

        // 构建导出数据
        MicroFinishedProductStorage queryParam = new MicroFinishedProductStorage();
        queryParam.setProductCodeOrName(productCodeOrName);
        List<MicroFinishedProductStorage> storageList = microFinishedProductStorageService.selectMicroFinishedProductStorageList(queryParam);

        // 导出并发送邮件
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) { // KU易记导出 
            return microEmailService.exportAndSendEmail(MailConstants.FINISH_PRODUCT_STORAGE_MAIL_SUBJECT,
                    MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, storageList, MicroFinishedProductStorage.class, placeholderMap);
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)
                || CommonConstant.ApplicationSignEnum.MICRO_MATERIAL.getKey().equals(appSign)) { // 工易派、料易投导出 
            List<MicroPlanFinishProductStorage> resultList = Lists.newArrayList();

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

            // 导出并发送邮件
            return microEmailService.exportAndSendEmailByEasyPoi(MailConstants.FINISH_PRODUCT_STORAGE_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID,
                    receivedBy, resultList, MicroPlanFinishProductStorage.class, placeholderMap);
        }

        return null;
    }

    /**
     * 邮件导出完工库存变动信息
     *
     * @param productSeq
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Override
    public String exportFinishStorageHistoryInfos(Date startDate, Date endDate, String productSeq, String receivedBy) {
        log.info("请求参数为:startDate:{}, endDate:{}, productSeq:{}, receivedBy:{}", startDate, endDate, productSeq, receivedBy);
        // 校验邮箱格式
        if (!MicroEmailUtils.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }
        // 构建导出数据
        MicroFinishStorageChangeHistoryParam queryParam = new MicroFinishStorageChangeHistoryParam();
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);
        queryParam.setProductSeq(productSeq);
        List<MicroFinishedProductStorageHistory> resultList = microFinishedProductStorageHistoryService.selectMicroFinishedStorageChangeHistoryList(queryParam);
        resultList = resultList.stream().peek(e -> {
            // 查询完工操作人昵称
            MicroUser microUser = microUserService.selectMicroUserById(e.getChangeUser());
            if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                e.setChangeUserNickName(microUser.getNickName());
            }

            // 查询产品编码
            MicroProduct microProduct = microProductService.selectMicroProductByProductSeq(e.getProductSeq());
            if(null != microProduct && StringUtils.hasText(microProduct.getProductCode())){
                e.setProductCode(microProduct.getProductCode());
            }
        }).collect(Collectors.toList());

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.FINISH_PRODUCT_STORAGE_CHANGE_EXCEL_NAME);

        // 导出并发送邮件
        return microEmailService.exportAndSendEmail(MailConstants.FINISH_PRODUCT_STORAGE_CHANGE_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, resultList, MicroFinishedProductStorageHistory.class, placeholderMap);
    }

    /**
     * 查询产成品库存变动列表
     *
     * @param param
     * @return
     */
    @Override
    public List<FinishStorageChangeHistoryGpByDayResult> changeHistoryList(MicroFinishStorageChangeHistoryParam param) {
        List<FinishStorageChangeHistoryGpByDayResult> resultList = Lists.newArrayList();

        // 按照条件查询产成品库存变动历史表
        List<MicroFinishedProductStorageHistory> storageHistorieList = microFinishedProductStorageHistoryService.selectMicroFinishedStorageChangeHistoryList(param);

        // 按天分组
        Map<String, List<MicroFinishedProductStorageHistory>> collectGroupResult = storageHistorieList.stream()
                .peek(e -> e.setChangeDate(DateUtils.parseDateToStr("yyyy-MM-dd", e.getChangeTime())))
                .collect(Collectors.groupingBy(MicroFinishedProductStorageHistory::getChangeDate));

        // 对map可以按照key降序
        collectGroupResult = collectGroupResult.entrySet().stream()
                .sorted(Map.Entry.<String, List<MicroFinishedProductStorageHistory>>comparingByKey().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));

        for (Map.Entry<String, List<MicroFinishedProductStorageHistory>> entry : collectGroupResult.entrySet()) {
            FinishStorageChangeHistoryGpByDayResult result = new FinishStorageChangeHistoryGpByDayResult();
            String dateGroupKey = entry.getKey();
            List<MicroFinishedProductStorageHistory> finishStorageHistoryList = entry.getValue();

            // 按照库存变动时间降序
            finishStorageHistoryList = finishStorageHistoryList.stream()
                    .peek(e -> {
                        // 查询完工操作人昵称
                        MicroUser microUser = microUserService.selectMicroUserById(e.getChangeUser());
                        if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                            e.setChangeUserNickName(microUser.getNickName());
                        }
                    })
                    .sorted(Comparator.comparing(MicroFinishedProductStorageHistory::getChangeTime).reversed())
                    .collect(Collectors.toList());

            // 统计计算每天的入库总数、出库总数
            BigDecimal inBoundNum = BigDecimal.ZERO;
            BigDecimal outBoundNum = BigDecimal.ZERO;
            for (MicroFinishedProductStorageHistory storageHistory : finishStorageHistoryList) {
                if (storageHistory.getChangeNum().signum() >= 0) {
                    inBoundNum = inBoundNum.add(storageHistory.getChangeNum());
                } else {
                    outBoundNum = outBoundNum.add(storageHistory.getChangeNum());
                }
            }

            result.setDate(dateGroupKey);
            result.setInBoundNum(inBoundNum);
            result.setOutBoundNum(outBoundNum);
            result.setFinishChangeNum(finishStorageHistoryList.get(0).getFinishChangeNum());
            result.setStorageHistoryList(finishStorageHistoryList);
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 批量出入库
     *
     * @param param
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void batchInOrOutBound(MicroFinishProductStorageBoundBatchParam param) {
        Date nowDate = DateUtils.getNowDate();
        Long userId = SecurityUtils.getUserId();

        List<MicroFinishProductStorageBoundParam> paramList = param.getBoundParamList();

        // 1.数据合法性校验 + 组装库存变动数据集合
        List<MicroFinishedProductStorage> storageAddList = Lists.newArrayList();
        List<MicroFinishedProductStorage> storageUpdateList = Lists.newArrayList();
        List<MicroFinishedProductStorageHistory> storageHistoryList = Lists.newArrayList();

        for (MicroFinishProductStorageBoundParam boundParam : paramList) {
            String productSeq = boundParam.getProductSeq();
            BigDecimal changeNum = boundParam.getChangeNum();

            MicroFinishedProductStorage storage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(productSeq);
            String changeType = null;
            if (param.getInOrOutFlag()) { // 入库 
                if (null == storage) {
                    MicroProduct microProduct = microProductService.selectMicroProductByProductSeq(productSeq);
                    if (null == microProduct) {
                        log.warn("{}产品不存在！", productSeq);
                        throw new CustomException(productSeq + "产品不存在！");
                    } else { // 不存在，新增处理
                        storage = new MicroFinishedProductStorage();
                        storage.setProductSeq(productSeq);
                        storage.setProductName(microProduct.getProductName());
                        storage.setNum(changeNum);
                        storage.setCreatedBy(userId.toString());
                        storage.setCreatedDate(nowDate);
                        storageAddList.add(storage);
                    }
                } else { // 存在，更新处理
                    BigDecimal storageNum = storage.getNum().add(changeNum);
                    storage.setNum(storageNum);
                    storage.setLastUpdBy(userId.toString());
                    storage.setLastUpdDate(nowDate);
                    storageUpdateList.add(storage);
                }

                changeType = FinishStorageChangeTypeEnum.STORAGE_INBOUND.getCode();
            } else { // 出库
                if (null == storage) {
                    log.warn("{}产品不存在！", productSeq);
                    throw new CustomException(productSeq + "产品不存在！");
                }

                changeType = FinishStorageChangeTypeEnum.STORAGE_OUTBOUND.getCode();
                changeNum = changeNum.negate();

                BigDecimal storageNum = storage.getNum().add(changeNum);
                storage.setNum(storageNum);
                storage.setLastUpdBy(userId.toString());
                storage.setLastUpdDate(nowDate);
                storageUpdateList.add(storage);
            }

            MicroFinishedProductStorageHistory storageHistory = new MicroFinishedProductStorageHistory();
            storageHistory.setProductSeq(storage.getProductSeq());
            storageHistory.setProductName(storage.getProductName());
            storageHistory.setChangeType(changeType);
            storageHistory.setChangeNum(changeNum);
            storageHistory.setFinishChangeNum(storage.getNum());
            storageHistory.setChangeTime(nowDate);
            storageHistory.setChangeUser(userId);
            storageHistory.setChangeReason(param.getReason());
            storageHistory.setCreatedBy(userId.toString());
            storageHistory.setCreatedDate(nowDate);
            storageHistoryList.add(storageHistory);
        }

        // 2.调整库存
        ChangeFinishedStorageParam changeFinishedStorageParam = new ChangeFinishedStorageParam();
        changeFinishedStorageParam.setStorageAddList(storageAddList);
        changeFinishedStorageParam.setStorageUpdateList(storageUpdateList);
        changeFinishedStorageParam.setStorageHistoryList(storageHistoryList);
        microFinishedProductStorageService.changeBound(changeFinishedStorageParam);
    }
}
