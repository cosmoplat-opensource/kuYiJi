/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.submit.BatchSubmitOrQcDto;
import com.cosmo.hhim.micro.application.dto.submit.SubmitInfoInDifferentStatusByUser;
import com.cosmo.hhim.micro.application.dto.submit.SubmitRecordInfoByWorkOrderDto;
import com.cosmo.hhim.micro.application.service.submit.IMicroSubmitFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.check.*;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmit;
import com.cosmo.hhim.micro.base.domain.entity.submit.MicroWorkSubmitDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.SubmitRecordQueryParam;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.enums.ApplicationTypeEnum;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

/**
 * 报工记录Controller
 *
 * @date 2022-10-12
 */
@Validated
@RestController
@RequestMapping("/submit")
public class MicroWorkSubmitController extends BaseController {
    @Autowired
    private IMicroWorkSubmitService microWorkSubmitService;
    @Autowired
    private RedisCache redisCache;
    @Autowired
    private IMicroSubmitFacadeService submitFacadeService;

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @GetMapping("/times")
    public AjaxResult times() {
        Object cache = redisCache.getCacheObject(CommonConstants.SUBMIT_TIMES + SecurityUtils.getUserId());
        return AjaxResult.success(cache == null ? 0 : (int) cache);
    }

    /**
     * 查询报工记录列表
     */
    @GetMapping("/list")
    public TableDataInfo list(MicroWorkSubmit microWorkSubmit) {
        startPage();
        microWorkSubmit.setCreatedBy(String.valueOf(SecurityUtils.getUserId()));
        if (SecurityUtils.getApplicationSign().equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            microWorkSubmit.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        } else {
            microWorkSubmit.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        }
        List<MicroWorkSubmitDto> list = microWorkSubmitService.selectMicroWorkSubmitExList(microWorkSubmit);
        return getDataTable(list);
    }

    /**
     * 获取报工记录详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(microWorkSubmitService.selectMicroWorkSubmitExById(id));
    }

    /**
     * 根据ids获取报工记录详细信息
     */
    @GetMapping(value = "/getInfos")
    public AjaxResult getInfos(@RequestParam("ids") Long[] ids,
                               @RequestParam(name = "submitNickName", required = false) String submitNickName,
                               @RequestParam(name = "submitStatus", required = false) Long submitStatus) {
        return AjaxResult.success(microWorkSubmitService.selectMicroWorkSubmitExListByIds(ids, submitNickName, submitStatus));
    }

    /**
     * 新增报工记录
     */
    @Log(title = "报工模块", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody MicroWorkSubmitDto microWorkSubmitDto) {
        return toAjax(submitFacadeService.createSubmitRecord(microWorkSubmitDto));
    }

    /**
     * 修改报工记录
     */
    @Log(title = "报工模块", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    public AjaxResult edit(@RequestBody MicroWorkSubmitDto microWorkSubmit) {
        return toAjax(submitFacadeService.updateMicroWorkSubmit(microWorkSubmit));
    }

    /**
     * 批量推荐现工序（F02 已下线，兼容前端返回空）
     */
    @Deprecated
    @PostMapping("/multiRecommend")
    public AjaxResult multiRecommend(@RequestBody List<Object> productList) {
        return AjaxResult.success(new java.util.HashMap<>());
    }

    /**
     * 删除报工记录
     */
    @Log(title = "报工模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(submitFacadeService.removeMicroWorkSubmitRecordByIds(ids));
    }

    /**
     * 计算多天的报工产品数量
     *
     * @param dates
     * @return
     */
    @GetMapping("/countByDay")
    public AjaxResult productCountByDay(@RequestParam List<String> dates) {
        return AjaxResult.success(microWorkSubmitService.getProductCountForSubmitByDay(dates));
    }

    /**
     * 获取该月里每天的报工数量
     *
     * @param monthOfYear
     * @return
     */
    @GetMapping("/countByMonth")
    public AjaxResult productCountByMonth(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "月份格式不正确（应为yyyy-MM）") String monthOfYear) {
        return AjaxResult.success(microWorkSubmitService.getProductCountForSubmitByMonth(monthOfYear));
    }

    /**
     * 审核首页 - 产品维度 （待办）
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/checkIndex/product/list")
    public TableDataInfo checkIndexByProduct(MicroWorkSubmitDto microWorkSubmitDto) {
        startPage();
        List<MicroWorkSubmitDto> list = microWorkSubmitService.selectSubmitRecordGroupByProduct(microWorkSubmitDto);
        return getDataTable(list);
    }

    /**
     * 审核首页 - 产品维度 v2版本
     *
     * @param checkByProductParam
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/checkIndex/product/list/v2")
    public TableDataInfo checkIndexByProduct(CheckByProductParam checkByProductParam) {
        startPage();
        List<SubmitRecordGroupByProduct> list = submitFacadeService.selectSubmitRecordGroupByProductList(checkByProductParam);
        return getDataTable(list);
    }

    /**
     * 审核首页 - 产品维度v2版本 详细的报工记录信息
     *
     * @param checkByProductParam
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/checkIndex/product/detailRecords")
    public AjaxResult showDetailRecordsByProduct(CheckByProductParam checkByProductParam) {
        DetailCheckSubmitRecordByProductRes res = submitFacadeService.showDetailSubmitRecordByProduct(checkByProductParam);
        return AjaxResult.success(res);
    }

    /**
     * 审核首页 - 产品维度 （待办数量）
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/getTodoNumsByProduct")
    public AjaxResult getTodoNumsByProduct(MicroWorkSubmitDto microWorkSubmitDto) {
        return AjaxResult.success(microWorkSubmitService.obtainedTodoNumsByProduct(microWorkSubmitDto));
    }

    /**
     * 审核首页 - 员工维度
     *
     * @param submitRecordQueryParam
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/checkIndex/user/list")
    public TableDataInfo checkIndexByUser(SubmitRecordQueryParam submitRecordQueryParam) {
        startPage();
        List<MicroWorkSubmitDto> list = microWorkSubmitService.selectSubmitRecordByUser(submitRecordQueryParam);
        return getDataTable(list);
    }

    /**
     * 审核首页 - 员工维度 （待办数量）
     *
     * @param microWorkSubmitDto
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/getTodoNumsByUser")
    public AjaxResult getTodoNumsByUser(MicroWorkSubmitDto microWorkSubmitDto) {
        return AjaxResult.success(microWorkSubmitService.obtainedTodoNumsByUser(microWorkSubmitDto));
    }

    /**
     * 批量审核
     *
     * @param ids
     * @return
     */
    @Log(title = "报工模块", businessType = BusinessType.OTHER)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/check/{ids}")
    public AjaxResult checkSubmitRecordBatch(@PathVariable Long[] ids) {
        return submitFacadeService.checkSubmitRecordBatch(ids);
    }

    /**
     * 报产明细(产品维度) - 审产接口
     * 需传递 id 和 isLastProcess 字段
     *
     * @param microWorkSubmitList
     * @return
     */
    @Log(title = "报工模块", businessType = BusinessType.OTHER)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @PostMapping("/check")
    public AjaxResult checkSubmitRecordBatchByProduct(@RequestBody List<MicroWorkSubmit> microWorkSubmitList) {
        return submitFacadeService.checkSubmitRecordBatchByProduct(microWorkSubmitList);
    }

    /**
     * 编辑并审核界面
     *
     * @param microWorkSubmitDto
     * @return
     */
    @Log(title = "报工模块", businessType = BusinessType.OTHER)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.TRIAL_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @PostMapping("/editAndCheck")
    public AjaxResult editAndCheck(@RequestBody MicroWorkSubmitDto microWorkSubmitDto) {
        return submitFacadeService.editAndCheck(microWorkSubmitDto);
    }

    /**
     * 找到产品的所有工序
     *
     * @param productSeq
     * @return
     */
    @GetMapping("/allProcess")
    public AjaxResult findAllProcess(@RequestParam("productSeq") String productSeq,
                                     @RequestParam(required = false) String isLastProcess) {
        return AjaxResult.success(microWorkSubmitService.findProcessListByProductSeq(productSeq, isLastProcess));
    }

    /**
     * 是否已经有最后一道工序
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    @GetMapping("/isLastProcess")
    @Deprecated
    public AjaxResult checkIsLastProcess(@RequestParam(required = false) String productSeq,
                                         @RequestParam(required = false) String processSeq) {
        return AjaxResult.success(microWorkSubmitService.checkIsLastProcess(productSeq, processSeq));
    }

    /**
     * 获取产品（产品 + 工序）的良品和不良品率
     *
     * @param microWorkSubmitDto
     * @return
     */
    @GetMapping("/completedProductInformation")
    public AjaxResult completedProductInformation(MicroWorkSubmitDto microWorkSubmitDto) {
        return AjaxResult.success(microWorkSubmitService.selectCompletedProductList(microWorkSubmitDto));
    }

    /**
     * 查询首次报工或审产信息
     *
     * @param operateType
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/getFirstOperateInfo")
    public AjaxResult getFirstOperateInfo(@RequestParam("operateType") String operateType) {
        return AjaxResult.success(microWorkSubmitService.selectFirstSubmitOrCheckInfo(operateType));
    }

    /**
     * 撤销审核记录
     *
     * @param id
     * @return
     */
    @GetMapping("/undoCheckedRecord")
    public AjaxResult undoCheckedRecord(Long id) {
        return AjaxResult.success(submitFacadeService.undoCheckedRecord(id));
    }

    /**
     * 首序和尾序判断接口统一
     *
     * @param productSeq
     * @param processSeq
     * @param processType
     * @return
     */
    @GetMapping("/isOrNotHaveFirstOrLastProcess")
    public AjaxResult checkIsOrNotHaveFirstOrLastProcess(@RequestParam(required = false) String productSeq,
                                                         @RequestParam(required = false) String processSeq,
                                                         @RequestParam("processType") String processType) {
        return AjaxResult.success(microWorkSubmitService.checkIsOrNotHaveFirstOrLastProcess(productSeq, processSeq, processType));
    }

    /**
     * 异常提示
     *
     * @param ids
     * @return
     */
    @GetMapping("/tipsForCheck")
    public AjaxResult tipsForCheck(@RequestParam("ids") Long[] ids) {
        return AjaxResult.success(microWorkSubmitService.tipsForCheck(ids));
    }

    /**
     * 编辑并审核接口调用前的异常判断 -> 用于编辑之后异常判断的方法
     *
     * @param microWorkSubmitDto
     * @return
     */
    @PostMapping("/tipsForEditAndCheck")
    public AjaxResult tipsForEditAndCheck(@RequestBody MicroWorkSubmitDto microWorkSubmitDto) {
        return AjaxResult.success(microWorkSubmitService.tipsForEditAndCheck(microWorkSubmitDto));
    }

    /**
     * 报工记录审核驳回
     *
     * @param ids
     * @return
     */
    @GetMapping("/rejectRecord")
    public AjaxResult rejectSubmitRecord(@RequestParam("ids") Long[] ids) {
        return AjaxResult.success(submitFacadeService.rejectSubmitRecord(ids));
    }

    /**
     * 根据产品 + 工序 获取首尾序的标示
     *
     * @param productSeq
     * @param processSeq
     * @return
     */
    @GetMapping("/getFirstOrLastProcessFlag")
    public AjaxResult getFirstOrLastProcessFlag(@RequestParam String productSeq,
                                                @RequestParam String processSeq,
                                                @RequestParam boolean standard) {
        return AjaxResult.success(submitFacadeService.getFirstOrLastProcessFlag(productSeq,processSeq,standard));
    }

    /**
     * 工易派 - 工单维度审核展示列表
     *
     * @param checkParamByWorkOrder
     * @return
     */
    @GetMapping("/checkIndex/workOrder/list")
    public TableDataInfo selectSubmitRecordInfoByWorkOrder(CheckParamByWorkOrder checkParamByWorkOrder) {
        startPage();
        List<SubmitRecordInfoByWorkOrderDto> list = submitFacadeService.selectSubmitRecordInfoByWorkOrder(checkParamByWorkOrder);
        return getDataTable(list);
    }

    /**
     * 工易派 - 工单下的详细报工记录信息
     *
     * @param workOrderNo
     * @return
     */
    @GetMapping("/check/detailRecord/list")
    public AjaxResult selectDetailSubmitRecordInfoByWorkOrderList(@RequestParam String workOrderNo,
                                                                  @RequestParam String submitStatus) {
        return AjaxResult.success(submitFacadeService.selectDetailSubmitRecordInfoByWorkOrderList(workOrderNo, submitStatus));
    }

    /**
     * 工易派 - 获取某个工单某个工序下面的到人的报工记录信息
     *
     * @param userSubmitRecordQueryParam
     * @return
     */
    @GetMapping("/check/detailRecord/user/list")
    public AjaxResult selectUserDetailSubmitRecordInfoByWorkOder(UserSubmitRecordQueryParam userSubmitRecordQueryParam) {
        return AjaxResult.success(submitFacadeService.selectUserDetailSubmitRecordInfoByWorkOder(userSubmitRecordQueryParam));
    }

    /**
     * 获取不同单据状态下的记录数
     *
     * @param microWorkSubmit
     * @return
     */
    @GetMapping("/obtainedSubmitRecordNumInDifferentStatus")
    public AjaxResult obtainedSubmitRecordNumInDifferentStatus(MicroWorkSubmit microWorkSubmit) {
        String appSign = SecurityUtils.getApplicationSign();
        if (appSign.equals(ApplicationTypeEnum.GONG_YI_PAI.getCode())) {
            microWorkSubmit.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        } else if (appSign.equals(ApplicationTypeEnum.KU_YI_JI.getCode())) {
            microWorkSubmit.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        }
        microWorkSubmit.setSubmitUser(SecurityUtils.getUserId().toString());
        return AjaxResult.success(submitFacadeService.obtainedSubmitRecordNumInDifferentStatus(microWorkSubmit));
    }

    /**
     * 多产品多工序批量报工
     *
     * @param batchSubmitOrQcDto
     * @return
     */
    @PostMapping("/multiMixed")
    public AjaxResult multiMixed(@RequestBody BatchSubmitOrQcDto batchSubmitOrQcDto) {
        return toAjax(submitFacadeService.multiMixed(batchSubmitOrQcDto));
    }

    /**
     * 计算报工的数量 （已审核、未审核）
     *
     * @param workOrderNo
     * @param processSeq
     * @return
     */
    @GetMapping("/obtainedSubmitNum")
    public AjaxResult obtainedSubmitNum(@RequestParam String workOrderNo,
                                        @RequestParam String processSeq) {
        return AjaxResult.success(submitFacadeService.obtainedSubmitNum(workOrderNo, processSeq));
    }

    /**
     * 获取到人的不同状态报工记录汇总信息
     *
     * @param startDate
     * @param endDate
     * @param submitStatus
     * @return
     */
    @GetMapping("/listByStatus")
    public TableDataInfo getSubmitRecordListByStatus(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                     @RequestParam Long submitStatus) {
        startPage();
        List<SubmitInfoInDifferentStatusByUser> list = submitFacadeService.getSubmitInfoInDifferentStatusByUser(startDate, endDate, submitStatus);
        return getDataTable(list);
    }

    /**
     * 获取用户不同状态报工记录汇总信息
     *
     * @param startDate
     * @param endDate
     * @param submitStatus
     * @return
     */
    @GetMapping("/summaryInfoByStatus")
    public AjaxResult summaryInfoByStatus(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                          @RequestParam Long submitStatus) {
        return AjaxResult.success(microWorkSubmitService.getSubmitSummaryInfoByUser(startDate, endDate, submitStatus));
    }
}

