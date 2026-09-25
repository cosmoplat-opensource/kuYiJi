/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.submit;

import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.common.FirstSubmitOrCheckInfoResult;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroProcessCommon;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByProductFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.ng.NgProductByUserFromAlreadyCheck;
import com.cosmo.hhim.micro.base.domain.entity.submit.*;
import com.cosmo.hhim.micro.base.domain.entity.tech.MicroProcessChainBindEntity;
import com.cosmo.hhim.micro.base.domain.entity.warn.SubmitRecordException;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 报工记录Service接口
 *
 * @date 2022-10-12
 */
public interface IMicroWorkSubmitService {

    /**
     * 新增报工记录
     *
     * @param microWorkSubmit 报工记录
     * @return 结果
     */
    int insertMicroWorkSubmit(MicroWorkSubmit microWorkSubmit);

    /**
     * 更新报工记录
     *
     * @param microWorkSubmit
     * @return
     */
    int updateMicroWorkSubmit(MicroWorkSubmit microWorkSubmit);

    /**
     * 删除报工记录
     *
     * @param id
     * @return
     */
    int deleteMicroWorkSubmitById(Long id);

    /**
     * 查询报工记录
     *
     * @param id 报工记录ID
     * @return 报工记录
     */
    MicroWorkSubmit selectMicroWorkSubmitById(Long id);

    /**
     * 查询报工记录列表
     *
     * @param microWorkSubmit 报工记录
     * @return 报工记录集合
     */
    List<MicroWorkSubmit> selectMicroWorkSubmitList(MicroWorkSubmit microWorkSubmit);

    /**
     * 获取报工明细记录
     *
     * @param id
     * @return
     */
    MicroWorkSubmit selectMicroWorkSubmitExById(Long id);

    /**
     * 获取报工列表 - 扩展信息 (产品、工序名称)
     *
     * @param microWorkSubmit
     * @return
     */
    List<MicroWorkSubmitDto> selectMicroWorkSubmitExList(MicroWorkSubmit microWorkSubmit);

    /**
     * 根据ids获取报工列表 - 扩展信息 (产品、工序名称)
     *
     * @param ids
     * @param userName
     * @param submitStatus
     * @return
     */
    List<MicroWorkSubmitDto> selectMicroWorkSubmitExListByIds(Long[] ids, String userName, Long submitStatus);

    /**
     * 批量删除报工记录
     *
     * @param ids 需要删除的报工记录ID
     * @return 结果
     */
    int deleteMicroWorkSubmitByIds(Long[] ids);

    /**
     * 批量审核报工记录 (报工记录无改变)
     *
     * @param microWorkSubmitList
     * @return
     */
    List<MicroWorkSubmit> checkSubmitRecordBatch(List<MicroWorkSubmit> microWorkSubmitList);

    /**
     * 报工明细中的批量审核接口
     * 可能会改变 是否是最后一道工序
     * 前端只需要传递 id 和 isLastProcess字段
     *
     * @param microWorkSubmitList
     * @return
     */
    List<MicroWorkSubmit> checkSubmitRecordBatchByProduct(List<MicroWorkSubmit> microWorkSubmitList);

    /**
     * 编辑并审核
     *
     * @param microWorkSubmit
     * @return
     */
    MicroWorkSubmit editAndCheck(MicroWorkSubmit microWorkSubmit);

    /**
     * 驳回报工记录
     *
     * @param ids
     * @return
     */
    Boolean rejectSubmitRecord(Long[] ids);

    /**
     * 撤销审核
     *
     * @param id
     * @return 返回值包含未撤销审核前的审核数量
     */
    MicroWorkSubmit undoCheckedRecord(Long id, String type); 

    /**
     * 获取每天总的报工数量
     *
     * @param dates
     * @return
     */
    List<MicroWorkSubmitProductCount> getProductCountForSubmitByDay(List<String> dates);

    /**
     * 获取该月里每天的报工数量
     *
     * @param monthOfYear
     * @return
     */
    Map<String, BigDecimal> getProductCountForSubmitByMonth(String monthOfYear);

    /**
     * 报工记录按照产品纬度进行汇总查询
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectSubmitRecordGroupByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 获取审产首页 - 产品维度待办数量
     *
     * @param microWorkSubmitDto
     * @return
     */
    int obtainedTodoNumsByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 报工记录按照员工维度展示
     *
     * @param submitRecordQueryParam
     * @return
     */
    List<MicroWorkSubmitDto> selectSubmitRecordByUser(SubmitRecordQueryParam submitRecordQueryParam);

    /**
     * 获取审产首页 - 产品维度待办数量
     *
     * @param microWorkSubmitDto
     * @return
     */
    int obtainedTodoNumsByUser(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 检查该产品当前工序是否已经有最后一道工序
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    MicroProcessCommon checkIsLastProcess(String productSeq, String processSeq);

    /**
     * 审核完的报工产品数据
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectCompletedProductList(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 查询首次报工或审产信息
     *
     * @param operateType
     * @return
     */
    FirstSubmitOrCheckInfoResult selectFirstSubmitOrCheckInfo(String operateType);

    /**
     * 根据产品查找工序
     *
     * @param productSeq
     * @param isLastProcess
     * @return
     */
    List<MicroProcessCommon> findProcessListByProductSeq(String productSeq, String isLastProcess);

    /**
     * 判定工序是否已经有首序或者尾序
     *
     * @param productSeq
     * @param processSeq
     * @param processType
     * @return
     */
    CheckFirstOrLastProcess checkIsOrNotHaveFirstOrLastProcess(String productSeq, String processSeq, String processType);

    /**
     * 审核前的提示
     *
     * @param ids
     * @return
     */
    String tipsForCheck(Long[] ids);

    /**
     * 编辑并审核接口调用前的异常判断 -> 用于编辑之后异常判断的方法
     *
     * @param microWorkSubmitDto
     * @return
     */
    SubmitRecordException tipsForEditAndCheck(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 根据产品 + 工序 获取首尾序的标示
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    FirstOrLastProcess getFirstOrLastProcessFlagByRecords(String productSeq, String processSeq);

    /**
     * 判断记工记录是否存在预警标识
     *
     * @param submitRecordQueryParam
     * @return
     */
    Boolean isHaveWarnExceptionForWorkSubmits(SubmitRecordQueryParam submitRecordQueryParam);

    /**
     * 新的产品维度审核展示列表
     *
     * @param checkByProductParam
     * @return
     */
    List<SubmitRecordGroupByProduct> selectSubmitRecordGroupByProductList(CheckByProductParam checkByProductParam);

    /**
     * 展示产品维度审核记录的详细信息
     *
     * @param productSeq
     * @param microWorkSubmitDtoList
     * @param bindEntityList
     * @return
     */
    DetailCheckSubmitRecordByProductRes showDetailSubmitRecordByProduct(String productSeq, List<MicroWorkSubmitDto> microWorkSubmitDtoList, List<MicroProcessChainBindEntity> bindEntityList);

    /**
     * 获取不良品清单 - 产品（已审核）
     *
     * @param productNameOrCode
     * @return
     */
    List<NgProductByProductFromAlreadyCheck> selectNgProductListFromAlreadyCheck(String productNameOrCode);

    /**
     * 获取不良品清单 - 员工（已审核）
     *
     * @param submitNickName
     * @return
     */
    List<NgProductByUserFromAlreadyCheck> selectNgProductListByUserFromAlreadyCheck(String submitNickName);

    /**************************
     * 工易派报工相关实现接口
     * ************************/

    /**
     * 工易派 - 工单维度的审核记录信息列表
     *
     * @param checkParamByWorkOrder
     * @return
     */
    List<SubmitRecordInfoByWorkOrder> selectSubmitRecordInfoByWorkOrder(CheckParamByWorkOrder checkParamByWorkOrder);


    /**
     * 工易派 - 某个工单下详细的报工记录信息
     *
     * @param workOrderNo
     * @param submitStatus
     * @return
     */
    List<DetailSubmitRecordInfoByWorkOrder> selectDetailSubmitRecordInfoByWorkOrderList(String workOrderNo, String submitStatus);

    /**
     * 获取某个工单某个工序下面的到人的报工记录信息
     *
     * @param userSubmitRecordQueryParam
     * @return
     */
    List<UserDetailSubmitRecordInfoByWorkOder> selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam);

    /**
     * 获取不同审核状态下面的报工记录条数
     *
     * @param microWorkSubmit
     * @return
     */
    Map<String, Integer> obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit);

    /**
     * 根据产品+现工序获取最近的前工序和当前现工序是否首尾序
     *
     * @param rawNonStandardList
     * @return
     */
    List<MicroLastPreProcessEntity> findLatestPreProcessAndFirstOrLast(List<MicroLastPreProcessEntity> rawNonStandardList);

    Map<String, List<MicroWorkSubmit>> selectMicroRecordByProductList(List<String> productList); 

    List<String> selectAllProductBySubmit(MicroWorkSubmit submit); 

    /**
     * 获取报工数量 (已审核 + 未审核 良品数)
     *
     * @param microWorkSubmit
     * @return
     */
    TotalSubmitNumDTO obtainedSubmitNum(MicroWorkSubmit microWorkSubmit);

    /**
     * 根据工序ID获取报工记录
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids);

    /**
     * 工易派 - 产品维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitSummaryInfoByProduct> selectProductiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 基于产品的工序维度生产报工汇总分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitSummaryInfoByProcessBaseProduct> selectProductiveSubmitSummaryAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 员工维度生产报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByUser> selectProductiveSubmitAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 员工维度下各产品的报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductSubmitSummaryInfoBaseUser> selectProductiveSubmitSummaryInfoBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 获取用户不同状态报工记录信息
     *
     * @param startDate
     * @param endDate
     * @return
     */
    List<MicroWorkSubmit> getSubmitInfoInDifferentStatusByUser(Date startDate, Date endDate, Long submitStatus); 

    /**
     * 获取用户不同状态报工记录汇总信息
     *
     * @param startDate
     * @param endDate
     * @param submitStatus
     * @return
     */
    SubmitTotalInfoInDifferentStatusByUser getSubmitSummaryInfoByUser(Date startDate, Date endDate, Long submitStatus);
}
