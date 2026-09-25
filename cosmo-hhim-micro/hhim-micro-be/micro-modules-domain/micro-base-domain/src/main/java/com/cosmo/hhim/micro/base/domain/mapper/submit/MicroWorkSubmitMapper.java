/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.mapper.submit;

import com.cosmo.hhim.micro.base.domain.entity.analysis.*;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.common.FirstSubmitOrCheckInfoResult;
import com.cosmo.hhim.micro.base.domain.entity.ng.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.*;
import com.cosmo.hhim.micro.infrastructure.entity.MicroExistEntity;
import com.cosmo.hhim.micro.infrastructure.entity.MicroSelectEntity;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 报工记录Mapper接口
 *
 * @date 2022-10-12
 */
public interface MicroWorkSubmitMapper {
    /**
     * 查询报工记录
     *
     * @param id 报工记录ID
     * @return 报工记录
     */
    public MicroWorkSubmit selectMicroWorkSubmitById(Long id);

    /**
     * 根据id获取报工记录信息
     *
     * @param ids
     * @return
     */
    List<MicroWorkSubmit> selectMicroWorkSubmitByIds(@Param("ids") Long[] ids);

    /**
     * 查询报工记录列表
     *
     * @param microWorkSubmit 报工记录
     * @return 报工记录集合
     */
    public List<MicroWorkSubmit> selectMicroWorkSubmitList(MicroWorkSubmit microWorkSubmit);

    /**
     * 新增报工记录
     *
     * @param microWorkSubmit 报工记录
     * @return 结果
     */
    public int insertMicroWorkSubmit(MicroWorkSubmit microWorkSubmit);

    /**
     * 批量新增报工记录
     *
     * @param microWorkSubmitList
     * @return
     */
    int insertMicroWorkSubmitBatch(@Param("list") List<MicroWorkSubmit> microWorkSubmitList);

    /**
     * 修改报工记录
     *
     * @param microWorkSubmit 报工记录
     * @return 结果
     */
    public int updateMicroWorkSubmit(MicroWorkSubmit microWorkSubmit);

    /**
     * 修改报工记录完工标识
     * @param microWorkSubmit
     * @return
     */
    int updateWorkSubmitCompleteFlag(MicroWorkSubmit microWorkSubmit);

    /**
     * 修改报工记录的是否送检标示
     *
     * @param microWorkSubmit
     * @return
     */
    int updateSubmitRecordCheckStatus(MicroWorkSubmit microWorkSubmit);

    /**
     * 批量修改报工记录的是否送检标示
     *
     * @param microWorkSubmits
     * @return
     */
    int updateSubmitRecordCheckStatusBatch(@Param("microWorkSubmits") List<MicroWorkSubmit> microWorkSubmits);

    /**
     * 删除报工记录
     *
     * @param id 报工记录ID
     * @return 结果
     */
    public int deleteMicroWorkSubmitById(Long id);

    /**
     * 清理数据
     *
     * @return
     */
    int deleteAllMicroWorkSubmit();

    /**
     * 批量删除报工记录
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteMicroWorkSubmitByIds(Long[] ids);

    List<MicroExistEntity> selectSubmitRecordByProduct(Long[] ids); 

    /**
     * 查询报工记录列表
     *
     * @param microWorkSubmit 报工记录
     * @return 报工记录集合
     */
    List<MicroWorkSubmitDto> selectMicroWorkSubmitExList(MicroWorkSubmit microWorkSubmit);

    /**
     * 查询报工记录明细
     *
     * @param id
     * @return
     */
    MicroWorkSubmit selectMicroWorkSubmitExById(Long id);

    /**
     * 查询多条报工记录明细
     *
     * @param ids
     * @param submitNickName
     * @param submitStatus
     * @return
     */
    List<MicroWorkSubmitDto> selectMicroWorkSubmitExByIds(@Param("ids") Long[] ids, @Param("submitNickName") String submitNickName, @Param("submitStatus") Long submitStatus);

    /**
     * 获取每天总的报工数量
     *
     * @param dates
     * @param userId
     * @return
     */
    List<MicroWorkSubmitProductCount> getProductCountForSubmitByDay(@Param("dates") List<String> dates, @Param("userId") String userId, @Param("submitType") Long submitType); 

    /**
     * 获取本月中每天的报工数量
     *
     * @param startDayOfMonth
     * @param endDayOfMonth
     * @param userId
     * @return
     */
    List<MicroWorkSubmitProductCount> getProductCountForSubmitByMonth(@Param("startDayOfMonth") LocalDate startDayOfMonth, @Param("endDayOfMonth") LocalDate endDayOfMonth, @Param("userId") String userId);

    /**
     * 按照产品纬度 - 审核首页index
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectSubmitRecordGroupByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 新的产品维度审核展示列表
     *
     * @param checkByProductParam
     * @return
     */
    List<SubmitRecordGroupByProduct> selectSubmitRecordGroupByProductList(CheckByProductParam checkByProductParam);

    /**
     * 获取审产首页 - 产品维度待办数量
     *
     * @param microWorkSubmitDto
     * @return
     */
    int obtainedTodoNumsByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 按照员工维度 - 审核首页index
     *
     * @param submitRecordQueryParam
     * @return
     */
    List<MicroWorkSubmit> selectSubmitRecordByUser(SubmitRecordQueryParam submitRecordQueryParam);

    /**
     * 获取审产首页 - 员工维度待办数量
     *
     * @param microWorkSubmitDto
     * @return
     */
    int obtainedTodoNumsByUser(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 批量更新返修复核之后的报工记录数量
     *
     * @param microWorkSubmits
     * @return
     */
    int updateSubmitRecordRepairNumBatch(@Param("microWorkSubmits") List<MicroWorkSubmit> microWorkSubmits);

    /**
     * 批量更新
     *
     * @param microWorkSubmits
     * @return
     */
    int updateMicroWorkSubmitBatch(@Param("microWorkSubmits") List<MicroWorkSubmit> microWorkSubmits);

    /**
     * 根据报工记录获取产品的最后一道工序或者首序
     *
     * @param productSeq
     * @param isLastProcess
     * @param isFirstProcess
     * @param submitStatus
     * @return
     */
    List<MicroWorkSubmitDto> getProcessByProductSeq(@Param("productSeq") String productSeq, @Param("isLastProcess") String isLastProcess, @Param("isFirstProcess") String isFirstProcess, @Param("submitStatus") Long submitStatus);

    /**
     * 审核完的报工产品数据 (产品 + 工序)
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectCompletedProductListByProductAddProcess(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 审核完的报工产品数据 （产品维度）
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectCompletedProductListByProduct(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 审核完的报工产品数据（工序维度）
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectCompletedProductListByProcess(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 审核完的报工产品数据 (员工维度)
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmitDto> selectCompletedProductListByEmployee(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 查询非指定人的首次记工信息
     *
     * @return
     */
    FirstSubmitOrCheckInfoResult selectFirstSubmitInfo(String userId); 


    /**
     * 查询非指定人的首次审产信息
     *
     * @return
     */
    FirstSubmitOrCheckInfoResult selectFirstCheckInfo(String userId); 

    /**
     * 获取报工人的多个报工时间（按照顺序）
     *
     * @param startDate
     * @param endDate
     * @param submitUser
     * @return
     */
    List<Submitter> findSubmitterWithSubmitDay(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("submitUser") String submitUser);

    /**
     * 获取员工 + 产品 + 工序维度的平均报工数量（大于10次，审核后）
     *
     * @param submitUser
     * @param productSeq
     * @param processSeq
     * @return
     */
    BigDecimal getAvgSubmitNumForUserAndProductAndProcess(@Param("submitUser") String submitUser, @Param("productSeq") String productSeq, @Param("processSeq") String processSeq);

    /**
     * 查询产品 + 工序对应的最后一道工序字段
     *
     * @param productSeq
     * @param processSeq
     * @param submitStatus
     * @return
     */
    FirstOrLastProcess selectIsFirstAndLastProcess(@Param("productSeq") String productSeq, @Param("processSeq") String processSeq, @Param("submitStatus") Long submitStatus);

    /**
     * 获取每个人到天的日产能
     *
     * @param currentDate
     * @return
     */
    List<ProductiveCapacityByDay> obtainedProductiveCapacityByDayForEmployee(LocalDate currentDate);

    /**
     * 工厂维度的日均产能
     *
     * @return
     */
    BigDecimal obtainedAverageDayProductiveCapacityForFactory();

    /**
     * 工厂维度下的人均日产能
     *
     * @return
     */
    BigDecimal obtainedAverageDayProductiveCapacityForEmployeeFromFactory();

    /**
     * 获取每个人的日均产能
     *
     * @return
     */
    @MapKey("submitUser")
    Map<String, Map<String, Object>> obtainedAverageDayProductiveCapacityForEmployee();

    /**
     * 查询产品 + 工序的总数量
     *
     * @param productSeq
     * @param processSeq
     * @param submitStatus
     * @return
     */
    CountForProductAndProcess obtainedCountForProductAndProcess(@Param("productSeq") String productSeq, @Param("processSeq") String processSeq, @Param("submitStatus") Long submitStatus);

    /**
     * 根据报工记录获取产品+ 工序的报工数量 (所有)
     *
     * @param submitStatus
     * @return
     */
    @MapKey("seqKey")
    Map<String, CountForProductAndProcess> obtainedCountForAllProductAndProcess(@Param("submitStatus") Long submitStatus);

    /**
     * 根据报工人和报工时间范围查询报工记录
     *
     * @param submitUser
     * @param submitStartDay
     * @param submitEndDay
     * @param submitType
     * @return
     */
    List<MicroWorkSubmit> selectWorkSubmitInfosBySubmitUserAndDay(@Param("submitUser") String submitUser,
                                                                  @Param("submitStartDay") Date submitStartDay,
                                                                  @Param("submitEndDay") Date submitEndDay,
                                                                  @Param("submitType") Long submitType);

    /**
     * 根据报工人和报工时间范围查询报工记录涉及的工单信息
     *
     * @param submitUser
     * @param submitStartDay
     * @param submitEndDay
     * @param submitType
     * @return
     */
    List<String> selectAllWorkOrderFromSubmitRecordByUserAndDay(@Param("submitUser") String submitUser,
                                                                @Param("submitStartDay") Date submitStartDay,
                                                                @Param("submitEndDay") Date submitEndDay,
                                                                @Param("submitType") Long submitType);

    /**
     * 查询报工记录 - 待扩展字段
     *
     * @param submitUser
     * @param submitStartDay
     * @param submitEndDay
     * @param submitType
     * @param submitStatus
     * @return
     */
    List<MicroWorkSubmit> selectWorkSubmitExInfosBySubmitUserAndDay(@Param("submitUser") String submitUser,
                                                                    @Param("submitStartDay") Date submitStartDay,
                                                                    @Param("submitEndDay") Date submitEndDay,
                                                                    @Param("submitType") Long submitType,
                                                                    @Param("submitStatus") Long submitStatus);

    /**
     * 获取总的报工汇总信息
     *
     * @param submitUser
     * @param submitStartDay
     * @param submitEndDay
     * @param submitType
     * @param submitStatus
     * @return
     */
    SubmitTotalInfoInDifferentStatusByUser getSubmitSummaryInfoByUser(@Param("submitUser") String submitUser,
                                                                      @Param("submitStartDay") Date submitStartDay,
                                                                      @Param("submitEndDay") Date submitEndDay,
                                                                      @Param("submitType") Long submitType,
                                                                      @Param("submitStatus") Long submitStatus);

    /**
     * 查询报工记录信息
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<MicroWorkSubmit> selectWorkSubmitInfos(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 按照用户分组统计记工记录
     *
     * @param microWorkSubmitDto
     * @return
     */
    List<Map<String, Object>> selectWorkSubmitSumGroupByUser(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 获取用户到天的报工数
     *
     * @param userId
     * @return
     */
    List<ProductiveCapacityByDay> selectUserSubmitNumGroupBySubmitDay(@Param("userId") long userId);

    /**
     * 根据产品seq获取工序列表(报工通过审核的数据)
     *
     * @param productSeq
     * @param key
     * @return
     */
    List<MicroSelectEntity> selectProcessByProduct(@Param("productSeq") String productSeq, @Param("key") String key);

    /**
     * 根据用户id、产品序列码、工序序列码和报工时间获取总报工数量
     *
     * @param microWorkSubmit
     * @return
     */
    TotalSubmitNumByUser obtainedTotalSubmitNum(MicroWorkSubmit microWorkSubmit);

    /**
     * 按照产品、工序、报工人和报工时间汇总的信息
     *
     * @return
     */
    @MapKey("conditionKey")
    Map<String, TotalSubmitNumByCondition> obtainedTotalSubmitNumByCondition();

    Set<String> selectAllProcess(); 

    /**
     * 工易派 - 获取工单维度的列表展示
     *
     * @param checkParamByWorkOrder
     * @return
     */
    List<SubmitRecordInfoByWorkOrder> selectSubmitRecordInfoByWorkOrderList(CheckParamByWorkOrder checkParamByWorkOrder);

    /**
     * 工易派 - 某个工单下详细的报工记录信息
     *
     * @param workOrderNo
     * @return
     */
    List<DetailSubmitRecordInfoByWorkOrder> selectDetailSubmitRecordInfoByWorkOrderList(@Param("workOrderNo") String workOrderNo, @Param("submitStatus") String submitStatus); 

    /**
     * 工易派 - 获取某个工单某个工序下面的到人的报工记录信息
     *
     * @param userSubmitRecordQueryParam
     * @return
     */
    List<UserDetailSubmitRecordInfoByWorkOder> selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam);
    /**
     * 查询已审核尾序报工信息列表
     * @param queryDto
     * @return
     */
    List<WaitDealResultDto> selectLastProcessWorkSubmitInfos(WaitDealQueryDto queryDto);

    /**
     * 获取待审核和驳回的记录条数
     *
     * @param microWorkSubmit
     * @return
     */
    @MapKey("statusKey")
    Map<String, Integer> obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit);

    /**
     * 获取不良品清单 - 产品（已审核）
     *
     * @param productNameOrCode
     * @param submitType
     * @return
     */
    List<NgProductByProductFromAlreadyCheck> selectNgProductListFromAlreadyCheck(@Param("productNameOrCode") String productNameOrCode,
                                                                                 @Param("submitType") Long submitType);

    /**
     * 获取不良品清单的工序维度数量信息 - 产品（已审核）
     *
     * @param productSeqList
     * @param submitType
     * @return
     */
    List<NgProductAndProcessByProductFromAlreadyCheck> selectNgProductAndProcessListFromAlreadyCheck(@Param("productSeqList") String productSeqList,
                                                                                                     @Param("submitType") Long submitType);

    /**
     * 获取不良品清单到 产品 + 工序 + 人 维度
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    List<NgProductAndProcessAndUserByProduct> selectNgProductAndProcessAndUserByProductFromAlreadyCheck(@Param("productSeq") String productSeq,
                                                                                                        @Param("processSeq") String processSeq);

    /**
     * 获取不良品清单 - 员工维度
     *
     * @param submitNickName
     * @param submitType
     * @return
     */
    List<NgProductByUserFromAlreadyCheck> selectNgProductByUserFromAlreadyCheck(@Param("submitNickName") String submitNickName,
                                                                                @Param("submitType") Long submitType);

    /**
     * 获取不良品清单产品 + 工序数量 - 员工维度
     *
     * @param submitUserList
     * @return
     */
    List<NgProductAndProcessByUserFromAlreadyCheck> selectNgProductAndProcessByUserFromAlreadyCheck(@Param("list") List<Long> submitUserList, 
                                                                                                    @Param("submitType") Long submitType);

    /**
     * 不良品模块中涉及的报工记录信息查询
     *
     * @param ngQueryParam
     * @return
     */
    List<MicroWorkSubmit> selectMicroWorkSubmitListByNgManage(NgQueryParam ngQueryParam);

    /**
     * 工作台不良品模块外部展示数量
     *
     * @param submitType
     * @return
     */
    NgNumFromProductAndUser obtainedNgProductTotalNumAndUser(Long submitType);

    /**
     * 获取到产品到工序到人的不良品清单信息
     *
     * @return
     */
    List<NgInfoByProductAndProcessAndUser> selectNgProductNumByProductAndProcessAndUser();

    @MapKey("productAndProcess")
    Map<String, MicroLastPreProcessEntity> findLastedPreProcess(@Param("list") List<MicroLastPreProcessEntity> rawNonStandardList); 

    @MapKey("productAndProcess")
    Map<String, MicroLastPreProcessEntity> findFirstOrLastProcess(@Param("list") List<MicroLastPreProcessEntity> rawNonStandardList); 

    List<MicroWorkSubmit> selectMicroWorkSubmitByProduct(@Param("productList") List<String> productList); 

    List<String> selectAllProductBySubmit(MicroWorkSubmit submit); 

    /**
     * 根据产品ids查询关联的产品数据
     *
     * @param ids
     * @return
     */
    List<MicroSelectEntity> selectReProductByProductIds(Long[] ids);

    List<MicroSelectEntity> selectReProcessByProcessIds(Long[] ids); 

    /**
     * 工易派 - 产品维度信息汇总分页查询
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByProduct> selectProductiveSubmitSummaryAnalysisByProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 产品维度生产报工分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<DifferentStatusSubmitAnalysisByProduct> selectProductiveSubmitAnalysisByProductInDifferentStatus(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 基于产品的工序纬度生产报工数据分析
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByProcessBaseProduct> selectProductiveSubmitAnalysisByProcessBaseProduct(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 工易派 - 员工维度生产报工数据
     * 
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByUser> selectProductiveSubmitAnalysisByUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);

    /**
     * 某个员工下面 产品+工序的报工数据
     *
     * @param productionQualityAnalysisParam
     * @return
     */
    List<ProductiveSubmitAnalysisByProductAndProcessBaseUser> selectProductiveSubmitAnalysisByProductAndProcessBaseUser(ProductionQualityAnalysisParam productionQualityAnalysisParam);
}
