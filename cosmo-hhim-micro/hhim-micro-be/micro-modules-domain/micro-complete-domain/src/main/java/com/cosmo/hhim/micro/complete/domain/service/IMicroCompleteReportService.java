/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.service;

import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReport;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportBasicParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportDetailGroupInfo;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportExportResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupPlanResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsDayResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsProductResult;

import java.util.List;

/**
 * 完工报告单Service接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface IMicroCompleteReportService {
    /**
     * 根据完工单号查询完工报告单详情列表
     *
     * @param reportNo 完工报告单单号
     * @return 完工报告单
     */
    List<MicroCompleteReport> selectMicroCompleteReportByReportNo(String reportNo);

    /**
     * 根据完工报告单Id查询完工报告详细信息
     * @param id
     * @return
     */
    MicroCompleteReport selectMicroCompleteReportById(Long id);

    /**
     * 根据完工单号查询完工报告单详情列表（按照产品分组）
     * @param reportNo
     * @return
     */
    List<MicroCompleteReportDetailGroupInfo> selectGroupMicroCompleteReportByReportNo(String reportNo);

    /**
     * 查询完工报告单列表-完工报告维度
     *
     * @param queryParam 完工报告单
     * @return 完工报告单集合
     */
    List<MicroCompleteReportGroupResult> selectMicroCompleteReportList(MicroCompleteReportParam queryParam);

    /**
     * 查询完工报告单列表-产品维度
     *
     * @param queryParam 完工报告单
     * @return 完工报告单集合
     */
    List<MicroCompleteReportProductGroupResult> selectMicroCompleteReportProductSideList(MicroCompleteReportProductSideParam queryParam);

    /**
     * 查询工单完工报告单列表（工易派）
     * @param queryParam
     * @return
     */
    List<MicroCompleteReportGroupPlanResult> selectMicroCompleteReportPlanList(MicroCompleteReportParam queryParam);

    /**
     * 查询完工报告单常规列表
     *
     * @param queryParam 完工报告单
     * @return 完工报告单集合
     */
    List<MicroCompleteReport> selectMicroCompleteReportNormalList(MicroCompleteReportBasicParam queryParam);

    /**
     * 新增完工报告单
     *
     * @param microCompleteReports 完工报告单
     * @return 结果
     */
    String insertMicroCompleteReportBatch(List<MicroCompleteReport> microCompleteReports);

    /**
     * 修改完工报告单-根据完工单号
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    public int updateMicroCompleteReport(MicroCompleteReport microCompleteReport);

    /**
     * 修改完工报告单-根据完工单ID
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    int updateMicroCompleteReportById(MicroCompleteReport microCompleteReport);

    /**
     * 按天统计完工情况
     * @param param
     * @return
     */
    List<MicroCompleteReportStatisticsDayResult> selectMicroCompleteNumGroupByDay(MicroCompleteReportStatisticsParam param);


    /**
     * 根据产品+完工单号分组统计完工数量
     * @param param
     * @return
     */
    List<MicroCompleteReportExportResult> selectCompleteReportsGpByProductAndReportNo(MicroCompleteReportStatisticsParam param);

    /**
     * 按产品统计完工情况
     * @param param
     * @return
     */
    List<MicroCompleteReportStatisticsProductResult> selectMicroCompleteNumGroupByProduct(MicroCompleteReportStatisticsParam param);

}
