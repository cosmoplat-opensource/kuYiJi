/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.complete.impl;

import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.micro.application.assembler.complete.MicroSettlementAssembler;
import com.cosmo.hhim.micro.application.dto.complete.*;
import com.cosmo.hhim.micro.application.service.complete.IMicroSettlementFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.complete.domain.entity.*;
import com.cosmo.hhim.micro.complete.domain.enums.MicroSettlementStatusEnum;
import com.cosmo.hhim.micro.complete.domain.service.IMicroCompleteReportService;
import com.cosmo.hhim.micro.complete.domain.service.IMicroSettlementReportService;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.enums.CompleteReportStateEnum;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 */
@Service
@RequiredArgsConstructor
public class MicroSettlementFacadeServiceImpl implements IMicroSettlementFacadeService {

    private final IMicroSettlementReportService settlementReportService;
    private final IMicroCompleteReportService reportService;
    private final IMicroEmailService microEmailService;
    private final IMicroCompleteReportService microCompleteReportService;
    private final MicroProductMapper microProductMapper;
    private final MicroWorkSubmitMapper microWorkSubmitMapper;
    private final MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generate(MicroSettlementGenerateQueryDTO generateQueryDTO) {
        MicroSettlementReportEntity settleQuery = new MicroSettlementReportEntity();
        //和前端约定,这里的startDate传递查询月份的第一天
        Date settlementDate = generateQueryDTO.getStartDate();
        int settlementYear = DateUtil.year(generateQueryDTO.getStartDate());
        int calculateMonth = validSettlementMonth(settlementDate);
        settleQuery.setSettlementDay(settlementDate);
        settleQuery.setSettlementStatus(MicroSettlementStatusEnum.SETTLED.getCode());
        //查询所有完工报告的产品完工数据 
        MicroCompleteReportStatisticsParam queryParam = new MicroCompleteReportStatisticsParam();
        queryParam.setEndDate(DateUtil.endOfMonth(generateQueryDTO.getEndDate()).toJdkDate());
        queryParam.setStartDate(settlementDate);
        queryParam.setOnlyNormal(true);
        List<MicroCompleteReportStatisticsProductResult> completeProducts = reportService.selectMicroCompleteNumGroupByProduct(queryParam);
        if (CollectionUtils.isEmpty(completeProducts)) {
            throw new CustomException(settlementYear + "年" + calculateMonth + "月无可结算数据，请检查" + calculateMonth + "月是否已生成完工报告？");
        }
        //生成结算报告
        List<MicroSettlementReportEntity> settledList = settlementReportService.generateReport(MicroSettlementAssembler.completeToSettleDomain(completeProducts), queryParam.getStartDate());
        // 生成针对报工记录的结算日志
        if (CollectionUtil.isNotEmpty(settledList)) {
            Long[] ids = settledList.stream().map(MicroSettlementReportEntity::getSubmitId)
                    .distinct().toArray(Long[]::new);
            // 查询报工记录
            List<MicroWorkSubmit> microWorkSubmitList = microWorkSubmitMapper.selectMicroWorkSubmitByIds(ids);
            // 组装报工变动历史
            List<MicroWorkSubmitHistory> microWorkSubmitHistoryList = MicroSettlementAssembler
                    .settlementReportEntityAndWorkSubmitCovert2MicroWorkSubmitHistory(microWorkSubmitList, settledList);
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistoryBatch(microWorkSubmitHistoryList);
        }
        Set<String> completeReportOrders = completeProducts.stream()
                .flatMap(entity -> Arrays.stream(entity.getReportNoArrayStr().split(",")))
                .collect(Collectors.toSet());
        MicroCompleteReport completeReport;
        for (String reportOrder : completeReportOrders) {
            completeReport = new MicroCompleteReport();
            completeReport.setReportNo(reportOrder);
            completeReport.setState(CompleteReportStateEnum.SETTLED.getCode());
            completeReport.setLastUpdDate(new Date());
            reportService.updateMicroCompleteReport(completeReport);
        }
        return 1;
    }

    /**
     * 结算报告校验
     */
    private int validSettlementMonth(Date validDate) {
        int month = DateUtil.month(validDate);
        return ++month;
    }

    @Override
    public List<MicroSettlementReportDomain> getOpenSettlementUser(MicroSettlementQueryDTO queryDTO) {
        return settlementReportService.getOpenSettlementUser(MicroSettlementAssembler.toReportEntity(queryDTO));
    }

    @Override
    public List<MicroSettlementReportDomain> getSettledUser(MicroSettlementQueryDTO queryDTO) {
        return settlementReportService.getSettledUser(MicroSettlementAssembler.toReportEntity(queryDTO));
    }

    @Override
    public List<MicroSettledProductDomain> getSettledProduct(MicroSettlementQueryDTO queryDTO) {
        return settlementReportService.getSettledProduct(MicroSettlementAssembler.toReportEntity(queryDTO));
    }

    @Override
    public List<MicroSettledProductDetailDomain> getSettledProductDetail(MicroSettlementQueryDTO queryDTO) {
        if (StringUtils.isEmpty(queryDTO.getProductSeq())) {
            throw new CustomException("无法获取产品信息");
        }
        return settlementReportService.getSettledProductDetail(queryDTO.getProductSeq(), queryDTO.getSearchDate());
    }

    @Override
    public int editEmployeeSettlement(MicroSettlementModifyDTO modifyDTO) {
        MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(modifyDTO.getProductSeq());
        String unit;
        if (microProduct == null) {
            throw new CustomException("无法获取该产品信息");
        } else {
            unit = microProduct.getUnit();
        }
        MicroSettlementReportEntity reportDTO = MicroSettlementAssembler.toReportDTO(modifyDTO);
        reportDTO.setProductUnit(unit);
        return settlementReportService.editEmployeeSettlement(reportDTO);
    }

    /**
     * 获取结算历史列表
     */
    @Override
    public List<MicroSettlementHistoryDTO> getReportEditHistory(MicroSettlementQueryDTO queryDTO) {
        List<MicroSettlementHistoryQueryEntity> queryEntities = settlementReportService.getReportEditHistory(MicroSettlementAssembler.toReportEntity(queryDTO), queryDTO.getSearchKey());
        List<MicroSettlementHistoryDTO> historyDTOList = new ArrayList<>();
        MicroSettlementHistoryDTO historyDTO;
        for (MicroSettlementHistoryQueryEntity queryEntity : queryEntities) {
            historyDTO = new MicroSettlementHistoryDTO();
            historyDTO.setEmployeeId(queryEntity.getEmployeeId());
            historyDTO.setEmployeeName(queryEntity.getEmployeeName());
            historyDTO.setEmployeeUserName(queryEntity.getEmployeeUserName());
            historyDTO.setDetailList(BeanUtil.copyToList(queryEntity.getDetailList(), MicroSettlementHistorySubDTO.class));
            historyDTOList.add(historyDTO);
        }
        return historyDTOList;
    }

    /**
     * 获取调整历史明细
     */
    @Override
    public List<MicroSettlementHistoryDetailDTO> getReportEditHistoryDetail(Long userId, String productSeq, String operateProcessSeq, Date searchDate) {
        MicroSettlementHistoryEntity history = new MicroSettlementHistoryEntity();
        history.setEmployeeId(userId);
        history.setProductSeq(productSeq);
        history.setOperateProcessSeq(operateProcessSeq);
        history.setCreatedDate(searchDate);
        List<MicroSettlementHistoryEntity> domainList = settlementReportService.getReportEditHistoryDetail(history);
        return BeanUtil.copyToList(domainList, MicroSettlementHistoryDetailDTO.class);
    }


    /**
     * 导出已结算报告
     */
    @Override
    public String exportSettledReport(MicroSettlementQueryDTO queryDTO) {
        HashMap<String, Object> map1 = new HashMap<>(8);
        ExportParams p1 = new ExportParams();
        p1.setSheetName("产品维度");
        p1.setType(ExcelType.XSSF);
        map1.put("title", p1);
        map1.put("entity", MicroSettledProductExportDTO.class);
        List<MicroSettledProductExportDTO> settledProduct;
        List<MicroSettledProductDomain> settledProductDomainList = settlementReportService.getSettledProduct(MicroSettlementAssembler.toReportEntity(queryDTO));
        if (CollectionUtils.isEmpty(settledProductDomainList)) {
            settledProduct = new ArrayList<>();
        } else {
            settledProduct = BeanUtil.copyToList(settledProductDomainList, MicroSettledProductExportDTO.class);
        }
        map1.put("data", settledProduct);
        HashMap<String, Object> map2 = new HashMap<>(8);
        ExportParams p2 = new ExportParams();
        p2.setSheetName("员工维度");
        p2.setType(ExcelType.XSSF);
        map2.put("title", p2);
        map2.put("entity", MicroSettlementUserDetailExportDTO.class);
        List<MicroSettlementUserDetailExportDTO> settledUser;
        List<MicroSettlementReportDomain> settledUserDomainList = settlementReportService.getSettledUser(MicroSettlementAssembler.toReportEntity(queryDTO));
        if (CollectionUtils.isEmpty(settledUserDomainList)) {
            settledUser = new ArrayList<>();
        } else {
            settledUser = MicroSettlementAssembler.toExportDetailDTO(settledUserDomainList, MicroSettlementStatusEnum.SETTLED);
        }
        map2.put("data", settledUser);
        List<Map<String, Object>> multiSheetList = new ArrayList<>();
        multiSheetList.add(map1);
        multiSheetList.add(map2);

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.SETTLED_REPORT_EXCEL_NAME);
        // 导出并发送邮件
        return microEmailService.exportAndSendEmailByMultiSheet(MailConstants.SETTLED_REPORT_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, queryDTO.getReceivedBy(), multiSheetList, placeholderMap);
    }

    /**
     * 计件待结算导出模块
     */
    @Override
    public String exportOpenSettlementReport(MicroSettlementQueryDTO queryDTO) {
        HashMap<String, Object> map1 = new HashMap<>(8);
        ExportParams p1 = new ExportParams();
        p1.setSheetName("产品维度");
        p1.setType(ExcelType.XSSF);
        map1.put("title", p1);
        map1.put("entity", MicroOpenProductExportDTO.class);
        map1.put("data", this.getOpenProductData(queryDTO.getSearchDate()));
        HashMap<String, Object> map2 = new HashMap<>(8);
        ExportParams p2 = new ExportParams();
        p2.setSheetName("员工维度");
        p2.setType(ExcelType.XSSF);
        map2.put("title", p2);
        map2.put("entity", MicroSettlementUserDetailExportDTO.class);
        List<MicroSettlementUserDetailExportDTO> settledUser;
        List<MicroSettlementReportDomain> settledUserDomainList = settlementReportService.getOpenSettlementUser(MicroSettlementAssembler.toReportEntity(queryDTO));
        if (CollectionUtils.isEmpty(settledUserDomainList)) {
            settledUser = new ArrayList<>();
        } else {
            settledUser = MicroSettlementAssembler.toExportDetailDTO(settledUserDomainList, MicroSettlementStatusEnum.OPEN);
        }
        map2.put("data", settledUser);
        List<Map<String, Object>> multiSheetList = new ArrayList<>();
        multiSheetList.add(map1);
        multiSheetList.add(map2);

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.OPEN_SETTLEMENT_REPORT_EXCEL_NAME);
        // 导出并发送邮件
        return microEmailService.exportAndSendEmailByMultiSheet(MailConstants.OPEN_SETTLEMENT_REPORT_MAIL_SUBJECT,
                MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, queryDTO.getReceivedBy(), multiSheetList, placeholderMap);
    }


    /**
     * 获取待结算产品数据
     */
    private List<MicroOpenProductExportDTO> getOpenProductData(Date searchDate) {
        List<MicroOpenProductExportDTO> openProduct;
        MicroCompleteReportStatisticsParam queryParam = new MicroCompleteReportStatisticsParam();
        queryParam.setStartDate(DateUtil.beginOfMonth(searchDate).toJdkDate());
        queryParam.setEndDate(DateUtil.endOfMonth(searchDate).toJdkDate());
        queryParam.setOnlyNormal(true);
        List<MicroCompleteReportStatisticsProductResult> resultList = microCompleteReportService.selectMicroCompleteNumGroupByProduct(queryParam);
        if (CollectionUtils.isEmpty(resultList)) {
            return Collections.emptyList();
        }
        // 2.给产品编码和产品单位赋值
        List<MicroCompleteReportStatisticsProductResult> settledProductDomainList = resultList.stream().peek(e -> {
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(e.getProductSeq());
            if (null != microProduct) {
                e.setProductCode(microProduct.getProductCode());
                e.setProductUnit(microProduct.getUnit());
            }
        }).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(settledProductDomainList)) {
            openProduct = new ArrayList<>();
        } else {
            openProduct = BeanUtil.copyToList(settledProductDomainList, MicroOpenProductExportDTO.class);
        }
        return openProduct;
    }

    @Override
    public MicroSettlementHomeIndexDTO homeIndex() {
        MicroSettlementHomeIndexDTO result = new MicroSettlementHomeIndexDTO();
        MicroCompleteReportStatisticsParam queryParam = new MicroCompleteReportStatisticsParam();
        queryParam.setEndDate(DateUtil.endOfMonth(DateUtil.offset(new Date(), DateField.MONTH, -1)).toJdkDate());
        queryParam.setOnlyNormal(true);
        List<MicroCompleteReportStatisticsProductResult> completeProductList = microCompleteReportService.selectMicroCompleteNumGroupByProduct(queryParam);
        if (CollectionUtils.isEmpty(completeProductList)) {
            return result;
        }
        result.setProductNum((long) completeProductList.size());
        result.setOpenSettlementNum(completeProductList
                .stream()
                .map(MicroCompleteReportStatisticsProductResult::getCompleteTotalNum)
                .reduce(BigDecimal::add)
                .get().setScale(4, RoundingMode.HALF_UP));
        return result;
    }

}
