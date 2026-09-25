/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.complete.domain.mapper;


import com.cosmo.hhim.micro.complete.domain.entity.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.util.List;

/**
 * 完工报告单Mapper接口
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
public interface MicroCompleteReportMapper {

    /**
     * 根据完工报告单ID查询完工报告详细信息
     * @param id
     * @return
     */
    MicroCompleteReport selectMicroCompleteReportById(Long id);

    /**
     * 根据完工单号查询完工报告单详情列表
     *
     * @param reportNo 完工报告单单号
     * @return 完工报告单
     */
    List<MicroCompleteReport> selectMicroCompleteReportByReportNo(String reportNo);

    /**
     * 根据完工单号查询完工报告单详情列表（按照产品分组）
     *
     * @param reportNo
     * @return
     */
    List<MicroCompleteReportDetailGroupInfo> selectGroupMicroCompleteReportByReportNo(String reportNo);

    /**
     * 查询完工报告单列表-按照完工单号分组
     *
     * @return 完工报告单集合
     */
    List<MicroCompleteReportGroupResult> selectMicroCompleteReportList(MicroCompleteReportParam param); 


    /**
     * 查询完工报告单列表-按照产品分组
     *
     * @return 完工报告单集合
     */
    List<MicroCompleteReportProductGroupResult> selectMicroCompleteReportListGroupByProduct(MicroCompleteReportProductSideParam param); 

    /**
     * 查询工单的完工报告单列表
     * @param param
     * @return
     */
    List<MicroCompleteReportGroupPlanResult> selectMicroCompleteReportForWorkOrderList(MicroCompleteReportParam param);

    /**
     * 查询完工报告单列表
     * @param param
     * @return
     */
    List<MicroCompleteReport> selectMicroCompleteReportNormalList(MicroCompleteReportBasicParam param);

    /**
     * 新增完工报告单
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    public int insertMicroCompleteReport(MicroCompleteReport microCompleteReport);

    /**
     * 根据完工单号修改完工报告单
     *
     * @param microCompleteReport 完工报告单
     * @return 结果
     */
    public int updateMicroCompleteReport(MicroCompleteReport microCompleteReport);

    /**
     * 根据完工ID修改完工报告单
     * @param microCompleteReport
     * @return
     */
    int updateMicroCompleteReportById(MicroCompleteReport microCompleteReport);

    /**
     * 按天统计完工情况
     * @param param
     * @return
     */
    List<MicroCompleteReportStatisticsDayResult> selectCompleteTotalNumGroupByDay(MicroCompleteReportStatisticsParam param);

    /**
     * 根据产品+完工单号分组统计完工数量
     * @param param
     * @return
     */
    List<MicroCompleteReportExportResult> selectCompleteReportsGpByProductAndReportNo(MicroCompleteReportStatisticsParam param);

    /**
     * 按产品统计完工情况
     *
     * @param param
     * @return
     */
    List<MicroCompleteReportStatisticsProductResult> selectCompleteTotalNumGroupByProduct(MicroCompleteReportStatisticsParam param);

    /**
     * 根据产品ids查询关联的产品数据
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProductByProductIds(Long[] ids);
}
