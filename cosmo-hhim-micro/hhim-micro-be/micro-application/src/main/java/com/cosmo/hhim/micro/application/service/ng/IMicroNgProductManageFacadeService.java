/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ng;

import com.cosmo.hhim.micro.application.dto.ng.*;
import com.cosmo.hhim.micro.base.domain.entity.ng.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.ng.domain.entity.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description: 不良品管理Facade层, 汇总各模块
 * @date 2023/3/31 13:53
 */
public interface IMicroNgProductManageFacadeService {

    /**
     * 获取报工记录的质检列表
     *
     * @param submitRecordQueryParam
     * @return
     */
    List<QualityControlOfSubmitRecordInfo> selectQualityControlOfSubmitRecordList(SubmitRecordQueryParam submitRecordQueryParam);

    /**
     * 质检操作
     *
     * @param qualityControlOfSubmitRecordDto
     * @return
     */
    int qualityControlOfRecord(QualityControlOfSubmitRecordDto qualityControlOfSubmitRecordDto);

    /**
     * 批量质检
     *
     * @param  ids  报工记录id
     * @return
     */
    Boolean batchQualityControlOfRecord(Long[] ids);

    /**
     * 根据报工记录id获取详细的质检信息
     * @param id
     * @return
     */
    DetailQualityControlInfo getDetailQcInfo(Long id);

    /**
     * 获取不良品类型 (根据已有质检记录)
     *
     * @param ngTypeQueryParam
     * @return
     */
    List<String> selectNgTypeList(NgTypeQueryParam ngTypeQueryParam);

    /**
     * 获取不良品清单 - 产品（已审核）
     *
     * @param productNameOrCode
     * @return
     */
    List<NgProductByProductFromAlreadyCheck> selectNgProductListFromAlreadyCheck(String productNameOrCode);

    /**
     * 获取到人的不良品详情信息
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    List<NgProductAndProcessAndUserByProduct> selectNgProductAndProcessByUserFromAlreadyCheck(String productSeq, String processSeq);


    /**
     * 获取不良品清单 - 员工（已审核）
     *
     * @param submitNickName
     * @return
     */
    List<NgProductByUserFromAlreadyCheck> selectNgProductListByUserFromAlreadyCheck(String submitNickName);

    /**
     * 返修复核时获取的质检记录信息
     *
     * @param ngQueryParam
     * @return
     */
    DetailQualityControlInfoWhenRepair selectQualityControlInfoWhenRepair(NgQueryParam ngQueryParam);

    /**
     * 返修复核接口
     *
     * @param repairAndThenQualityControlParam
     * @return
     */
    int repairAndThenQualityControl(RepairAndThenQualityControlParam repairAndThenQualityControlParam);

    /**
     * 返修列表
     *
     * @param queryKey (产品编码、名称、工序)
     * @return
     */
    List<RepairRecordInfo> selectMicroRepairRecordListByQueryKey(String queryKey);

    /**
     * 根据repairNo获取返修详情信息
     *
     * @param repairNo
     * @return
     */
    DetailRepairAndThenQualityControlInfo getDetailRepairAndThenQualityControlInfo(String repairNo);

    /**
     * 质检记录历史
     *
     * @param qualityControlHistoryQueryParam
     * @return
     */
    List<QualityControlHistoryInfo> selectQualityControlHistoryInfoList(QualityControlHistoryQueryParam qualityControlHistoryQueryParam);

    /**
     * 不良类型统计分析
     *
     * @param ngTypeStatisticQueryParam
     * @return
     */
    List<NgTypeStatisticInfo> obtainedNgTypeStatisticAnalysis(NgTypeStatisticQueryParam ngTypeStatisticQueryParam);

    /**
     * 工作台不良品模块外部展示数量
     *
     * @return
     */
    NgNumFromProductAndUser obtainedNgProductTotalNumAndUser();

    /**
     * 邮件导出不良品清单
     *
     * @return
     */
    String exportNgProductStatisticsReport(String receivedBy); 
}
