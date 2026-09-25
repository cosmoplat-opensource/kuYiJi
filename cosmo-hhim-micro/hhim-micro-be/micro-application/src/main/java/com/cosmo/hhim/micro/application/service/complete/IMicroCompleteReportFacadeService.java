/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.complete;

import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportAndSubmitResult;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportDetailInfo;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportPlanWaitParam;
import com.cosmo.hhim.micro.application.dto.complete.MicroWaitCompleteReportIndexResult;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealQueryDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealResultDto;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReport;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportBasicParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportDetailGroupInfo;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupPlanResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideDetailResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsProductResult;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/3/23
 */
public interface IMicroCompleteReportFacadeService {

    /**
     * 查询待处理的完工产品列表（工易派）
     * @param queryParam
     */
    List<MicroManufactureWorkOrder> waitDealList(MicroCompleteReportPlanWaitParam queryParam); 

    /**
     * 查询待处理的完工产品列表
     * @param queryDto
     */
    List<WaitDealResultDto> waitDealList(WaitDealQueryDto queryDto); 

    /**
     * 待确认完工报告-导出并发送邮件
     * @param receivedBy
     * @return
     */
    String exportCompleteReportWaitDealRecord(String receivedBy, WaitDealQueryDto queryDto); 

    /**
     * 统计待处理的完工指标
     * @return
     */
    MicroWaitCompleteReportIndexResult statisticsWaitCompleteReportIndex(WaitDealQueryDto queryDto); 

    /**
     * 查询完工产品列表(KU易记)-完工报告维度
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportGroupResult> finishDealList(MicroCompleteReportParam queryParam);


    /**
     * 查询完工产品列表(KU易记)-产品维度
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportProductGroupResult> finishDealProductSideList(MicroCompleteReportProductSideParam queryParam);

    /**
     * 获取完工报告单详细信息列表（KU易记）--产品维度
     * @param param
     * @return
     */
    List<MicroCompleteReportProductSideDetailResult> getDetailInfoForProductSide(MicroCompleteReportBasicParam param);

    /**
     * 查询完工产品列表(工易派)
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportGroupPlanResult> finishDealPlanList(MicroCompleteReportParam queryParam);

    /**
     * 查询完工报告+报工记录中的相关信息
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportAndSubmitResult> selectMicroCompleteReportAndSubmitInfos(MicroCompleteReportParam queryParam);


    /**
     * 已完成完工报告-导出并发送邮件
     * @param receivedBy
     * @return
     */
    String exportCompleteReportFinishDealRecord(Date startDate, Date endDate, String receivedBy); 

    /**
     * 查询完工产品详细信息-按照产品分组
     * @param reportNo
     * @return
     */
    List<MicroCompleteReportDetailGroupInfo> getDetailGroupInfo(String reportNo);

    /**
     * 查询完工产品详细信息
     * @param reportNo
     * @return
     */
    List<MicroCompleteReportDetailInfo> getDetailInfo(String reportNo);

    /**
     * 新增完工单-KU易记
     */
    void addCompleteReport(List<MicroCompleteReport> microCompleteReports); 

    /**
     * 新增完工单-工易派
     * @param microCompleteReports
     */
    void addPlanCompleteReport(List<MicroCompleteReport> microCompleteReports);

    /**
     * 撤销完工单前置校验
     * @param reportNo
     */
    void cancelCheck(String reportNo);

    /**
     * 撤销完工单号前置校验（工易派）
     * @param id
     */
    void cancelCheckPlan(Long id);

    /**
     * 撤销完工单
     * @param reportNo
     */
    void cancelCompleteReport(String reportNo);

    /**
     * 撤销完工单（工易派）
     * @param id
     */
    void cancelPlanCompleteReport(Long id);

    /**
     * 邮件导出完工报告
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportCompleteReportStatisticsRecord(Date startDate, Date endDate, String receivedBy);

    /**
     * 按产品统计完工报告情况
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportStatisticsProductResult> statisticsCompleteNumGroupByProduct(MicroCompleteReportStatisticsParam queryParam);


}
