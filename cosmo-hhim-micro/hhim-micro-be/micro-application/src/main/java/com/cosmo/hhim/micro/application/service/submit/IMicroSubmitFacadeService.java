/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.submit;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.dto.submit.BatchSubmitOrQcDto;
import com.cosmo.hhim.micro.application.dto.submit.SubmitInfoInDifferentStatusByUser;
import com.cosmo.hhim.micro.application.dto.submit.SubmitRecordInfoByWorkOrderDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.TotalSubmitNumDTO;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.FirstOrLastProcess;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
public interface IMicroSubmitFacadeService {

    /* *************************************
     *            通用接口
     * *************************************/
    /**
     * 新增报工记录
     *
     * @param microWorkSubmitDto
     * @return
     */
    int createSubmitRecord(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 更新报工记录信息
     *
     * @param microWorkSubmitDto
     * @return
     */
    int updateMicroWorkSubmit(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 批量删除报工记录信息
     *
     * @param ids
     * @return
     */
    int removeMicroWorkSubmitRecordByIds(Long[] ids);

    /**
     * 删除报工记录
     *
     * @param id
     * @return
     */
    int deleteMicroWorkSubmitById(Long id);

    /**
     * 批量审核接口
     *
     * @param ids
     * @return
     */
    AjaxResult checkSubmitRecordBatch(Long[] ids);

    /**
     * 报工明细 - 审产接口 (需传递 id 和 isLastProcess 字段)
     *
     * @param microWorkSubmits
     * @return
     */
    AjaxResult checkSubmitRecordBatchByProduct(List<MicroWorkSubmit> microWorkSubmits);

    /**
     * 编辑并审核接口
     *
     * @param microWorkSubmitDto
     * @return
     */
    AjaxResult editAndCheck(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 驳回接口
     *
     * @param ids
     * @return
     */
    Boolean rejectSubmitRecord(Long[] ids);

    /**
     * 撤销审核
     *
     * @param id
     * @return
     */
    Boolean undoCheckedRecord(Long id);

    /**
     * 获取不同审核状态下面的报工记录条数
     *
     * @param microWorkSubmit
     * @return
     */
    Map<String, Integer> obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit);

    /**
     * 送检接口
     *
     * @param microWorkSubmitDto
     * @return
     */
    int submittalForInspection(MicroWorkSubmitDto microWorkSubmitDto);

    /**
     * 导出员工的报工记录
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    String exportEmployeeSubmitRecord(Date startDate, Date endDate, String receivedBy);

    /* *************************************
     *            工易派对外接口
     * *************************************/

    /**
     * 工易派 - 工单维度的展示列表
     *
     * @param checkParamByWorkOrder
     * @return
     */
    List<SubmitRecordInfoByWorkOrderDto> selectSubmitRecordInfoByWorkOrder(CheckParamByWorkOrder checkParamByWorkOrder);

    /**
     * 工易派 - 某个工单下详细的报工记录信息
     *
     * @param workOrderNo
     * @return
     */
    List<DetailSubmitRecordInfoByWorkOrder> selectDetailSubmitRecordInfoByWorkOrderList(String workOrderNo, String submitStatus); 

    /**
     * 工易派 - 获取某个工单某个工序下面的到人的报工记录信息
     *
     * @param userSubmitRecordQueryParam
     * @return
     */
    List<UserDetailSubmitRecordInfoByWorkOder> selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam);

    /* *************************************
     *            Ku易记对外接口
     * *************************************/

    /**
     * 产品维度下的审核列表
     *
     * @param checkByProductParam
     * @return
     */
    List<SubmitRecordGroupByProduct> selectSubmitRecordGroupByProductList(CheckByProductParam checkByProductParam);

    /**
     * 产品维度下的具体报工记录整合信息
     *
     * @param checkByProductParam
     * @return
     */
    DetailCheckSubmitRecordByProductRes showDetailSubmitRecordByProduct(CheckByProductParam checkByProductParam);

    /**
     * 查询产品 + 工序的首尾序信息 （标准和非标准工艺）
     *
     * @param productSeq
     * @param processSeq
     * @param standard
     * @return
     */
    FirstOrLastProcess getFirstOrLastProcessFlag(String productSeq, String processSeq, boolean standard);

    /**
     * 批量报工或者送检 （通过构建报工记录的方式）
     *
     * @param batchSubmitOrQcDto
     * @return
     */
    int multiMixed(BatchSubmitOrQcDto batchSubmitOrQcDto);

    /**
     * 计算报工的数量 （已审核、未审核）
     *
     * @param workOrderNo
     * @param processSeq
     * @return
     */
    TotalSubmitNumDTO obtainedSubmitNum(String workOrderNo, String processSeq);

    /**
     * 获取到人的不同状态报工记录汇总信息
     *
     * @param startDate
     * @param endDate
     * @param submitStatus
     * @return
     */
    List<SubmitInfoInDifferentStatusByUser> getSubmitInfoInDifferentStatusByUser(Date startDate, Date endDate, Long submitStatus);
}