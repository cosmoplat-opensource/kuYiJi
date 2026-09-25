/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.redis.distributedlock.utils.RedisLockHelper;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.complete.domain.entity.*;
import com.cosmo.hhim.micro.complete.domain.enums.MicroSettlementStatusEnum;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroSettlementHistoryMapper;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroSettlementReportMapper;
import com.cosmo.hhim.micro.complete.domain.service.IMicroSettlementReportService;
import com.cosmo.hhim.micro.infrastructure.util.CodeGenerateUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 计件结算报告Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-22
 */
@Slf4j
@Service
public class MicroSettlementReportServiceImpl implements IMicroSettlementReportService {
    @Autowired
    private MicroSettlementReportMapper reportMapper;
    @Autowired
    private MicroSettlementHistoryMapper historyMapper;
    @Autowired
    private RedisLockHelper redisLockHelper;

    /**
     * 报工记录审产后生成待结算报告
     *
     * @param generateList
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean generateReportAfterCheck(List<MicroSettlementGenerateDomain> generateList) {
        String operatorId = String.valueOf(SecurityUtils.getUserId());
        String targetCustomer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        List<MicroSettlementReportEntity> reportList = new ArrayList<>();
        List<MicroSettlementHistoryEntity> historyList = new ArrayList<>();
        MicroSettlementReportEntity report;
        MicroSettlementHistoryEntity history;
        String version = IdUtils.nextId();
        for (MicroSettlementGenerateDomain generateDomain : generateList) {
            if (generateDomain.getCheckedNum().signum() == 0) {
                continue;
            }
            report = new MicroSettlementReportEntity();
            report.setTenantCode(targetCustomer);
            BeanUtils.copyProperties(generateDomain, report);
            report.setSettledNum(BigDecimal.ZERO);
            report.setAdjustedNum(generateDomain.getCheckedNum());
            report.setSettlementStatus(MicroSettlementStatusEnum.OPEN.getCode());
            report.setCreatedBy(operatorId);
            report.setLastUpdBy(operatorId);
            reportList.add(report);
            history = new MicroSettlementHistoryEntity();
            BeanUtils.copyProperties(generateDomain, history);
            history.setTenantCode(targetCustomer);
            history.setAdjustedNum(generateDomain.getCheckedNum());
            history.setCreatedBy(operatorId);
            history.setVersion(version);
            historyList.add(history);
        }
        if (CollectionUtil.isNotEmpty(reportList)) {
            reportMapper.insertMicroSettlementReportBatch(reportList);
        }
        if (CollectionUtil.isNotEmpty(historyList)) {
            historyMapper.insertMicroSettlementHistoryBatch(historyList);
        }
        return true;
    }

    /**
     * 审产撤销后清理,如果未结算清理待结算数据,如果已结算,不允许撤销
     *
     * @param submitIds
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean undoSettlementReport(List<Long> submitIds) {
        List<MicroSettlementReportEntity> list = reportMapper.selectReportListBySubmitIds(submitIds);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        if (list.stream().anyMatch(a -> MicroSettlementStatusEnum.SETTLED.getCode().equals(a.getSettlementStatus()))) {
            throw new CustomException("已有结算数据,无法撤销!");
        }
//        String warningStr = list.stream().filter(a -> MicroSettlementStatusEnum.SETTLED.getCode().equals(a.getSettlementStatus()))
//                .map(this::joinWarningStr).collect(Collectors.joining("\t"));
//        if (!StringUtils.isEmpty(warningStr)) {
//            throw new CustomException(warningStr);
//        } 
        reportMapper.deleteMicroSettlementReportBySubmitIds(submitIds.toArray(new Long[0]));
        historyMapper.deleteMicroSettlementHistoryBySubmitIds(submitIds.toArray(new Long[0]));
        return true;
    }

    private String joinWarningStr(MicroSettlementReportEntity a) {
        return "产品:" + a.getOperateProcessSeq() + ",工序:" + a.getProductSeq() + ",报工ID:" + a.getSubmitId() + ",已有结算数据,无法撤销!";
    }

    /**
     * 生成结算报告
     *
     * @param toSettleList   要结算的完工产品
     * @param settlementDate 要结算的月份, 待结算数据截止到该月份的最后一天
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<MicroSettlementReportEntity> generateReport(List<MicroSettledDomain> toSettleList, Date settlementDate) {
        settlementDate = DateUtil.endOfDay(DateUtil.endOfMonth(settlementDate)).toJdkDate();
        String targetCustomer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        String lockKey = "micro:generate_report:" + targetCustomer;
        String uuid = IdUtils.fastSimpleUUID();
        long timeout = 10L;
        if (!redisLockHelper.lock(lockKey, uuid, timeout, TimeUnit.SECONDS)) {
            throw new CustomException("有其他用户正在结算，请核实后操作！");
        }
        List<String> productSeqs = toSettleList.stream().map(MicroSettledDomain::getProductSeq).collect(Collectors.toList());
        List<MicroSettlementReportEntity> reportList = reportMapper.selectToGenerateReportList(productSeqs, settlementDate);
        HashBasedTable<String, String, TreeSet<MicroSettlementReportEntity>> calcuTable = this.convertTable(reportList);
        List<MicroSettlementReportEntity> settledList = new ArrayList<>();
        List<MicroSettlementReportEntity> openList = new ArrayList<>();
        long nextId = CodeGenerateUtils.getInstance().genCode();
        String settlementNo = "JS" + nextId;
        String operatorId = String.valueOf(SecurityUtils.getUserId());
        Date lastUpdDate = new Date();
        for (MicroSettledDomain settledDomain : toSettleList) {
            BigDecimal productNum = settledDomain.getProductNum();
            BigDecimal productCompleteNum = productNum;
            String completeReportNoArrayStr = settledDomain.getCompleteReportNoArrayStr();
            String productSeq = settledDomain.getProductSeq();
            if (calcuTable.containsRow(productSeq)) {
                Map<String, TreeSet<MicroSettlementReportEntity>> row = calcuTable.row(productSeq);
                for (Map.Entry<String, TreeSet<MicroSettlementReportEntity>> processEntry : row.entrySet()) {
                    BigDecimal finalNum = productNum;
                    TreeSet<MicroSettlementReportEntity> calcuSet = processEntry.getValue();
                    for (MicroSettlementReportEntity report : calcuSet) {
                        if (BigDecimal.ZERO.compareTo(finalNum) == 0) {
                            break;
                        }
                        BigDecimal adjustedNum = report.getAdjustedNum();
                        if (finalNum.compareTo(adjustedNum) >= 0) {
                            finalNum = finalNum.subtract(adjustedNum);
                            report.setAdjustedNum(adjustedNum);
                            report.setSettledNum(adjustedNum);
                        } else {
                            //新增一条待结算的数据,待结算是未扣除数量,结算数量为空
                            BigDecimal openNum = adjustedNum.subtract(finalNum);
                            MicroSettlementReportEntity openNewReport = new MicroSettlementReportEntity();
                            BeanUtils.copyProperties(report, openNewReport);
                            openNewReport.setId(null);
                            openNewReport.setAdjustedNum(openNum);
                            openNewReport.setSettledNum(BigDecimal.ZERO);
                            openNewReport.setCreatedBy(operatorId);
                            openNewReport.setLastUpdBy(operatorId);
                            openList.add(openNewReport);
                            //取扣减数量,把能扣减的数量置为已结算数量和调整数量
                            report.setAdjustedNum(finalNum);
                            report.setSettledNum(finalNum);
                            finalNum = BigDecimal.ZERO;
                        }
                        report.setProductCompleteNum(productCompleteNum);
                        report.setProductCompleteReportNo(completeReportNoArrayStr);
                        report.setLastUpdBy(operatorId);
                        report.setLastUpdDate(lastUpdDate);
                        report.setSettlementDay(new Date());
                        report.setSettlementNo(settlementNo);
                        report.setSettlementStatus(MicroSettlementStatusEnum.SETTLED.getCode());
                        settledList.add(report);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(openList)) {
            reportMapper.insertMicroSettlementReportBatch(openList);
        }
        if (!CollectionUtils.isEmpty(settledList)) {
            settledList.forEach(s -> reportMapper.updateMicroSettlementReport(s));
        }
        //根据产品汇总并排序待结算数据
        redisLockHelper.unlock(lockKey, uuid);
        return settledList;
    }

    /**
     * 转成行列table
     * row:productSeq
     * column:operateProcessSeq
     * value: 根据报工记录排序的treeSet
     *
     * @param reportList
     * @return
     */
    private HashBasedTable<String, String, TreeSet<MicroSettlementReportEntity>> convertTable(List<MicroSettlementReportEntity> reportList) {
        HashBasedTable<String, String, TreeSet<MicroSettlementReportEntity>> calcuTable = HashBasedTable.create();
        Comparator<MicroSettlementReportEntity> comparator = Comparator.comparing(MicroSettlementReportEntity::getSubmitId);
        TreeSet<MicroSettlementReportEntity> subTree;
        for (MicroSettlementReportEntity reportEntity : reportList) {
            String productSeq = reportEntity.getProductSeq();
            String operateProcessSeq = reportEntity.getOperateProcessSeq();
            subTree = calcuTable.get(productSeq, operateProcessSeq);
            if (CollectionUtils.isEmpty(subTree)) {
                subTree = new TreeSet<>(comparator);
            }
            subTree.add(reportEntity);
            calcuTable.put(productSeq, operateProcessSeq, subTree);
        }
        return calcuTable;
    }

    /**
     * 查询人员的待结算信息
     *
     * @param entity
     * @return
     */
    @Override
    public List<MicroSettlementReportDomain> getOpenSettlementUser(MicroSettlementReportEntity entity) {
        //获取待结算所有信息,统计到人维度
        entity.setSettlementStatus(MicroSettlementStatusEnum.OPEN.getCode());
        entity.setSettlementDay(null);
        return convertSettlementUserData(entity);
    }

    /**
     * 查询人员的已结算信息
     *
     * @param entity
     * @return
     */
    @Override
    public List<MicroSettlementReportDomain> getSettledUser(MicroSettlementReportEntity entity) {
        entity.setSettlementStatus(MicroSettlementStatusEnum.SETTLED.getCode());
        return convertSettlementUserData(entity);
    }

    /**
     * 转换员工维度结算与未结算数据
     *
     * @param entity
     * @return
     */
    private List<MicroSettlementReportDomain> convertSettlementUserData(MicroSettlementReportEntity entity) {
        Map<Long, MicroSettlementReportEmployeeDomain> userMap = reportMapper.selectMicroSettlementReportByUser(entity);
        if (CollectionUtils.isEmpty(userMap)) {
            return Collections.emptyList();
        }
        List<MicroSettlementReportResultDomain> domainList = reportMapper.selectMicroSettlementReportUserDetail(entity, userMap.keySet());
        if (CollectionUtils.isEmpty(domainList)) {
            return Collections.emptyList();
        }
        HashMultimap<Long, MicroSettlementReportResultDomain> domainMap = HashMultimap.create();
        String settlementStatus = entity.getSettlementStatus();
        for (MicroSettlementReportResultDomain domain : domainList) {
            if (MicroSettlementStatusEnum.OPEN.getCode().equals(settlementStatus) && domain.getAdjustedNum().signum() == 0) {
                continue;
            }
            domainMap.put(domain.getEmployeeId(), domain);
        }
        List<MicroSettlementReportDomain> report = new ArrayList<>();
        MicroSettlementReportDomain reportDomain;
        for (Long employeeId : domainMap.keySet()) {
            Set<MicroSettlementReportResultDomain> reports = domainMap.get(employeeId);
            reportDomain = new MicroSettlementReportDomain();
            if (userMap.containsKey(employeeId)) {
                MicroSettlementReportEmployeeDomain employeeDomain = userMap.get(employeeId);
                reportDomain.setEmployeeId(employeeDomain.getEmployeeId());
                reportDomain.setEmployeeName(employeeDomain.getEmployeeName());
                reportDomain.setEmployeeUserName(employeeDomain.getEmployeeUserName());
                reportDomain.setTotalAdjustedNum(employeeDomain.getTotalAdjustedNum());
                reportDomain.setTotalSettledNum(employeeDomain.getTotalSettledNum());
                if (!CollectionUtils.isEmpty(reports)) {
                    reportDomain.setDetailList(BeanUtil.copyToList(reports, MicroSettlementDetailDomain.class));
                    report.add(reportDomain);
                }
            }
        }
        return report;
    }

    /**
     * 查询产品的已结算信息
     *
     * @param entity
     * @return
     */
    @Override
    public List<MicroSettledProductDomain> getSettledProduct(MicroSettlementReportEntity entity) {
        return reportMapper.getSettledByProduct(entity);
    }

    @Override
    public List<MicroSettledProductDetailDomain> getSettledProductDetail(String productSeq, Date searchDate) {
        List<MicroSettledProductDetailDomain> productDetail = reportMapper.selectSettledByProductDetail(productSeq, searchDate);
        return productDetail.stream().filter(p -> !CollectionUtils.isEmpty(p.getDetailList())).collect(Collectors.toList());
    }

    /**
     * 调整员工的待结算数量
     *
     * @param reportEntity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int editEmployeeSettlement(MicroSettlementReportEntity reportEntity) {
        //加锁
        //调整后总数量
        BigDecimal totalAdjustedNum = reportEntity.getAdjustedNum();
        //调整前数量
        BigDecimal oldNum = reportEntity.getCheckedNum();
        if (totalAdjustedNum.compareTo(oldNum) == 0) {
            return 1;
        }
        //总减少量
        BigDecimal reductionNum = oldNum.subtract(totalAdjustedNum);
        String remark = reportEntity.getRemark();
        Long employeeId = reportEntity.getEmployeeId();
        String productSeq = reportEntity.getProductSeq();
        String operateProcessSeq = reportEntity.getOperateProcessSeq();
        MicroSettlementReportEntity queryEntity = new MicroSettlementReportEntity();
        queryEntity.setEmployeeId(employeeId);
        queryEntity.setProductSeq(productSeq);
        queryEntity.setOperateProcessSeq(operateProcessSeq);
        queryEntity.setSettlementStatus(MicroSettlementStatusEnum.OPEN.getCode());
        List<MicroSettlementReportEntity> updateList = new ArrayList<>();
        List<MicroSettlementReportEntity> deleteList = new ArrayList<>();
        //查询所有待结的数量,报工记录按照时间正序排
        List<MicroSettlementReportEntity> reportList = reportMapper.selectMicroSettlementReportList(queryEntity);
        Date lastUpdDate = new Date();
        String operatorId = String.valueOf(SecurityUtils.getUserId());
        BigDecimal usableNum;
        for (MicroSettlementReportEntity report : reportList) {
            if (BigDecimal.ZERO.compareTo(report.getSettledNum()) != 0) {
                log.info("该条记录{}状态为未结算,但已结算数量不为0", report.getId());
                continue;
            }
            if (BigDecimal.ZERO.compareTo(reductionNum) == 0) {
                break;
            }
            report.setRemark(remark);
            //可被调整的数量
            usableNum = report.getAdjustedNum();
            report.setAdjustedFromNum(usableNum);
            if (reductionNum.compareTo(usableNum) >= 0) {
                reductionNum = reductionNum.subtract(usableNum);
                report.setAdjustedNum(BigDecimal.ZERO);
                deleteList.add(report);
            } else {
                report.setLastUpdBy(operatorId);
                report.setLastUpdDate(lastUpdDate);
                report.setAdjustedNum(usableNum.subtract(reductionNum));
                reductionNum = BigDecimal.ZERO;
                updateList.add(report);
            }
        }
        return this.saveReport(updateList, deleteList, reportEntity);
    }

    /**
     * 保存结算报告及生成历史记录
     *
     * @param updateList 要更新的待结算数据
     * @param deleteList 要删除的待结算数据
     * @param rawReport  原始请求携带的结算数据(产品+工序信息,调整备注等)
     * @return
     */
    private int saveReport(List<MicroSettlementReportEntity> updateList, List<MicroSettlementReportEntity> deleteList, MicroSettlementReportEntity rawReport) {
        String operatorId = String.valueOf(SecurityUtils.getUserId());
        String targetCustomer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        List<MicroSettlementHistoryEntity> historyList = new ArrayList<>();
        String historyVersion = IdUtils.nextId();
        //更新数据,其实只会更新一条
        for (MicroSettlementReportEntity updateReport : updateList) {
            historyList.add(this.covertToHistory(operatorId, targetCustomer, updateReport, rawReport, historyVersion));
            reportMapper.updateMicroSettlementReport(updateReport);
        }
        //删除数据
        if (!CollectionUtils.isEmpty(deleteList)) {
            Long[] ids = new Long[deleteList.size()];
            MicroSettlementReportEntity deleteReport;
            for (int i = 0; i < deleteList.size(); i++) {
                deleteReport = deleteList.get(i);
                ids[i] = deleteReport.getId();
                historyList.add(this.covertToHistory(operatorId, targetCustomer, deleteReport, rawReport, historyVersion));
            }
            reportMapper.deleteMicroSettlementReportByIds(ids);
        }
        historyMapper.insertMicroSettlementHistoryBatch(historyList);
        return 1;
    }

    private MicroSettlementHistoryEntity covertToHistory(String operatorId, String tenantCode, MicroSettlementReportEntity operateReport, MicroSettlementReportEntity rawReport, String historyVersion) {
        MicroSettlementHistoryEntity historyEntity = new MicroSettlementHistoryEntity();
        historyEntity.setTenantCode(tenantCode);
        historyEntity.setCreatedBy(operatorId);
        historyEntity.setSubmitId(operateReport.getSubmitId());
        historyEntity.setEmployeeId(operateReport.getEmployeeId());
        historyEntity.setAdjustedFromNum(operateReport.getAdjustedFromNum());
        historyEntity.setAdjustedNum(operateReport.getAdjustedNum());
        historyEntity.setProductSeq(rawReport.getProductSeq());
        historyEntity.setOperateProcessSeq(rawReport.getOperateProcessSeq());
        historyEntity.setRemark(rawReport.getRemark());
        historyEntity.setVersion(historyVersion);
        historyEntity.setProductCode(rawReport.getProductCode());
        historyEntity.setProductUnit(rawReport.getProductUnit());
        historyEntity.setProductName(rawReport.getProductName());
        historyEntity.setOperateProcessCode(rawReport.getOperateProcessCode());
        historyEntity.setOperateProcessName(rawReport.getOperateProcessName());
        return historyEntity;
    }

    @Override
    public List<MicroSettlementHistoryQueryEntity> getReportEditHistory(MicroSettlementReportEntity reportEntity, String searchKey) {
        if (reportEntity.getEmployeeId() != null || !StringUtils.isEmpty(reportEntity.getProductSeq()) || !StringUtils.isEmpty(reportEntity.getOperateProcessSeq())) {
            searchKey = null;
        }
        Map<Long, MicroSettlementHistoryQueryEntity> historyMap = historyMapper.selectHistoryListGroupByUser(reportEntity, searchKey);
        if (CollectionUtils.isEmpty(historyMap)) {
            return Collections.emptyList();
        }
        List<MicroSettlementHistoryQuerySubEntity> detailList = historyMapper.selectHistorySubListGroupByUser(reportEntity, historyMap.keySet());
        Map<Long, List<MicroSettlementHistoryQuerySubEntity>> detailMap = detailList.stream().collect(Collectors.groupingBy(MicroSettlementHistoryQuerySubEntity::getEmployeeId));
        List<MicroSettlementHistoryQueryEntity> result = new ArrayList<>();
        MicroSettlementHistoryQueryEntity entity;
        for (Map.Entry<Long, List<MicroSettlementHistoryQuerySubEntity>> entry : detailMap.entrySet()) {
            Long employeeId = entry.getKey();
            List<MicroSettlementHistoryQuerySubEntity> values = entry.getValue();
            if (historyMap.containsKey(employeeId) && !CollectionUtils.isEmpty(values)) {
                entity = historyMap.get(employeeId);
                entity.setDetailList(values);
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public List<MicroSettlementHistoryEntity> getReportEditHistoryDetail(MicroSettlementHistoryEntity history) {
        return historyMapper.selectMicroSettlementHistoryDetailList(history);
    }

}
