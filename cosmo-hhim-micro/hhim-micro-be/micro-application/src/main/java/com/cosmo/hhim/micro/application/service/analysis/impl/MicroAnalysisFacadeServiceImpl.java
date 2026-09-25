/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.analysis.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.micro.application.dto.analysis.*;
import com.cosmo.hhim.micro.application.service.analysis.IMicroAnalysisFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.bom.MicroProductBom;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserBusinessEntity;
import com.cosmo.hhim.micro.base.domain.entity.customer.MicroCustomer;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.mapper.bom.MicroProductBomMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroProductService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.base.domain.service.customer.IMicroCustomerService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroManufactureLineService;
import com.cosmo.hhim.micro.base.domain.service.factory.IMicroWorkShopService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.base.ActiveFlagStandardEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.BomAndTechTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.OrderStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.planning.WorkOrderStatusEnum;
import com.cosmo.hhim.micro.planning.domain.entity.manufacture.MicroManufactureOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.CountWorkOrderNumQueryParam;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.mapper.manufacture.MicroManufactureOrderMapper;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.planning.domain.service.manufacture.IMicroManufactureOrderService;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishStorageChangeHistoryParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorageHistory;
import com.cosmo.hhim.micro.storage.domain.mapper.MicroFinishedProductStorageHistoryMapper;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @description: 不同角色首页展示接口 Facade 层
 * @date 2023/5/4 13:48
 */
@Service
@Slf4j
public class MicroAnalysisFacadeServiceImpl implements IMicroAnalysisFacadeService {

    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;
    @Autowired
    private MicroProductBomMapper microProductBomMapper;
    @Autowired
    private MicroManufactureOrderMapper microManufactureOrderMapper;
    @Autowired
    private MicroFinishedProductStorageHistoryMapper microFinishedProductStorageHistoryMapper;
    @Autowired
    private IMicroFinishedProductStorageService microFinishedProductStorageService;
    @Autowired
    private IMicroManufactureOrderService microManufactureOrderService;
    @Autowired
    private IMicroUserService microUserService;
    @Autowired
    private IMicroManufactureLineService microManufactureLineService;
    @Autowired
    private IMicroWorkShopService microWorkShopService;
    @Autowired
    private IMicroCustomerService microCustomerService;
    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;
    @Autowired
    private IMicroProductService microProductService;

    /**
     * 计算工易派首页信息（企业主、审产员）
     *
     * @return
     */
    @Override
    public IndexStatisticInfoFromPlan calculateIndexStatisticInfoFromPlan(Date queryDate) {
        log.info("请求的参数为:", queryDate.toString());
        // 返回结果
        IndexStatisticInfoFromPlan result = new IndexStatisticInfoFromPlan();
        // 查询报工记录信息
        List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectWorkSubmitInfosBySubmitUserAndDay(null,
                queryDate, queryDate, SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        if (!CollectionUtils.isEmpty(microWorkSubmitList)) {
            BigDecimal checkNum = this.calculateCheckNum(microWorkSubmitList);
            BigDecimal waitCheckNum = this.calculateWaitCheckNum(microWorkSubmitList);

            result.setCheckNum(checkNum);
            result.setWaitCheckNum(waitCheckNum);
            result.setTotalSubmitNum(checkNum.add(waitCheckNum));
        }

        // 统计入库数量
        MicroFinishStorageChangeHistoryParam queryParamByStorage = new MicroFinishStorageChangeHistoryParam();
        queryParamByStorage.setStartDate(DateUtil.beginOfDay(queryDate).toJdkDate());
        queryParamByStorage.setEndDate(DateUtil.endOfDay(queryDate).toJdkDate());
        // 期初库存导入不算在内
        queryParamByStorage.setChangeTypeList(Arrays.asList(FinishStorageChangeTypeEnum.FINISH_INBOUND.getCode(),
                FinishStorageChangeTypeEnum.FINISH_CANCEL.getCode()));
        List<MicroFinishedProductStorageHistory> storageHistoryList = microFinishedProductStorageHistoryMapper.selectMicroFinishedStorageChangeHistoryList(queryParamByStorage);
        if (CollectionUtil.isNotEmpty(storageHistoryList)) {
            // 统计入库数量
            BigDecimal inboundNum = storageHistoryList.stream().map(MicroFinishedProductStorageHistory::getChangeNum)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.setInboundNum(inboundNum);

            // 统计产品款数
            Long inBoundProductCategoryNum = 0L;
            Map<String, List<MicroFinishedProductStorageHistory>> map = storageHistoryList.stream().collect(Collectors.groupingBy(MicroFinishedProductStorageHistory::getProductSeq));
            for (Map.Entry<String, List<MicroFinishedProductStorageHistory>> entry : map.entrySet()) {
                BigDecimal tempNum = entry.getValue().stream().map(MicroFinishedProductStorageHistory::getChangeNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (tempNum.signum() > 0) {
                    inBoundProductCategoryNum ++;
                }
            }
            result.setInBoundProductCategoryNum(inBoundProductCategoryNum);
        }
        return result;
    }

    /**
     * 工易派 - 不同状态工单数量统计
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public WorkOrderNumInDifferentStatus calculateWorkOrderNumInDifferentStatus(Date startDate, Date endDate) {
        log.info("请求的参数为:startDate:{},endDate:{}", startDate, endDate);
        // 存储结果
        WorkOrderNumInDifferentStatus result = new WorkOrderNumInDifferentStatus();

        CountWorkOrderNumQueryParam queryParam = new CountWorkOrderNumQueryParam();
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);

        // 待生产
        queryParam.setWorkOrderStatus(WorkOrderStatusEnum.TO_BE_PRODCED.getCode());
        Long waitingPeriodOrderNum = microManufactureWorkOrderMapper.countWorkOrderNumByCondition(queryParam);

        // 生产中
        queryParam.setWorkOrderStatus(WorkOrderStatusEnum.IN_PRODUCTION.getCode());
        Long inProductionOrderNum = microManufactureWorkOrderMapper.countWorkOrderNumByCondition(queryParam);

        // 生产中并且逾期的
        queryParam.setOverDateFlag("1");
        Long overDateOrderNum = microManufactureWorkOrderMapper.countWorkOrderNumByCondition(queryParam);
        queryParam.setOverDateFlag(null);

        // 完成
        queryParam.setWorkOrderStatus(WorkOrderStatusEnum.FINISHED.getCode());
        Long finishedWorkOrderNum = microManufactureWorkOrderMapper.countWorkOrderNumByCondition(queryParam);

        // 关闭
        queryParam.setWorkOrderStatus(WorkOrderStatusEnum.CLOSED.getCode());
        Long closedWorkOrderNum = microManufactureWorkOrderMapper.countWorkOrderNumByCondition(queryParam);

        Long terminalOrderNum = finishedWorkOrderNum + closedWorkOrderNum;
        // 生产中为逾期
        inProductionOrderNum = inProductionOrderNum - overDateOrderNum;

        result.setWaitingPeriodOrderNum(waitingPeriodOrderNum);
        result.setInProductionOrderNum(inProductionOrderNum);
        result.setOverDateOrderNum(overDateOrderNum);
        result.setTerminalOrderNum(terminalOrderNum);
        result.setTotalOrderNum(waitingPeriodOrderNum + inProductionOrderNum + overDateOrderNum + terminalOrderNum);

        return result;
    }

    /**
     * 工易派首页 - 预警统计信息
     *
     * @return
     */
    @Override
    public WarnStatisticInfo calculateWarnStatisticInfo() {
        WarnStatisticInfo result = new WarnStatisticInfo();
        // 1. 计算逾期工单的数量
        MicroManufactureWorkOrder queryParam = new MicroManufactureWorkOrder();
        // 工单状态为待生产和生产中
        queryParam.setAvailableFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        queryParam.setOverDateFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        List<MicroManufactureWorkOrder> microManufactureWorkOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderListByCondition(queryParam);
        if (!CollectionUtils.isEmpty(microManufactureWorkOrderList)) {
            result.setOverDateWorkOrderNum(microManufactureWorkOrderList.size());
        }

        // 2. 超安全库存的产品预警
        List<MicroFinishedProductStorage> microFinishedProductStorageList = microFinishedProductStorageService.selectMicroFinishedProductStorageFromOverSafetyStock();
        if (!CollectionUtils.isEmpty(microFinishedProductStorageList)) {
            result.setOverSafetyStockProductNum(microFinishedProductStorageList.size());
        }

        // 3. 逾期的订单(未交付和已交付)
        MicroManufactureOrder queryParamByOrder = new MicroManufactureOrder();
        queryParamByOrder.setAvailableFlag("1");
        queryParamByOrder.setOrderWarnQueryFlag("1");
        List<MicroManufactureOrder> microManufactureOrderList = microManufactureOrderMapper.selectMicroManufactureOrderListByCondition(queryParamByOrder);
        if (CollectionUtil.isNotEmpty(microManufactureOrderList)) {
            result.setWarnOrderNum(microManufactureOrderList.size());
        }

        return result;
    }

    /**
     * 统计工易派生产管理模块的信息
     *
     * @return
     */
    @Override
    public ProductionManageStatisticInfo calculateProductionManageStatisticInfo() {
        ProductionManageStatisticInfo result = new ProductionManageStatisticInfo();
        // 1. 待排产订单数量
        MicroManufactureOrder queryParamByOrder = new MicroManufactureOrder();
        queryParamByOrder.setOrderStatus(OrderStatusEnum.WAITING_PERIOD.getCode());
        List<MicroManufactureOrder> microManufactureOrderList = microManufactureOrderService.selectMicroManufactureOrderList(queryParamByOrder);
        if (!CollectionUtils.isEmpty(microManufactureOrderList)) {
            result.setWaitingPeriodOrderNum(microManufactureOrderList.size());
        }
        // 2. 待生产工单数量
        MicroManufactureWorkOrder queryParamByWorkOrder = new MicroManufactureWorkOrder();
        queryParamByWorkOrder.setWorkOrderStatus(WorkOrderStatusEnum.TO_BE_PRODCED.getCode());
        List<MicroManufactureWorkOrder> microManufactureWorkOrderList = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderListByCondition(queryParamByWorkOrder);
        if (!CollectionUtils.isEmpty(microManufactureWorkOrderList)) {
            result.setWaitingPeriodWorkOrderNum(microManufactureWorkOrderList.size());
        }
        // 3. 待入库数量 -> 根据工单信息进行统计, (1)工单状态是已完成或者已关单 (2)完工标示为1,未完工
        MicroManufactureWorkOrder queryParam = new MicroManufactureWorkOrder();
        List<String> statusList = new ArrayList<>();
        statusList.add(WorkOrderStatusEnum.FINISHED.getCode());
        statusList.add(WorkOrderStatusEnum.CLOSED.getCode());
        queryParam.setStatusList(statusList);
        queryParam.setIsComplete(IsCompleteEnum.NO.getCode());
        List<MicroManufactureWorkOrder> microManufactureWorkOrders = microManufactureWorkOrderMapper.selectMicroManufactureWorkOrderListByWorkOrderStatusList(queryParam);
        if (!CollectionUtils.isEmpty(microManufactureWorkOrders)) {
            BigDecimal waitingInBoundNum = microManufactureWorkOrders.stream()
                    .map(MicroManufactureWorkOrder::getFinishNum)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.setWaitingInBoundNum(waitingInBoundNum);
        }

        return result;
    }

    /**
     * 工易派 - 基础数据信息统计
     *
     * @return
     */
    @Override
    public BaseStatisticInfo calculateBaseStatisticInfo() {
        BaseStatisticInfo result = new BaseStatisticInfo();
        // 1. 基础的用户、产品、工序信息
        MicroUserBusinessEntity businessInfo = microUserService.getBusinessInfo();
        result.setStaffNum(businessInfo.getStaffNum().intValue());
        result.setProductNum(businessInfo.getProductNum().intValue());
        result.setProcessNum(businessInfo.getProcessNum().intValue());
        // 2. bom信息
        MicroProductBom queryParamByBom = new MicroProductBom();
        // 第一级
        queryParamByBom.setParentProductSeq("0");
        queryParamByBom.setBomType(BomAndTechTypeEnum.DRAFT.getCode());
        List<MicroProductBom> microProductBomListByDraft = microProductBomMapper.selectMicroProductBomList(queryParamByBom);
        if (!CollectionUtils.isEmpty(microProductBomListByDraft)) {
            result.setDraftBomNum(microProductBomListByDraft.size());
        }
        queryParamByBom.setBomType(BomAndTechTypeEnum.STANDARD.getCode());
        List<MicroProductBom> microProductBomListByStandard = microProductBomMapper.selectMicroProductBomList(queryParamByBom);
        if (!CollectionUtils.isEmpty(microProductBomListByStandard)) {
            result.setStandardBomNum(microProductBomListByStandard.size());
        }
        // 3. 车间产线信息
        MicroWorkShop queryParamByWorkShop = new MicroWorkShop();
        queryParamByWorkShop.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        List<MicroWorkShop> microWorkShopList = microWorkShopService.selectMicroWorkshopList(queryParamByWorkShop);
        if (!CollectionUtils.isEmpty(microWorkShopList)) {
            result.setWorkShopNum(microWorkShopList.size());
        }
        MicroManufactureLine queryParamByLine = new MicroManufactureLine();
        queryParamByLine.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        List<MicroManufactureLine> microManufactureLineList = microManufactureLineService.selectMicroManufactureLineList(queryParamByLine);
        if (!CollectionUtils.isEmpty(microManufactureLineList)) {
            result.setManufactureLineNum(microManufactureLineList.size());
        }
        // 4. 客户信息
        MicroCustomer queryParamByCustomer = new MicroCustomer();
        queryParamByCustomer.setActiveFlag(ActiveFlagStandardEnum.NORMAL.getCode());
        List<MicroCustomer> microCustomerList = microCustomerService.selectMicroCustomerList(queryParamByCustomer);
        if (!CollectionUtils.isEmpty(microCustomerList)) {
            result.setCustomerNum(microCustomerList.size());
        }
        return result;
    }

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @Override
    public List<ProductiveSubmitSummaryInfoByProduct> getProductiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));
        return microWorkSubmitService.selectProductiveSubmitSummaryAnalysisByProduct(productionQualityAnalysisParam);
    }

    /**
     * 工易派 - 基于产品的工序维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @Override
    public List<ProductiveSubmitSummaryInfoByProcessBaseProduct> getProductiveSubmitSummaryAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));
        return microWorkSubmitService.selectProductiveSubmitSummaryAnalysisByProcessBaseProduct(productionQualityAnalysisParam);
    }

    /**
     * 工易派 - 员工维度生产报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @Override
    public List<ProductiveSubmitAnalysisByUser> getProductiveSubmitSummaryAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));
        return microWorkSubmitService.selectProductiveSubmitAnalysisByUser(productionQualityAnalysisParam);
    }

    /**
     * 工易派 - 员工维度下各产品的报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    @Override
    public List<ProductSubmitSummaryInfoBaseUser> getProductiveSubmitSummaryInfoBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));
        return microWorkSubmitService.selectProductiveSubmitSummaryInfoBaseUser(productionQualityAnalysisParam);
    }

    /**
     * 统计时间范围内入库产品信息
     *
     * @param startDate
     * @param endDate
     * @return
     */
    @Override
    public List<InBoundProduct> InBoundProductStatisticsInfo(Date startDate, Date endDate) { 
        log.info("请求的参数为:startDate:{}, endDate:{}", startDate, endDate);
        // 存储结果
        List<InBoundProduct> result = new ArrayList<>();
        // 统计入库数量
        MicroFinishStorageChangeHistoryParam queryParam = new MicroFinishStorageChangeHistoryParam();
        queryParam.setStartDate(DateUtil.beginOfDay(startDate).toJdkDate());
        queryParam.setEndDate(DateUtil.endOfDay(endDate).toJdkDate());
        // 期初库存导入不算在内
        queryParam.setChangeTypeList(Arrays.asList(FinishStorageChangeTypeEnum.FINISH_INBOUND.getCode(),
                FinishStorageChangeTypeEnum.FINISH_CANCEL.getCode()));
        List<MicroFinishedProductStorageHistory> storageHistoryList = microFinishedProductStorageHistoryMapper.selectMicroFinishedStorageChangeHistoryList(queryParam);
        if (CollectionUtil.isNotEmpty(storageHistoryList)) {
            // 以产品进行划分，库存变动历史记录
            Map<String, List<MicroFinishedProductStorageHistory>> map = storageHistoryList.stream().collect(Collectors.groupingBy(MicroFinishedProductStorageHistory::getProductSeq));
            for (Map.Entry<String, List<MicroFinishedProductStorageHistory>> entry : map.entrySet()) {
                InBoundProduct temp = new InBoundProduct();
                // 统计入库数量
                BigDecimal inboundNum = entry.getValue().stream().map(MicroFinishedProductStorageHistory::getChangeNum)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (inboundNum.signum() > 0) {
                    temp.setInboundNum(inboundNum);
                    MicroFinishedProductStorageHistory copyTemp = entry.getValue().get(0);
                    temp.setProductSeq(entry.getKey());
                    // 查询产品编码
                    MicroProduct microProduct = microProductService.selectMicroProductByProductSeq(entry.getKey());
                    if (null != microProduct){
                        temp.setProductCode(microProduct.getProductCode());
                        temp.setProductName(copyTemp.getProductName());
                        temp.setProductUnit(microProduct.getUnit());
                    }
                    result.add(temp);
                }
            }
        }
        return result;
    }

    public BigDecimal calculateCheckNum(List<MicroWorkSubmit> microWorkSubmitList) {
        return microWorkSubmitList.stream()
                .filter(obj -> obj.getSubmitStatus() == SubmitStatusEnum.APPROVED.getCode().longValue())
                .map(obj -> obj.getCheckPassNum().add(obj.getCheckNgNum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateWaitCheckNum(List<MicroWorkSubmit> microWorkSubmitList) {
        return microWorkSubmitList.stream()
                .filter(obj -> obj.getSubmitStatus() == SubmitStatusEnum.UN_APPROVE.getCode().longValue())
                .map(obj -> obj.getPassNum().add(obj.getNgNum()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 待生产的工单数量
     *
     * @param microManufactureWorkOrderList
     * @return
     */
    public Long calculateWaitingPeriodOrderNum(List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {
        return microManufactureWorkOrderList.stream()
                .filter(obj -> obj.getWorkOrderStatus().equals(WorkOrderStatusEnum.TO_BE_PRODCED.getCode()))
                .count();
    }

    /**
     * 工单属于生产中，并且还没有逾期
     *
     * @param microManufactureWorkOrderList
     * @return
     */
    public Long calculateInProductionOrderNum(List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {
        return microManufactureWorkOrderList.stream()
                .filter(obj -> obj.getWorkOrderStatus().equals(WorkOrderStatusEnum.IN_PRODUCTION.getCode())
                        && DateUtil.compare(obj.getPlanEndDate(), DateUtils.getNowDate(), "yyyy-MM-dd") >= 0)
                .count();
    }

    /**
     * 工单属于生产中, 但逾期
     *
     * @param microManufactureWorkOrderList
     * @return
     */
    public Long calculateOverDateOrderNum(List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {
        return microManufactureWorkOrderList.stream()
                .filter(obj -> obj.getWorkOrderStatus().equals(WorkOrderStatusEnum.IN_PRODUCTION.getCode())
                        && DateUtil.compare(obj.getPlanEndDate(), DateUtils.getNowDate(), "yyyy-MM-dd") < 0)
                .count();
    }

    /**
     * 已结束工单 (完成或者关闭)
     *
     * @param microManufactureWorkOrderList
     * @return
     */
    public Long calculateTerminalOrderNum(List<MicroManufactureWorkOrder> microManufactureWorkOrderList) {
        return microManufactureWorkOrderList.stream()
                .filter(obj -> obj.getWorkOrderStatus().equals(WorkOrderStatusEnum.FINISHED.getCode())
                        || obj.getWorkOrderStatus().equals(WorkOrderStatusEnum.CLOSED.getCode()))
                .count();
    }
}
