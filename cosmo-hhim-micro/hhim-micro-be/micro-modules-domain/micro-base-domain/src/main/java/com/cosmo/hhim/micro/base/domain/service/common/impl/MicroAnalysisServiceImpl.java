/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitProductCount;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroAnalysisMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAnalysisService;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitStatusEnum;
import com.cosmo.hhim.micro.infrastructure.enums.TimeTypeEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroSupportUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroAnalysisServiceImpl implements IMicroAnalysisService {

    @Autowired
    private MicroAnalysisMapper microAnalysisMapper;
    @Autowired
    private MicroWorkSubmitMapper microWorkSubmitMapper;
    @Autowired
    private MicroSupportUtil microSupportUtil;

    /**
     * 记功记录排行
     *
     */
    @Override
    public List<SubmitterRank> getRecordRankForSubmitter(LocalDate startDate, LocalDate endDate) {
        Long submitType = microSupportUtil.getSubmitType();
        List<SubmitterRank> recordRankForSubmitter = microAnalysisMapper.getRecordRankForSubmitter(startDate, endDate, submitType);
        return recordRankForSubmitter;
    }

    /**
     * 完工产品统计
     *
     */
    @Override
    public List<FinishedProduct> getFinishedProductStatistics(LocalDate startDate, LocalDate endDate) {
        List<FinishedProduct> finishedProducts = microAnalysisMapper.getFinishedProductStatistics(startDate, endDate);
        return finishedProducts;
    }

    /**
     * 库存排名（产品 + 工序）
     *
     */
    @Override
    public List<MicroProcessStorage> getStorageRank(String productSeq) {
        return microAnalysisMapper.getStorageRank(productSeq);
    }

    /**
     * 质量分析 - 总计展示
     *
     */
    @Override
    public MicroWorkSubmitProductCount showTotalCount(String productNameOrCode, LocalDate startDate, LocalDate endDate) {
        // 打印入参
        log.info("请求参数为：{}-{}-{}", productNameOrCode, startDate, endDate);

        // 查询数据库
        MicroWorkSubmitProductCount microWorkSubmitProductCount = microAnalysisMapper.showTotalCount(productNameOrCode, startDate, endDate);
        if (microWorkSubmitProductCount.getTotalCounts().signum() == 0) {
            return null;
        }
        microWorkSubmitProductCount.setPassRate(microWorkSubmitProductCount.getTotalPassNum()
                .divide(microWorkSubmitProductCount.getTotalCounts(), 3, RoundingMode.DOWN));
        return microWorkSubmitProductCount;
    }

    /**
     * 生产质量趋势分析
     *
     */
    @Override
    public List<MicroWorkSubmitProductCount> productionQualityTrend(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求参数为：{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        // 区分应用, 增加报工方式参数
        productionQualityAnalysisParam.setSubmitType(microSupportUtil.getSubmitType());
        List<MicroWorkSubmitProductCount> microWorkSubmitProductCounts = microAnalysisMapper.productionQualityTrend(productionQualityAnalysisParam);
        if (CheckObjectUtils.isEmpty(microWorkSubmitProductCounts)) {
            return null;
        }
        List<MicroWorkSubmitProductCount> valid = new ArrayList<>();
        for (MicroWorkSubmitProductCount obj : microWorkSubmitProductCounts) {
            // 已审但没有质检数（check_pass + check_ng = 0）的日子算不出良品率：
            // 跳过该天，而不是抛异常（原实现 throw CustomException 会把整张趋势图打挂；
            // 口径与问数侧登记 SQL 的 having > 0 一致：无质检数的日子不参与良品率）
            if (obj.getTotalCounts() == null || obj.getTotalCounts().signum() == 0) {
                log.warn("[趋势] {} 报工总数量为0（无质检数），已跳过该天", obj.getSubmitDay());
                continue;
            }
            obj.setPassRate(obj.getTotalPassNum().divide(obj.getTotalCounts(), 3, RoundingMode.DOWN));
            obj.setNgRate(obj.getTotalNgNum().divide(obj.getTotalCounts(), 3, RoundingMode.DOWN));
            valid.add(obj);
        }
        return valid;
    }

    /**
     * 良品率分析 - 产品维度
     *
     */
    @Override
    public List<MicroWorkSubmitProductCount> passRateAnalysisByProduct(MicroWorkSubmitDto microWorkSubmitDto) {
        // 打印入参
        log.info("请求参数为: {}", JSONObject.toJSONString(microWorkSubmitDto));
        // 存储结果
        List<MicroWorkSubmitProductCount> res = new ArrayList<>();

        // 增加小程序区分
        Long submitType = microSupportUtil.getSubmitType();
        microWorkSubmitDto.setSubmitType(submitType);
        // 产品维度
        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectCompletedProductListByProduct(microWorkSubmitDto);
        if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
            return res;
        }
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            BigDecimal totalCounts = obj.getCheckNgNum().add(obj.getCheckPassNum());
            // 已审但质检数为 0 的行算不出良品率 → 跳过该行（原实现直接 divide(0) 抛 ArithmeticException → 接口 500）
            if (totalCounts.signum() == 0) {
                log.warn("[良品率-产品] {} 质检数为0，已跳过", obj.getProductName());
                continue;
            }
            MicroWorkSubmitProductCount temp = new MicroWorkSubmitProductCount();
            temp.setProductCode(obj.getProductCode());
            temp.setProductName(obj.getProductName());
            temp.setProductSeq(obj.getProductSeq());
            temp.setTotalPassNum(obj.getCheckPassNum());
            temp.setTotalNgNum(obj.getCheckNgNum());
            temp.setTotalCounts(totalCounts);
            temp.setPassRate(obj.getCheckPassNum().divide(totalCounts, 3, RoundingMode.DOWN));
            res.add(temp);
        }
        // 按照良品率倒序
        res.sort(Comparator.comparing(MicroWorkSubmitProductCount::getPassRate).reversed());
        return res;
    }

    /**
     * 良品率分析 - 工序维度
     *
     */
    @Override
    public List<MicroWorkSubmitProductCount> passRateAnalysisByProcess(MicroWorkSubmitDto microWorkSubmitDto) {
        // 打印入参
        log.info("请求参数为: {}", JSONObject.toJSONString(microWorkSubmitDto));
        List<MicroWorkSubmitProductCount> result = new ArrayList<>();

        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectCompletedProductListByProcess(microWorkSubmitDto);
        if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
            return result;
        }
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            BigDecimal totalCounts = obj.getCheckPassNum().add(obj.getCheckNgNum());
            // 同上：无质检数的工序不参与良品率（否则除零 500）
            if (totalCounts.signum() == 0) {
                log.warn("[良品率-工序] {} 质检数为0，已跳过", obj.getOperateProcessName());
                continue;
            }
            MicroWorkSubmitProductCount resTemp = new MicroWorkSubmitProductCount();
            resTemp.setProcessCode(obj.getOperateProcessCode());
            resTemp.setProcessName(obj.getOperateProcessName());
            resTemp.setProcessSeq(obj.getOperateProcessSeq());
            resTemp.setTotalPassNum(obj.getCheckPassNum());
            resTemp.setTotalNgNum(obj.getCheckNgNum());
            resTemp.setTotalCounts(totalCounts);
            resTemp.setPassRate(obj.getCheckPassNum().divide(totalCounts, 3, RoundingMode.DOWN));
            result.add(resTemp);
        }
        // 按照良品率倒序
        result.sort(Comparator.comparing(MicroWorkSubmitProductCount::getPassRate).reversed());
        return result;
    }

    /**
     * 良品率分析 - 员工维度
     *
     */
    @Override
    public List<SubmitterRank> passRateAnalysisByEmployee(MicroWorkSubmitDto microWorkSubmitDto) {
        // 打印入参
        log.info("请求参数为: {}", microWorkSubmitDto);
        // 返回结果
        List<SubmitterRank> res = new ArrayList<>();

        List<MicroWorkSubmitDto> microWorkSubmitDtoList = microWorkSubmitMapper.selectCompletedProductListByEmployee(microWorkSubmitDto);
        if (CheckObjectUtils.isEmpty(microWorkSubmitDtoList)) {
            return res;
        }
        for (MicroWorkSubmitDto obj : microWorkSubmitDtoList) {
            BigDecimal submitNum = obj.getCheckPassNum().add(obj.getCheckNgNum());
            // 同上：无质检数的员工不参与良品率（否则除零 500）
            if (submitNum.signum() == 0) {
                log.warn("[良品率-员工] {} 质检数为0，已跳过", obj.getSubmitNickName());
                continue;
            }
            SubmitterRank submitter = new SubmitterRank();
            submitter.setNickName(obj.getSubmitNickName());
            submitter.setUserName(obj.getCreatedBy());
            submitter.setPassNum(obj.getCheckPassNum());
            submitter.setSubmitNum(submitNum);
            submitter.setPassRate(obj.getCheckPassNum().divide(submitNum, 3, RoundingMode.DOWN));
            res.add(submitter);
        }
        res.sort(Comparator.comparing(SubmitterRank::getPassRate).reversed());
        return res;
    }

    /**
     * 获取分析首页信息 - 传递开始和结束时间
     *
     */
    @Override
    public AnalysisIndex obtainedAnalysisIndexInformation(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        // 打印参数
        log.info("请求参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));
        if (CheckObjectUtils.isAnyEmpty(productionQualityAnalysisParam.getTimeType(),
                productionQualityAnalysisParam.getStartDate(),
                productionQualityAnalysisParam.getEndDate())) {
            throw new CustomException("时间类型、开始时间和结束时间均不能为空");
        }

        // 存放返回结果
        AnalysisIndex result;
        LocalDate startDate = productionQualityAnalysisParam.getStartDate();
        LocalDate endDate = productionQualityAnalysisParam.getEndDate();
        // 获取时间范围内的统计信息
        result = this.obtainedCheckInformation(productionQualityAnalysisParam);
        // 打印生成的结果
        log.info("选定时间范围内的首页信息为:{}", JSONObject.toJSONString(result));

        // 根据参数判定是按照本天、本月、本年还是自定义进行环比
        LocalDate startDateTemp = null;
        LocalDate endDateTemp = null;
        LocalDate dateTemp;
        // 这里和前端已经约定好了
        switch (TimeTypeEnum.getEnum(productionQualityAnalysisParam.getTimeType())) {
            case CURRENT_DAY:
                startDateTemp = startDate.minusDays(1);
                endDateTemp = endDate.minusDays(1);
                break;
            case CURRENT_MONTH:
                dateTemp = startDate.minusMonths(1);
                // 获取本月的第一天和本月的最后一天
                startDateTemp = dateTemp.with(TemporalAdjusters.firstDayOfMonth());
                endDateTemp = dateTemp.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case CURRENT_YEAR:
                dateTemp = startDate.minusYears(1);
                startDateTemp = dateTemp.with(TemporalAdjusters.firstDayOfYear());
                endDateTemp = dateTemp.with(TemporalAdjusters.lastDayOfYear());
                break;
            case UNDEFINED_TIME_TYPE:
                //自定义要先计算时间跨度
                long timeGap = endDate.toEpochDay() - startDate.toEpochDay();
                log.info("天数差距{}", timeGap);
                // 自定义可能输入的是本天的日期
                if (timeGap == 0L) {
                    startDateTemp = startDate.minusDays(1);
                    endDateTemp = endDate.minusDays(1);
                } else {
                    startDateTemp = startDate.minusDays(timeGap + 1);
                    endDateTemp = endDate.minusDays(timeGap + 1);
                }
                break;
            default:
                break;
        }

        ProductionQualityAnalysisParam param = new ProductionQualityAnalysisParam();
        param.setStartDate(startDateTemp);
        param.setEndDate(endDateTemp);
        AnalysisIndex temp = this.obtainedCheckInformation(param);
        // 环比计算结果打印
        log.info("环比计算时间范围内的信息为:{}", JSONObject.toJSONString(temp));

        // 环比计算
        BigDecimal momCheckProgress;
        BigDecimal momPassRate;
        // 总的审核数量环比
        if (result.getTotalNum().signum() > 0 && temp.getTotalNum().signum() == 0) {
            momCheckProgress = BigDecimal.valueOf(1);
        } else if (result.getTotalNum().signum() == 0 && temp.getTotalNum().signum() == 0) {
            momCheckProgress = BigDecimal.valueOf(0);
        } else if (result.getTotalNum().signum() == 0 && temp.getTotalNum().signum() > 0) {
            momCheckProgress = BigDecimal.valueOf(1).negate();
        } else {
            momCheckProgress = result.getTotalNum().divide(temp.getTotalNum(), 3, RoundingMode.DOWN).subtract(BigDecimal.valueOf(1L));
        }

        // 良品率环比
        if (result.getPassRate().signum() > 0 && temp.getPassRate().signum() == 0) {
            momPassRate = BigDecimal.valueOf(1);
        } else if (result.getPassRate().signum() == 0 && temp.getPassNum().signum() == 0) {
            momPassRate = BigDecimal.valueOf(0);
        } else if (result.getPassRate().signum() == 0 && temp.getPassRate().signum() > 0) {
            momPassRate = BigDecimal.valueOf(1).negate();
        } else {
            momPassRate = result.getPassRate().divide(temp.getPassRate(), 3, RoundingMode.DOWN).subtract(BigDecimal.valueOf(1));
        }

        result.setMomCheckProgress(momCheckProgress);
        result.setMomPassRate(momPassRate);

        return result;
    }

    /**
     * 展示不良品列表 (报工记录表)
     *
     */
    @Override
    public List<NgProduct> showNgProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求参数为：{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<NgProduct> ngProductList = microAnalysisMapper.selectNgProductList(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(ngProductList)) {
            return Collections.emptyList();
        }

        // 一次查出所有产品的工序良品数量
        String productSeqList = ngProductList.stream().map(NgProduct::getProductSeq).collect(Collectors.joining(","));
        productionQualityAnalysisParam.setProductNameOrCode(null);
        productionQualityAnalysisParam.setProductSeqList(productSeqList);
        List<NgProductAndProcess> ngProductAndProcessList = microAnalysisMapper.selectNgProductAndProcessList(productionQualityAnalysisParam);
        Map<String, List<NgProductAndProcess>> productSeqMap = ngProductAndProcessList.stream().collect(Collectors.groupingBy(NgProductAndProcess::getProductSeq));

        for (NgProduct ngProduct : ngProductList) {
            List<NgProductAndProcess> ngProductAndProcessListTemp = productSeqMap.get(ngProduct.getProductSeq());
            ngProduct.setNgProductAndProcessList(ngProductAndProcessListTemp);
        }

        return ngProductList;
    }

    /**
     * 展示不良品列表（库存表）
     *
     */
    @Override
    public List<NgProduct> showNgProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<NgProduct> productList = microAnalysisMapper.selectNgProductListByStock(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }

        // 一次查出所有产品的工序良品数量
        List<NgProductAndProcess> productAndProcessList = microAnalysisMapper.selectNgProductAndProcessListByStock();
        Map<String, List<NgProductAndProcess>> productSeqMap = productAndProcessList.stream().collect(Collectors.groupingBy(NgProductAndProcess::getProductSeq));

        for (NgProduct ngProduct : productList) {
            List<NgProductAndProcess> productAndProcessListTemp = productSeqMap.get(ngProduct.getProductSeq());
            ngProduct.setNgProductAndProcessList(productAndProcessListTemp);
        }

        return productList;
    }

    /**
     * 展示良品列表 （报工记录表）
     *
     */
    @Override
    public List<PassProduct> showPassProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求参数为：{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<PassProduct> passProductList = microAnalysisMapper.selectPassProductList(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(passProductList)) {
            return Collections.emptyList();
        }

        // 一次查出所有产品的工序良品数量
        String productSeqList = passProductList.stream().map(PassProduct::getProductSeq).collect(Collectors.joining(","));
        productionQualityAnalysisParam.setProductNameOrCode(null);
        productionQualityAnalysisParam.setProductSeqList(productSeqList);
        List<PassProductAndProcess> passProductAndProcessList = microAnalysisMapper.selectPassProductAndProcessList(productionQualityAnalysisParam);
        Map<String, List<PassProductAndProcess>> productSeqMap = passProductAndProcessList.stream().collect(Collectors.groupingBy(PassProductAndProcess::getProductSeq));

        // 分别设置到各自产品实体中
        for (PassProduct passProduct : passProductList) {
            List<PassProductAndProcess> passProductAndProcessListTemp = productSeqMap.get(passProduct.getProductSeq());
            passProduct.setPassProductAndProcessList(passProductAndProcessListTemp);
        }
        return passProductList;
    }

    /**
     * 展示良品列表 （库存表）
     *
     */
    @Override
    public List<PassProduct> showPassProductListByStock(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求的参数为:{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<PassProduct> productList = microAnalysisMapper.selectPassProductListByStock(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }

        // 一次查出所有产品的工序良品数量
        List<PassProductAndProcess> productAndProcessList = microAnalysisMapper.selectPassProductAndProcessListByStock();
        Map<String, List<PassProductAndProcess>> productSeqMap = productAndProcessList.stream().collect(Collectors.groupingBy(PassProductAndProcess::getProductSeq));

        for (PassProduct passProduct : productList) {
            List<PassProductAndProcess> productAndProcessListTemp = productSeqMap.get(passProduct.getProductSeq());
            passProduct.setPassProductAndProcessList(productAndProcessListTemp);
        }

        return productList;
    }

    /**
     * 展示产品列表 - 未审核 + 已审核
     *
     */
    @Override
    public List<RecordForProduct> showProductList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求参数为：{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<RecordForProduct> productList = microAnalysisMapper.selectProductList(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(productList)) {
            return Collections.emptyList();
        }

        // 一次查出所有产品的工序良品数量
        String productSeqList = productList.stream().map(RecordForProduct::getProductSeq).collect(Collectors.joining(","));
        productionQualityAnalysisParam.setProductNameOrCode(null);
        productionQualityAnalysisParam.setProductSeqList(productSeqList);
        List<RecordForProductAndProcess> recordForProductAndProcessList = microAnalysisMapper.selectProductAndProcessList(productionQualityAnalysisParam);
        Map<String, List<RecordForProductAndProcess>> productSeqMap = recordForProductAndProcessList.stream().collect(Collectors.groupingBy(RecordForProductAndProcess::getProductSeq));

        for (RecordForProduct product : productList) {
            List<RecordForProductAndProcess> recordForProductAndProcessListTemp = productSeqMap.get(product.getProductSeq());
            product.setRecordForProductAndProcessList(recordForProductAndProcessListTemp);
        }
        return productList;
    }

    /**
     * 分析页面 - 工人列表展示
     *
     */
    @Override
    public List<RecordForWorker> showWorkerList(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        log.info("请求参数为：{}", JSONObject.toJSONString(productionQualityAnalysisParam));

        List<RecordForWorker> recordForWorkerList = microAnalysisMapper.selectWorkerList(productionQualityAnalysisParam);
        if (CollectionUtils.isEmpty(recordForWorkerList)) {
            return Collections.emptyList();
        }

        return recordForWorkerList;
    }

    /**
     * 获取总的良品和不良品数量
     *
     */
    @Override
    public TotalStock obtainedTotalStock() {
        // 显式传租户（该 SQL 无 where 动态注入位置，防止跨租户合计）
        Object v = com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        return microAnalysisMapper.obtainedTotalStock(v == null ? "" : v.toString());
    }

    public AnalysisIndex obtainedCheckInformation(ProductionQualityAnalysisParam productionQualityAnalysisParam) {
        // 存放返回结果
        AnalysisIndex result;
        // 获取时间范围内的记工人数和产品数
        result = microAnalysisMapper.obtainedTotalCountsForProductAndSubmitUser(productionQualityAnalysisParam);

        // 构造查询条件
        ProductionQualityAnalysisParam param = new ProductionQualityAnalysisParam();
        param.setStartDate(productionQualityAnalysisParam.getStartDate());
        param.setEndDate(productionQualityAnalysisParam.getEndDate());
        param.setSubmitStatus(SubmitStatusEnum.UN_APPROVE.getCode());
        // 获取待审核的数量
        BigDecimal totalNumForWaitCheck = microAnalysisMapper.obtainedTotalProductCountsForSubmit(param);
        result.setWaitCheckNum(totalNumForWaitCheck);
        // 获取已审核的数量（总数量、总的不良品数量、良品数量）
        param.setSubmitStatus(SubmitStatusEnum.APPROVED.getCode());
        MicroWorkSubmitProductCount microWorkSubmitProductCount = microAnalysisMapper.obtainedTotalProductCountsForCheck(param);
        result.setCheckNum(microWorkSubmitProductCount.getTotalCounts());
        result.setPassNum(microWorkSubmitProductCount.getTotalPassNum());
        result.setNgNum(microWorkSubmitProductCount.getTotalNgNum());

        // 获取时间范围内的总报工数量 (未审核的报工数量和审核之后的报工数量)
        BigDecimal totalNum = totalNumForWaitCheck.add(microWorkSubmitProductCount.getTotalCounts());
        result.setTotalNum(totalNum);

        // 设置审产进度
        if (totalNum.signum() == 0) {
            result.setCheckProgress(BigDecimal.ZERO);
        } else {
            result.setCheckProgress(result.getCheckNum().divide(result.getTotalNum(), 3, RoundingMode.DOWN));
        }

        if (result.getCheckNum().signum() == 0) {
            result.setPassRate(BigDecimal.ZERO);
        } else {
            result.setPassRate(result.getPassNum().divide(result.getCheckNum(), 3, RoundingMode.DOWN));
        }

        return result;
    }
}
