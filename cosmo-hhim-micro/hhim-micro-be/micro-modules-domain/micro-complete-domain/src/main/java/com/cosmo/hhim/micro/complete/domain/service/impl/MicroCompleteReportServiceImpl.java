/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.service.impl;

import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCacheCustomer;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.complete.domain.entity.*;
import com.cosmo.hhim.micro.complete.domain.mapper.MicroCompleteReportMapper;
import com.cosmo.hhim.micro.complete.domain.service.IMicroCompleteReportService;
import com.cosmo.hhim.micro.infrastructure.enums.ActiveFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.CompleteReportStateEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 完工报告单Service业务层处理
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Slf4j
@Service
public class MicroCompleteReportServiceImpl implements IMicroCompleteReportService {
    @Autowired
    private MicroCompleteReportMapper microCompleteReportMapper;
    @Autowired
    private RedisCacheCustomer redisCacheCustomer;

    /**
     * 根据完工单号查询完工报告单详情列表
     *
     * @param reportNo 完工报告单单号
     * @return 完工报告单
     */
    @Override
    public List<MicroCompleteReport> selectMicroCompleteReportByReportNo(String reportNo) {
        return microCompleteReportMapper.selectMicroCompleteReportByReportNo(reportNo);
    }

    /**
     * 根据完工报告单Id查询完工报告详细信息
     * @param id
     * @return
     */
    @Override
    public MicroCompleteReport selectMicroCompleteReportById(Long id) {
        return microCompleteReportMapper.selectMicroCompleteReportById(id);
    }

    /**
     * 根据完工单号查询完工报告单详情列表（按照产品分组）
     *
     * @param reportNo
     * @return
     */
    @Override
    public List<MicroCompleteReportDetailGroupInfo> selectGroupMicroCompleteReportByReportNo(String reportNo) {
        return microCompleteReportMapper.selectGroupMicroCompleteReportByReportNo(reportNo);
    }

    /**
     * 查询完工报告单列表-完工报告维度
     *
     * @param param 完工报告单
     * @return 完工报告单
     */
    @Override
    public List<MicroCompleteReportGroupResult> selectMicroCompleteReportList(MicroCompleteReportParam param) {
        return microCompleteReportMapper.selectMicroCompleteReportList(param);
    }

    /**
     * 查询完工报告单列表-产品维度
     *
     * @param queryParam 完工报告单
     * @return 完工报告单集合
     */
    @Override
    public List<MicroCompleteReportProductGroupResult> selectMicroCompleteReportProductSideList(MicroCompleteReportProductSideParam queryParam) {
        return microCompleteReportMapper.selectMicroCompleteReportListGroupByProduct(queryParam);
    }

    /**
     * 查询工单完工报告单列表（工易派）
     * @param queryParam
     * @return
     */
    @Override
    public List<MicroCompleteReportGroupPlanResult> selectMicroCompleteReportPlanList(MicroCompleteReportParam queryParam) {
        return microCompleteReportMapper.selectMicroCompleteReportForWorkOrderList(queryParam);
    }


    /**
     * 查询完工报告单常规列表
     *
     * @param queryParam 完工报告单
     * @return 完工报告单集合
     */
    @Override
    public List<MicroCompleteReport> selectMicroCompleteReportNormalList(MicroCompleteReportBasicParam queryParam) {
        return microCompleteReportMapper.selectMicroCompleteReportNormalList(queryParam);
    }


    /**
     * 新增完工报告单
     *
     * @param microCompleteReports 完工报告单
     * @return 结果
     */
    @Override
    public String insertMicroCompleteReportBatch(List<MicroCompleteReport> microCompleteReports) {
        Date nowDate = DateUtils.getNowDate();

        // 1.生成完工单号
        String reportNo = genReportNo("MO");

        // 2.新增完工单
        for (MicroCompleteReport microCompleteReport : microCompleteReports) {
            microCompleteReport.setReportNo(reportNo);
            microCompleteReport.setState(CompleteReportStateEnum.NORMAL.getCode());
            microCompleteReport.setCompleteTime(nowDate);
            microCompleteReport.setCompleteUser(SecurityUtils.getUserId());
            microCompleteReport.setSourceChannel(SecurityUtils.getApplicationSign());
            microCompleteReport.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
            microCompleteReport.setCreatedBy(SecurityUtils.getUserId().toString());
            microCompleteReport.setCreatedDate(nowDate);
            microCompleteReportMapper.insertMicroCompleteReport(microCompleteReport);
        }
        return reportNo;
    }

    /**
     * 生成完工单号
     *
     * @param prefixStr
     * @return
     */
    public String genReportNo(String prefixStr) {
        String codeByDay = redisCacheCustomer.incrCodeByDay(prefixStr, 1, 5);
        return codeByDay.substring(0, prefixStr.length()) + codeByDay.substring(codeByDay.length() - 11);
    }

    /**
     * 修改完工报告单
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    @Override
    public int updateMicroCompleteReport(MicroCompleteReport microCompleteReport) {
        return microCompleteReportMapper.updateMicroCompleteReport(microCompleteReport);
    }

    /**
     * 修改完工报告单-根据完工单ID
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    @Override
    public int updateMicroCompleteReportById(MicroCompleteReport microCompleteReport) {
        return microCompleteReportMapper.updateMicroCompleteReportById(microCompleteReport);
    }

    /**
     * 按天统计完工情况
     *
     * @param param
     * @return
     */
    @Override
    public List<MicroCompleteReportStatisticsDayResult> selectMicroCompleteNumGroupByDay(MicroCompleteReportStatisticsParam param) {
        param.setSourceChannel(SecurityUtils.getApplicationSign());
        return microCompleteReportMapper.selectCompleteTotalNumGroupByDay(param);
    }

    /**
     * 根据产品+完工单号分组统计完工数量
     *
     * @param param
     * @return
     */
    @Override
    public List<MicroCompleteReportExportResult> selectCompleteReportsGpByProductAndReportNo(MicroCompleteReportStatisticsParam param) {
        param.setSourceChannel(SecurityUtils.getApplicationSign());
        return microCompleteReportMapper.selectCompleteReportsGpByProductAndReportNo(param);
    }

    /**
     * 按产品统计完工情况
     *
     * @param param
     * @return
     *
     */
    @Override
    public List<MicroCompleteReportStatisticsProductResult> selectMicroCompleteNumGroupByProduct(MicroCompleteReportStatisticsParam param) {
        param.setSourceChannel(SecurityUtils.getApplicationSign());
        return microCompleteReportMapper.selectCompleteTotalNumGroupByProduct(param);
    }
}
