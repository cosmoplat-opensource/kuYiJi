/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.complete.impl;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportAndSubmitResult;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportDetailInfo;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportPlanWaitParam;
import com.cosmo.hhim.micro.application.dto.complete.MicroWaitCompleteReportIndexResult;
import com.cosmo.hhim.micro.application.service.complete.IMicroCompleteReportFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroManufactureLine;
import com.cosmo.hhim.micro.base.domain.entity.factory.MicroWorkShop;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorage;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageDto;
import com.cosmo.hhim.micro.base.domain.entity.storage.MicroProcessStorageHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitHistory;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealQueryDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealResultDto;
import com.cosmo.hhim.micro.base.domain.mapper.common.MicroProductMapper;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroManufactureLineMapper;
import com.cosmo.hhim.micro.base.domain.mapper.factory.MicroWorkShopMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessCommonMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.process.MicroProcessStorageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitHistoryMapper;
import com.cosmo.hhim.micro.base.domain.mapper.submit.MicroWorkSubmitMapper;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroEmailService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.base.domain.service.process.IMicroProcessStorageService;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReport;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportBasicParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportDetailGroupInfo;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportExportResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupPlanResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideDetailResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsProductResult;
import com.cosmo.hhim.micro.complete.domain.service.IMicroCompleteReportService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.MailConstants;
import com.cosmo.hhim.micro.infrastructure.enums.CompleteReportStateEnum;
import com.cosmo.hhim.micro.infrastructure.enums.FinishStorageChangeTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.IsCompleteEnum;
import com.cosmo.hhim.micro.infrastructure.util.MicroEmailUtils;
import com.cosmo.hhim.micro.infrastructure.util.MicroPageUtils;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrderParam;
import com.cosmo.hhim.micro.planning.domain.mapper.work.MicroManufactureWorkOrderMapper;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishProductStorageAdjustParam;
import com.cosmo.hhim.micro.storage.domain.entity.MicroFinishedProductStorage;
import com.cosmo.hhim.micro.storage.domain.service.IMicroFinishedProductStorageService;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MicroCompleteReportFacadeServiceImpl implements IMicroCompleteReportFacadeService {

    private final IMicroCompleteReportService microCompleteReportService;
    private final IMicroWorkSubmitService microWorkSubmitService;
    private final MicroWorkSubmitMapper microWorkSubmitMapper;
    private final IMicroUserService microUserService;
    private final MicroProcessStorageMapper microProcessStorageMapper;
    private final MicroProcessStorageHistoryMapper microProcessStorageHistoryMapper;
    private final IMicroProcessStorageService microProcessStorageService;
    private final IMicroEmailService microEmailService;
    private final IMicroFinishedProductStorageService microFinishedProductStorageService;
    private final MicroWorkSubmitHistoryMapper microWorkSubmitHistoryMapper;
    private final MicroProductMapper microProductMapper;
    private final MicroProcessCommonMapper microProcessCommonMapper;
    private final MicroManufactureWorkOrderMapper microManufactureWorkOrderMapper;
    private final MicroManufactureLineMapper microManufactureLineMapper;
    private final MicroWorkShopMapper microWorkShopMapper;
    private static final String CHAR_SPILT = ";";

    /**
     * 查询待处理的完工产品列表（工易派）
     *
     * @param queryParam
     */
    @Override
    public List<MicroManufactureWorkOrder> waitDealList(MicroCompleteReportPlanWaitParam queryParam) {

        // 1.查询生产工单信息
        MicroManufactureWorkOrderParam workOrderParam = new MicroManufactureWorkOrderParam();
        workOrderParam.setOrderOrWorkNo(queryParam.getOrderOrWorkNo());
        List<MicroManufactureWorkOrder> resultList = microManufactureWorkOrderMapper.selectNotCompleteMicroManufactureWorkOrderList(workOrderParam);
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }

        // 2.产品名称、产品单位、车间名称、产线名称赋值
        for (MicroManufactureWorkOrder result : resultList) {
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(result.getProductSeq());
            if (null != microProduct && StringUtils.hasText(microProduct.getProductName())) {
                result.setProductName(microProduct.getProductName());
            }

            if (null != microProduct && StringUtils.hasText(microProduct.getUnit())) {
                result.setUnit(microProduct.getUnit());
            }

            MicroWorkShop microWorkShop = microWorkShopMapper.selectMicroWorkshopByCode(result.getWshopCode());
            if (null != microWorkShop && StringUtils.hasText(microWorkShop.getWshopName())) {
                result.setWshopName(microWorkShop.getWshopName());
            }

            MicroManufactureLine microManufactureLine = microManufactureLineMapper.selectMicroManufactureLineByWshopCodeAndLineCode(result.getWshopCode(), result.getMlineCode());
            if (null != microManufactureLine && StringUtils.hasText(microManufactureLine.getMlineName())) {
                result.setMlineName(microManufactureLine.getMlineName());
            }
        }

        return resultList;
    }

    /**
     * 查询待处理的完工产品列表
     *
     * @param queryDto
     */
    @Override
    public List<WaitDealResultDto> waitDealList(WaitDealQueryDto queryDto) {
        return microWorkSubmitMapper.selectLastProcessWorkSubmitInfos(queryDto);
    }

    /**
     * 待确认完工报告-导出并发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Override
    public String exportCompleteReportWaitDealRecord(String receivedBy, WaitDealQueryDto queryDto) {
        log.info("请求参数为--->receivedBy:{}", receivedBy);
        // 校验邮箱格式
        if (!MicroEmailUtils.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建导出数据
        List<WaitDealResultDto> resultList = microWorkSubmitMapper.selectLastProcessWorkSubmitInfos(queryDto);
        resultList = resultList.stream().peek(e -> {
            // 查询审核人昵称
            if (StringUtils.hasText(e.getCheckUser())) {
                MicroUser microUser = microUserService.selectMicroUserById(Long.parseLong(e.getCheckUser()));
                if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                    e.setCheckUserNickName(microUser.getNickName());
                }
            }
            // 查询质检人昵称
            if (StringUtils.hasText(e.getQcUser())) {
                MicroUser microUser = microUserService.selectMicroUserById(Long.parseLong(e.getQcUser()));
                if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                    e.setQcUserNickName(microUser.getNickName());
                }
            }

        }).collect(Collectors.toList());

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.WAIT_COMPLETE_REPORT_EXCEL_NAME);

        // 导出并发送邮件
        return microEmailService.exportAndSendEmail(MailConstants.COMPLETE_REPORT_MAIL_WAIT_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, resultList, WaitDealResultDto.class, placeholderMap);
    }

    /**
     * 统计待处理的完工指标
     *
     * @return
     */
    @Override
    public MicroWaitCompleteReportIndexResult statisticsWaitCompleteReportIndex(WaitDealQueryDto queryDto) {
        MicroWaitCompleteReportIndexResult result = new MicroWaitCompleteReportIndexResult();

        // 1.查询未完工已审核的产品尾序报工记录列表
        List<WaitDealResultDto> resultDtoList = microWorkSubmitMapper.selectLastProcessWorkSubmitInfos(queryDto);
        if (CollectionUtils.isEmpty(resultDtoList)) {
            return result;
        }

        // 2.统计产品款数
        long productCategoryNum = resultDtoList.stream().map(WaitDealResultDto::getProductSeq).distinct().count();

        result.setProductCategoryNum(productCategoryNum);
        result.setWaitDealSubmitTotalNum((long) (resultDtoList.size()));
        return result;
    }

    /**
     * 查询完工产品列表（KU易记）
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportGroupResult> finishDealList(MicroCompleteReportParam queryParam) {
        List<MicroCompleteReportGroupResult> groupResults = microCompleteReportService.selectMicroCompleteReportList(queryParam);

        // 用户昵称赋值
        if (!CollectionUtils.isEmpty(groupResults)) {
            List<MicroCompleteReportGroupResult> collect = groupResults.stream().peek(e -> {
                MicroUser microUser = microUserService.selectMicroUserById(e.getCompleteUser());
                if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                    e.setCompleteUserNickName(microUser.getNickName());
                }
            }).collect(Collectors.toList());
            groupResults = MicroPageUtils.listToPage(groupResults, collect);
        }
        return groupResults;
    }

    /**
     * 查询完工产品列表(KU易记)-产品维度
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportProductGroupResult> finishDealProductSideList(MicroCompleteReportProductSideParam queryParam) {
        return microCompleteReportService.selectMicroCompleteReportProductSideList(queryParam);
    }

    /**
     * 获取完工报告单详细信息列表（KU易记）--产品维度
     *
     * @param param
     * @return
     */
    @Override
    public List<MicroCompleteReportProductSideDetailResult> getDetailInfoForProductSide(MicroCompleteReportBasicParam param) {
        List<MicroCompleteReportProductSideDetailResult> resultList = Lists.newArrayList();

        // 1.查询完工报告表
        List<MicroCompleteReport> microCompleteReports = microCompleteReportService.selectMicroCompleteReportNormalList(param);
        if (CollectionUtils.isEmpty(microCompleteReports)) {
            return resultList;
        }
        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            MicroCompleteReportProductSideDetailResult result = new MicroCompleteReportProductSideDetailResult();

            // 查询完工人昵称
            MicroUser microUser = microUserService.selectMicroUserById(microCompleteReport.getCompleteUser());
            if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                result.setCompleteUser(microUser.getNickName());
            }
            result.setCompleteTime(microCompleteReport.getCompleteTime());
            result.setCompleteNum(microCompleteReport.getCompleteNum());

            // 2.查询报工记录表
            MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitById(microCompleteReport.getWorkSubmitId());
            if (null != microWorkSubmit) {
                // 查询报工人昵称
                MicroUser microUserInfo = microUserService.selectMicroUserById(Long.valueOf(microWorkSubmit.getSubmitUser()));
                if (null != microUserInfo && StringUtils.hasText(microUserInfo.getNickName())) {
                    result.setSubmitUser(microUserInfo.getNickName());
                }
                result.setSubmitTime(microWorkSubmit.getCreatedDate());
            }
            resultList.add(result);
        }

        return MicroPageUtils.listToPage(microCompleteReports, resultList);
    }

    /**
     * 查询完工产品列表（工易派）
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportGroupPlanResult> finishDealPlanList(MicroCompleteReportParam queryParam) {
        // 1.查询工单关联的完工报告单列表
        List<MicroCompleteReportGroupPlanResult> completeReportGroupPlanResultList = microCompleteReportService.selectMicroCompleteReportPlanList(queryParam);

        // 2.产品、车间、产线、用户昵称赋值
        if (!CollectionUtils.isEmpty(completeReportGroupPlanResultList)) {
            List<MicroCompleteReportGroupPlanResult> collect = completeReportGroupPlanResultList.stream().peek(e -> {
                // 完工报告操作人昵称
                MicroUser microUser = microUserService.selectMicroUserById(e.getCompleteUser());
                if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                    e.setCompleteUserNickName(microUser.getNickName());
                }

                // 产品编码、产品名称、产品单位
                MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(e.getProductSeq());
                if (null != microProduct) {
                    e.setProductCode(microProduct.getProductCode());
                    e.setProductName(microProduct.getProductName());
                    e.setUnit(microProduct.getUnit());
                }

                // 车间名称
                MicroWorkShop microWorkShop = microWorkShopMapper.selectMicroWorkshopByCode(e.getWshopCode());
                if (null != microWorkShop && StringUtils.hasText(microWorkShop.getWshopName())) {
                    e.setWshopName(microWorkShop.getWshopName());
                }

                // 产线名称
                MicroManufactureLine microManufactureLine = microManufactureLineMapper.selectMicroManufactureLineByWshopCodeAndLineCode(e.getWshopCode(), e.getMlineCode());
                if (null != microManufactureLine && StringUtils.hasText(microManufactureLine.getMlineName())) {
                    e.setMlineName(microManufactureLine.getMlineName());
                }
            }).collect(Collectors.toList());
            completeReportGroupPlanResultList = MicroPageUtils.listToPage(completeReportGroupPlanResultList, collect);
        }

        return completeReportGroupPlanResultList;
    }

    /**
     * 查询完工报告+报工记录中的相关信息
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportAndSubmitResult> selectMicroCompleteReportAndSubmitInfos(MicroCompleteReportParam queryParam) {
        List<MicroCompleteReportAndSubmitResult> resultList = Lists.newArrayList();

        // 1.查询完工报告记录常规列表
        List<MicroCompleteReport> microCompleteReports = microCompleteReportService.selectMicroCompleteReportNormalList(queryParam);
        if (CollectionUtils.isEmpty(microCompleteReports)) {
            return resultList;
        }

        // 2.组装完工报告+报工记录信息结构
        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            MicroCompleteReportAndSubmitResult result = new MicroCompleteReportAndSubmitResult();

            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(microCompleteReport.getProductSeq());
            if (null != microProduct && StringUtils.hasText(microProduct.getProductCode())) {
                result.setProductCode(microProduct.getProductCode());
            }
            result.setProductName(microCompleteReport.getProductName());
            result.setCompleteNum(microCompleteReport.getCompleteNum());
            result.setCompleteTime(microCompleteReport.getCompleteTime());
            result.setCompleteUserNickName(queryNickNameByUserId(microCompleteReport.getCompleteUser()));

            // 查询报工记录信息
            MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitById(microCompleteReport.getWorkSubmitId());
            if (null != microWorkSubmit) {
                if (StringUtils.hasText(microWorkSubmit.getSubmitUser())) {
                    result.setSubmitUserNickName(queryNickNameByUserId(Long.parseLong(microWorkSubmit.getSubmitUser())));
                }
                if (StringUtils.hasText(microWorkSubmit.getCheckUser())) {
                    result.setCheckUserNickName(queryNickNameByUserId(Long.parseLong(microWorkSubmit.getCheckUser())));
                }
                result.setCheckDate(microWorkSubmit.getCheckDate());
                result.setCheckStatus(microWorkSubmit.getCheckStatus());
                if (StringUtils.hasText(microWorkSubmit.getQcUser())) {
                    result.setQcUserNickName(queryNickNameByUserId(Long.parseLong(microWorkSubmit.getQcUser())));
                }
                result.setQcDate(microWorkSubmit.getQcDate());
                result.setRemark(microWorkSubmit.getRemark());
            }

            resultList.add(result);
        }


        return resultList;
    }

    /**
     * 根据用户Id查询用户昵称
     *
     * @return
     */
    private String queryNickNameByUserId(Long userId) {
        MicroUser microUser = microUserService.selectMicroUserById(userId);
        if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
            return microUser.getNickName();
        }
        return null;
    }


    /**
     * 已完成完工报告-导出并发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Override
    public String exportCompleteReportFinishDealRecord(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!MicroEmailUtils.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }

        // 构建导出数据
        MicroCompleteReportParam queryParam = new MicroCompleteReportParam();
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);
        queryParam.setSourceChannel(SecurityUtils.getApplicationSign());
        List<MicroCompleteReportAndSubmitResult> resultList = this.selectMicroCompleteReportAndSubmitInfos(queryParam);

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.FINISH_COMPLETE_REPORT_EXCEL_NAME);

        // 导出并发送邮件
        return microEmailService.exportAndSendEmail(MailConstants.COMPLETE_REPORT_MAIL_FINISH_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, resultList, MicroCompleteReportAndSubmitResult.class, placeholderMap);
    }

    /**
     * 查询完工产品详细信息-按照产品分组
     *
     * @param reportNo
     * @return
     */
    @Override
    public List<MicroCompleteReportDetailGroupInfo> getDetailGroupInfo(String reportNo) {
        return microCompleteReportService.selectGroupMicroCompleteReportByReportNo(reportNo);
    }

    /**
     * 查询完成产品详细信息
     *
     * @param reportNo
     * @return
     */
    @Override
    public List<MicroCompleteReportDetailInfo> getDetailInfo(String reportNo) {
        List<MicroCompleteReportDetailInfo> resultList = Lists.newArrayList();

        // 1.根据完工单号查询完工报告单列表
        List<MicroCompleteReport> microCompleteReports = microCompleteReportService.selectMicroCompleteReportByReportNo(reportNo);
        if (CollectionUtils.isEmpty(microCompleteReports)) {
            return resultList;
        }

        // 2.根据完工报告单记录的报工记录ID查询报工记录信息
        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            MicroCompleteReportDetailInfo result = new MicroCompleteReportDetailInfo();
            MicroWorkSubmit microWorkSubmit = microWorkSubmitService.selectMicroWorkSubmitById(microCompleteReport.getWorkSubmitId());

            result.setProductSeq(microCompleteReport.getProductSeq());
            result.setProductName(microCompleteReport.getProductName());
            result.setCompleteNum(microCompleteReport.getCompleteNum());
            if (null != microWorkSubmit) {
                result.setSubmitWorkTime(microWorkSubmit.getCreatedDate());

                // 3.查询报工人昵称
                MicroUser microUser = microUserService.selectMicroUserById(Long.valueOf(microWorkSubmit.getSubmitUser()));
                if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                    result.setSubmitWorkNickName(microUser.getNickName());
                }
            }
            resultList.add(result);
        }
        return resultList;
    }

    /**
     * 新增完工单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addCompleteReport(List<MicroCompleteReport> microCompleteReports) { 
        Date nowDate = DateUtils.getNowDate();
        String userId = SecurityUtils.getUserId().toString();

        // 1.新增完工单
        String reportNo = microCompleteReportService.insertMicroCompleteReportBatch(microCompleteReports);
        String reason = "完工报告" + reportNo + "入库";

        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            // 查询报工记录信息
            MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitById(microCompleteReport.getWorkSubmitId());
            String productSeq = microWorkSubmit.getProductSeq();
            String processSeq = microWorkSubmit.getOperateProcessSeq();
            microCompleteReport.setProductSeqAndProcessSeq(productSeq + CHAR_SPILT + processSeq);

            // 2.尾序报工工记录标记为已完工
            MicroWorkSubmit updateParam = new MicroWorkSubmit();
            updateParam.setId(microCompleteReport.getWorkSubmitId());
            updateParam.setLastUpdBy(userId);
            updateParam.setLastUpdDate(nowDate);
            updateParam.setIsComplete(IsCompleteEnum.YES.getCode());
            microWorkSubmitMapper.updateWorkSubmitCompleteFlag(updateParam);

            // 3.新增报工记录历史变动记录
            MicroWorkSubmitHistory insertWorkSubmitHistory = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(microWorkSubmit, insertWorkSubmitHistory);
            // 查产品基础信息
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(microWorkSubmit.getProductSeq());
            if (null != microProduct) {
                insertWorkSubmitHistory.setProductCode(microProduct.getProductCode());
                insertWorkSubmitHistory.setProductName(microProduct.getProductName());
            }
            // 查工序基础信息
            MicroProcessCommon preProcessCommonInfo = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(microWorkSubmit.getPreProcessSeq());
            if (null != preProcessCommonInfo) {
                insertWorkSubmitHistory.setPreProcessCode(preProcessCommonInfo.getProcessCode());
                insertWorkSubmitHistory.setPreProcessName(preProcessCommonInfo.getProcessName());
            }
            MicroProcessCommon currentProcessCommonInfo = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(microWorkSubmit.getOperateProcessSeq());
            if (null != currentProcessCommonInfo) {
                insertWorkSubmitHistory.setOperateProcessCode(currentProcessCommonInfo.getProcessCode());
                insertWorkSubmitHistory.setOperateProcessName(currentProcessCommonInfo.getProcessName());
            }
            insertWorkSubmitHistory.setCreatedBy(userId);
            insertWorkSubmitHistory.setCreatedDate(nowDate);
            insertWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_INBOUND_COMPLETE);
            insertWorkSubmitHistory.setIsComplete(IsCompleteEnum.YES.getCode());
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(insertWorkSubmitHistory);
        }

        // 按照产品+工序编码分组
        Map<String, List<MicroCompleteReport>> groupCollect = microCompleteReports.stream().collect(Collectors.groupingBy(MicroCompleteReport::getProductSeqAndProcessSeq));
        for (Map.Entry<String, List<MicroCompleteReport>> entry : groupCollect.entrySet()) {
            List<String> keys = Arrays.asList(entry.getKey().split(CHAR_SPILT));
            String productSeq = keys.get(0);
            String processSeq = keys.get(1);
            BigDecimal completeNum = entry.getValue().stream().map(MicroCompleteReport::getCompleteNum).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 查询工序库存信息
            MicroProcessStorageDto queryStorageParam = new MicroProcessStorageDto();
            queryStorageParam.setProductSeq(productSeq);
            queryStorageParam.setProcessSeq(processSeq);
            MicroProcessStorage microProcessStorage = microProcessStorageService.selectMicroProcessStorageByInfo(queryStorageParam);

            // 4.扣减尾序库存良品数量
            MicroProcessStorage updateStorageParam = new MicroProcessStorage();
            updateStorageParam.setProductSeq(productSeq);
            updateStorageParam.setProcessSeq(processSeq);
            updateStorageParam.setLastUpdBy(userId);
            updateStorageParam.setLastUpdDate(nowDate);
            updateStorageParam.setPassNum(microProcessStorage.getPassNum().subtract(completeNum));
            microProcessStorageMapper.updateProcessStorageByProductAndProcess(updateStorageParam);

            // 5.新增尾序库存变动记录
            MicroProcessStorageHistory insertStorageParam = new MicroProcessStorageHistory();
            insertStorageParam.setProductSeq(microProcessStorage.getProductSeq());
            insertStorageParam.setProductCode(microProcessStorage.getProductCode());
            insertStorageParam.setProductName(microProcessStorage.getProductName());
            insertStorageParam.setProcessSeq(microProcessStorage.getProcessSeq());
            insertStorageParam.setProcessCode(microProcessStorage.getProcessCode());
            insertStorageParam.setProcessName(microProcessStorage.getProcessName());
            insertStorageParam.setOperateNode(CommonConstants.STORAGE_PROCESS_COMPLETE_INBOUND);
            insertStorageParam.setPassFromNum(microProcessStorage.getPassNum());
            insertStorageParam.setPassToNum(updateStorageParam.getPassNum());
            insertStorageParam.setNgFromNum(microProcessStorage.getNgNum());
            insertStorageParam.setNgToNum(microProcessStorage.getNgNum());
            insertStorageParam.setCreatedBy(userId);
            insertStorageParam.setCreatedDate(nowDate);
            insertStorageParam.setRemark(reason);
            microProcessStorageHistoryMapper.insertMicroProcessStorageHistory(insertStorageParam);
        }

        // 6.新增或更新成品库存数量（并且 新增成品库存变更记录）
        // 按照产品编码分组汇总库存变动数量
        Map<String, List<MicroCompleteReport>> collectGroup = microCompleteReports.stream().collect(Collectors.groupingBy(MicroCompleteReport::getProductSeq));
        for (Map.Entry<String, List<MicroCompleteReport>> entry : collectGroup.entrySet()) {
            String productSeq = entry.getKey();
            List<MicroCompleteReport> completeReportGroupList = entry.getValue();
            BigDecimal changeNum = completeReportGroupList.stream().map(MicroCompleteReport::getCompleteNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            String productName = completeReportGroupList.get(0).getProductName();

            MicroFinishProductStorageAdjustParam adjustParam = new MicroFinishProductStorageAdjustParam();
            adjustParam.setProductSeq(productSeq);
            adjustParam.setProductName(productName);
            adjustParam.setChangeNum(changeNum);
            adjustParam.setChangeType(FinishStorageChangeTypeEnum.FINISH_INBOUND.getCode());
            adjustParam.setChangeReason(reason);
            microFinishedProductStorageService.updateMicroFinishedProductStorage(adjustParam);
        }
    }

    /**
     * 新增完工单-工易派
     *
     * @param microCompleteReports
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addPlanCompleteReport(List<MicroCompleteReport> microCompleteReports) {
        Date nowDate = DateUtils.getNowDate();
        String userId = SecurityUtils.getUserId().toString();

        // 1.新增完工单
        String reportNo = microCompleteReportService.insertMicroCompleteReportBatch(microCompleteReports);
        String reason = "完工报告" + reportNo + "入库";

        // 2.生产工单标记为已完工
        List<Long> workOrderIds = microCompleteReports.stream().map(MicroCompleteReport::getWorkSubmitId).collect(Collectors.toList());
        MicroManufactureWorkOrder updateParam = new MicroManufactureWorkOrder();
        updateParam.setIsComplete(IsCompleteEnum.YES.getCode());
        updateParam.setLastUpdBy(userId);
        updateParam.setLastUpdDate(nowDate);
        microManufactureWorkOrderMapper.updateCompleteFlag(workOrderIds, updateParam);

        // 3.新增或更新成品库存数量（并且 新增成品库存变更记录）
        // 按照产品编码分组汇总库存变动数量
        Map<String, List<MicroCompleteReport>> collectGroup = microCompleteReports.stream().collect(Collectors.groupingBy(MicroCompleteReport::getProductSeq));
        for (Map.Entry<String, List<MicroCompleteReport>> entry : collectGroup.entrySet()) {
            String productSeq = entry.getKey();
            List<MicroCompleteReport> completeReportGroupList = entry.getValue();
            BigDecimal changeNum = completeReportGroupList.stream().map(MicroCompleteReport::getCompleteNum).reduce(BigDecimal.ZERO, BigDecimal::add);
            String productName = completeReportGroupList.get(0).getProductName();

            MicroFinishProductStorageAdjustParam adjustParam = new MicroFinishProductStorageAdjustParam();
            adjustParam.setProductSeq(productSeq);
            adjustParam.setProductName(productName);
            adjustParam.setChangeNum(changeNum);
            adjustParam.setChangeType(FinishStorageChangeTypeEnum.FINISH_INBOUND.getCode());
            adjustParam.setChangeReason(reason);
            microFinishedProductStorageService.updateMicroFinishedProductStorage(adjustParam);
        }

    }

    /**
     * 撤销完工单前置校验
     *
     * @param reportNo
     */
    @Override
    public void cancelCheck(String reportNo) {
        Date nowDate = DateUtils.getNowDate();

        // 查询完工报告单详细信息列表
        List<MicroCompleteReport> microCompleteReports = microCompleteReportService.selectMicroCompleteReportByReportNo(reportNo);
        if (CollectionUtils.isEmpty(microCompleteReports)) {
            throw new CustomException("完工报告单号不存在！");
        }
        MicroCompleteReport microCompleteReport = microCompleteReports.get(0);
        Date completeTime = microCompleteReport.getCompleteTime();

        // 1.只能撤销当月的完工报告单
        if (!DateUtil.isSameMonth(nowDate, completeTime)) {
            throw new CustomException("已完成记工计算，无法撤销");
        }

        for (MicroCompleteReport completeReport : microCompleteReports) {
            // 2.不允许撤销已完成计件结算或已撤销过的完工报告
            if (!CompleteReportStateEnum.NORMAL.getCode().equals(completeReport.getState())) {
                log.warn("完工报告处于已撤销或者已完成计件结算的状态，无法撤销！reportNo:{}", reportNo);
                throw new CustomException("完工报告非正常状态，无法撤销");
            }

            // 3.检查产成品库存产品数量是否大于等于撤销数量
            BigDecimal completeNum = completeReport.getCompleteNum();

            MicroFinishedProductStorage productStorage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(completeReport.getProductSeq());
            if (productStorage.getNum().compareTo(completeNum) < 0) {
                throw new CustomException("产成品库存中的产品数量小于撤销数量！");
            }
        }
    }

    /**
     * 撤销完工单前置校验（工易派）
     *
     * @param id
     */
    @Override
    public void cancelCheckPlan(Long id) {
        Date nowDate = DateUtils.getNowDate();

        // 查询完工报告详细信息
        MicroCompleteReport microCompleteReport = microCompleteReportService.selectMicroCompleteReportById(id);
        if (null == microCompleteReport) {
            throw new CustomException("完工报告单不存在！");
        }

        // 1.只能撤销当月的完工报告单
        if (!DateUtil.isSameMonth(nowDate, microCompleteReport.getCompleteTime())) {
            throw new CustomException("已完成记工计算，无法撤销");
        }

        // 2.不允许撤销已完成计件结算或已撤销的完工报告
        if (!CompleteReportStateEnum.NORMAL.getCode().equals(microCompleteReport.getState())) {
            log.warn("完工报告处于已撤销或者已完成计件结算的状态，无法撤销！id:{}", id);
            throw new CustomException("完工报告非正常状态，无法撤销");
        }

        // 3.检查产成品库存产品数量是否大于等于撤销数量
        BigDecimal completeNum = microCompleteReport.getCompleteNum();

        MicroFinishedProductStorage productStorage = microFinishedProductStorageService.selectMicroFinishedProductStorageByProductSeq(microCompleteReport.getProductSeq());
        if (productStorage.getNum().compareTo(completeNum) < 0) {
            throw new CustomException("产成品库存中的产品数量小于撤销数量！");
        }

    }

    /**
     * 撤销完工单
     *
     * @param reportNo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelCompleteReport(String reportNo) { 
        Date nowDate = DateUtils.getNowDate();
        String userId = SecurityUtils.getUserId().toString();
        String reason = "完工报告" + reportNo + "撤销";

        // 1.前置校验处理
        this.cancelCheck(reportNo);

        // 查询完工报告单详细信息列表
        List<MicroCompleteReport> microCompleteReports = microCompleteReportService.selectMicroCompleteReportByReportNo(reportNo);

        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            // 2.报工记录去除已完工标记
            MicroWorkSubmit updateParam = new MicroWorkSubmit();
            updateParam.setId(microCompleteReport.getWorkSubmitId());
            updateParam.setLastUpdBy(userId);
            updateParam.setLastUpdDate(nowDate);
            updateParam.setIsComplete(IsCompleteEnum.NO.getCode());
            microWorkSubmitMapper.updateWorkSubmitCompleteFlag(updateParam);

            // 查询报工记录信息
            MicroWorkSubmit microWorkSubmit = microWorkSubmitMapper.selectMicroWorkSubmitById(microCompleteReport.getWorkSubmitId());
            String productSeq = microWorkSubmit.getProductSeq();
            String processSeq = microWorkSubmit.getOperateProcessSeq();
            microCompleteReport.setProductSeqAndProcessSeq(productSeq + CHAR_SPILT + processSeq);

            // 3.新增报工记录历史变动记录
            MicroWorkSubmitHistory insertWorkSubmitHistory = new MicroWorkSubmitHistory();
            BeanUtils.copyProperties(microWorkSubmit, insertWorkSubmitHistory);
            // 查产品基础信息
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(microWorkSubmit.getProductSeq());
            if (null != microProduct) {
                insertWorkSubmitHistory.setProductCode(microProduct.getProductCode());
                insertWorkSubmitHistory.setProductName(microProduct.getProductName());
            }
            // 查工序基础信息
            MicroProcessCommon preProcessCommonInfo = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(microWorkSubmit.getPreProcessSeq());
            if (null != preProcessCommonInfo) {
                insertWorkSubmitHistory.setPreProcessCode(preProcessCommonInfo.getProcessCode());
                insertWorkSubmitHistory.setPreProcessName(preProcessCommonInfo.getProcessName());
            }
            MicroProcessCommon currentProcessCommonInfo = microProcessCommonMapper.selectMicroProcessCommonByProcessSeq(microWorkSubmit.getOperateProcessSeq());
            if (null != currentProcessCommonInfo) {
                insertWorkSubmitHistory.setOperateProcessCode(currentProcessCommonInfo.getProcessCode());
                insertWorkSubmitHistory.setOperateProcessName(currentProcessCommonInfo.getProcessName());
            }
            insertWorkSubmitHistory.setCreatedBy(userId);
            insertWorkSubmitHistory.setCreatedDate(nowDate);
            insertWorkSubmitHistory.setOperateNode(CommonConstants.SUBMIT_HISTORY_CANCEL_COMPLETE);
            microWorkSubmitHistoryMapper.insertMicroWorkSubmitHistory(insertWorkSubmitHistory);

            // 4.更新完工报告单状态
            MicroCompleteReport updateCompleteParam = new MicroCompleteReport();
            updateCompleteParam.setReportNo(reportNo);
            updateCompleteParam.setState(CompleteReportStateEnum.CANCEL.getCode());
            updateCompleteParam.setLastUpdBy(userId);
            updateCompleteParam.setLastUpdDate(nowDate);
            microCompleteReportService.updateMicroCompleteReport(updateCompleteParam);
        }

        // 按照产品+工序分组
        Map<String, List<MicroCompleteReport>> collectGroup = microCompleteReports.stream().collect(Collectors.groupingBy(MicroCompleteReport::getProductSeqAndProcessSeq));
        for (Map.Entry<String, List<MicroCompleteReport>> entry : collectGroup.entrySet()) {
            List<String> keys = Arrays.asList(entry.getKey().split(CHAR_SPILT));
            String productSeq = keys.get(0);
            String processSeq = keys.get(1);
            BigDecimal completeNum = entry.getValue().stream().map(MicroCompleteReport::getCompleteNum).reduce(BigDecimal.ZERO, BigDecimal::add);

            // 查询工序库存信息
            MicroProcessStorageDto queryStorageParam = new MicroProcessStorageDto();
            queryStorageParam.setProductSeq(productSeq);
            queryStorageParam.setProcessSeq(processSeq);
            MicroProcessStorage microProcessStorage = microProcessStorageService.selectMicroProcessStorageByInfo(queryStorageParam);


            // 5.增加尾序库存良品数量
            MicroProcessStorage updateStorageParam = new MicroProcessStorage();
            updateStorageParam.setProductSeq(productSeq);
            updateStorageParam.setProcessSeq(processSeq);
            updateStorageParam.setLastUpdBy(userId);
            updateStorageParam.setLastUpdDate(nowDate);
            updateStorageParam.setPassNum(microProcessStorage.getPassNum().add(completeNum));
            microProcessStorageMapper.updateProcessStorageByProductAndProcess(updateStorageParam);

            // 6.新增尾序变动记录
            MicroProcessStorageHistory insertStorageParam = new MicroProcessStorageHistory();
            insertStorageParam.setProductSeq(microProcessStorage.getProductSeq());
            insertStorageParam.setProductCode(microProcessStorage.getProductCode());
            insertStorageParam.setProductName(microProcessStorage.getProductName());
            insertStorageParam.setProcessSeq(microProcessStorage.getProcessSeq());
            insertStorageParam.setProcessCode(microProcessStorage.getProcessCode());
            insertStorageParam.setProcessName(microProcessStorage.getProcessName());
            insertStorageParam.setOperateNode(CommonConstants.STORAGE_PROCESS_COMPLETE_OUTBOUND);
            insertStorageParam.setPassFromNum(microProcessStorage.getPassNum());
            insertStorageParam.setPassToNum(updateStorageParam.getPassNum());
            insertStorageParam.setNgFromNum(microProcessStorage.getNgNum());
            insertStorageParam.setNgToNum(microProcessStorage.getNgNum());
            insertStorageParam.setCreatedBy(userId);
            insertStorageParam.setCreatedDate(nowDate);
            insertStorageParam.setRemark(reason);
            microProcessStorageHistoryMapper.insertMicroProcessStorageHistory(insertStorageParam);
        }

        // 7.更新成品库存数量（+新增成品库存变更记录）
        // 按照产品编码进行分组汇总库存变动数量
        Map<String, List<MicroCompleteReport>> collect = microCompleteReports.stream().collect(Collectors.groupingBy(MicroCompleteReport::getProductSeq));
        for (Map.Entry<String, List<MicroCompleteReport>> entry : collect.entrySet()) {
            String productSeq = entry.getKey();
            List<MicroCompleteReport> completeReportGroupList = entry.getValue();
            String productName = completeReportGroupList.get(0).getProductName();
            BigDecimal changeNum = completeReportGroupList.stream().map(MicroCompleteReport::getCompleteNum).reduce(BigDecimal.ZERO, BigDecimal::add);

            MicroFinishProductStorageAdjustParam storageAdjustParam = new MicroFinishProductStorageAdjustParam();
            storageAdjustParam.setProductSeq(productSeq);
            storageAdjustParam.setProductName(productName);
            storageAdjustParam.setChangeNum(changeNum.negate());
            storageAdjustParam.setChangeType(FinishStorageChangeTypeEnum.FINISH_CANCEL.getCode());
            storageAdjustParam.setChangeReason(reason);
            microFinishedProductStorageService.updateMicroFinishedProductStorage(storageAdjustParam);
        }
    }

    /**
     * 撤销完工单（工易派）
     *
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cancelPlanCompleteReport(Long id) {
        Date nowDate = DateUtils.getNowDate();
        String userId = SecurityUtils.getUserId().toString();

        // 1.前置校验处理
        this.cancelCheckPlan(id);

        // 查询完工报告单详细信息
        MicroCompleteReport microCompleteReport = microCompleteReportService.selectMicroCompleteReportById(id);

        // 2.生产工单去除已完工标记
        List<Long> workOrderIds = Arrays.asList(microCompleteReport.getWorkSubmitId());
        MicroManufactureWorkOrder updateParam = new MicroManufactureWorkOrder();
        updateParam.setIsComplete(IsCompleteEnum.NO.getCode());
        updateParam.setLastUpdBy(userId);
        updateParam.setLastUpdDate(nowDate);
        microManufactureWorkOrderMapper.updateCompleteFlag(workOrderIds, updateParam);

        // 3.更新完工报告单状态
        MicroCompleteReport updateCompleteParam = new MicroCompleteReport();
        updateCompleteParam.setId(id);
        updateCompleteParam.setState(CompleteReportStateEnum.CANCEL.getCode());
        updateCompleteParam.setLastUpdBy(userId);
        updateCompleteParam.setLastUpdDate(nowDate);
        microCompleteReportService.updateMicroCompleteReportById(updateCompleteParam);


        // 4.更新成品库存数量（+新增成品库存变更记录）
        MicroManufactureWorkOrder microManufactureWorkOrder = microManufactureWorkOrderMapper.selectNormalMicroManufactureWorkOrderById(microCompleteReport.getWorkSubmitId());
        String reason = "完工报告" + microManufactureWorkOrder.getWorkOrderNo() + "撤销入库";

        MicroFinishProductStorageAdjustParam storageAdjustParam = new MicroFinishProductStorageAdjustParam();
        storageAdjustParam.setProductSeq(microCompleteReport.getProductSeq());
        storageAdjustParam.setProductName(microCompleteReport.getProductName());
        storageAdjustParam.setChangeNum(microCompleteReport.getCompleteNum().negate());
        storageAdjustParam.setChangeType(FinishStorageChangeTypeEnum.FINISH_CANCEL.getCode());
        storageAdjustParam.setChangeReason(reason);
        microFinishedProductStorageService.updateMicroFinishedProductStorage(storageAdjustParam);
    }

    /**
     * 邮件导出完工报告
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Override
    public String exportCompleteReportStatisticsRecord(Date startDate, Date endDate, String receivedBy) {
        log.info("请求参数为:startDate:{}-endDate:{}-receivedBy:{}", startDate, endDate, receivedBy);
        // 校验邮箱格式
        if (!MicroEmailUtils.isValidEmail(receivedBy)) {
            throw new CustomException("邮箱格式错误");
        }
        // 构建导出数据
        MicroCompleteReportStatisticsParam queryParam = new MicroCompleteReportStatisticsParam();
        queryParam.setStartDate(startDate);
        queryParam.setEndDate(endDate);
        List<MicroCompleteReportExportResult> resultList = microCompleteReportService.selectCompleteReportsGpByProductAndReportNo(queryParam);
        resultList = resultList.stream().peek(e -> {
            // 查询完工操作人昵称
            MicroUser microUser = microUserService.selectMicroUserById(e.getCompleteUser());
            if (null != microUser && StringUtils.hasText(microUser.getNickName())) {
                e.setCompleteUserNickName(microUser.getNickName());
            }
        }).collect(Collectors.toList());

        Map<String, Object> placeholderMap = Maps.newHashMap();
        placeholderMap.put(MailConstants.MAIL_REPORT_NAME, MailConstants.COMPLETE_REPORT_EXCEL_NAME);

        // 导出并发送邮件
        return microEmailService.exportAndSendEmail(MailConstants.COMPLETE_REPORT_MAIL_SUBJECT, MailConstants.EXCEL_EXPORT_COMMON_MAIL_TEMPLATE_ID, receivedBy, resultList, MicroCompleteReportExportResult.class, placeholderMap);
    }

    /**
     * 按产品统计完工报告情况
     *
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportStatisticsProductResult> statisticsCompleteNumGroupByProduct(MicroCompleteReportStatisticsParam queryParam) {
        // 1.按产品统计完工报告情况
        List<MicroCompleteReportStatisticsProductResult> resultList = microCompleteReportService.selectMicroCompleteNumGroupByProduct(queryParam);
        if (CollectionUtils.isEmpty(resultList)) {
            return resultList;
        }

        // 2.给产品编码和产品单位赋值
        List<MicroCompleteReportStatisticsProductResult> collectList = resultList.stream().peek(e -> {
            MicroProduct microProduct = microProductMapper.selectMicroProductByProductSeq(e.getProductSeq());
            if (null != microProduct) {
                e.setProductCode(microProduct.getProductCode());
                e.setProductUnit(microProduct.getUnit());
            }
        }).collect(Collectors.toList());
        return MicroPageUtils.listToPage(resultList, collectList);
    }


}
