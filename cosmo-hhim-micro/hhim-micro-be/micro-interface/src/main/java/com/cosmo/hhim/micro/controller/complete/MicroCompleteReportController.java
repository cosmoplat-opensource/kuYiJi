/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.complete;

import cn.hutool.core.date.DateUtil;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.dto.complete.MicroCompleteReportPlanWaitParam;
import com.cosmo.hhim.micro.application.dto.complete.MicroWaitCompleteReportIndexResult;
import com.cosmo.hhim.micro.application.service.complete.IMicroCompleteReportFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealQueryDto;
import com.cosmo.hhim.micro.base.domain.entity.submit.WaitDealResultDto;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReport;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportBasicParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupPlanResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductGroupResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideDetailResult;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportProductSideParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsParam;
import com.cosmo.hhim.micro.complete.domain.entity.MicroCompleteReportStatisticsProductResult;
import com.cosmo.hhim.micro.complete.domain.service.IMicroCompleteReportService;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import com.cosmo.hhim.micro.planning.domain.entity.work.MicroManufactureWorkOrder;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;
import java.util.List;

/**
 * 完工报告单Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-03-23
 */
@Api
@RestController
@RequestMapping("/complete/report")
public class MicroCompleteReportController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroCompleteReportService microCompleteReportService;
    @Autowired
    private IMicroCompleteReportFacadeService microCompleteReportFacadeService;

    /**
     * 查询待处理的完工产品列表（KU易记）
     *
     * @param queryDto
     * @return
     */
    @GetMapping("/waitDeal/list")
    public TableDataInfo waitDealList(WaitDealQueryDto queryDto) {
        startPage();
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            queryDto.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            queryDto.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        List<WaitDealResultDto> list = microCompleteReportFacadeService.waitDealList(queryDto);
        return getDataTable(list);
    }

    /**
     * 查询待处理的完工产品列表（工易派）
     *
     * @param queryParam
     * @return
     */
    @GetMapping("/plan/waitDeal/list")
    public TableDataInfo planWaitDealList(MicroCompleteReportPlanWaitParam queryParam) {
        startPage();
        List<MicroManufactureWorkOrder> list = microCompleteReportFacadeService.waitDealList(queryParam);
        return getDataTable(list);
    }

    /**
     * 待确认-完工报告导出发送邮件
     *
     * @param receivedBy
     * @return
     */
    @Log(title = "完工报告模块", businessType = BusinessType.EXPORT)
    @GetMapping("/waitDeal/export")
    public AjaxResult exportCompleteReportWaitDealRecord(@RequestParam(name = "receivedBy") String receivedBy,
                                                         @RequestParam(name = "productSeqOrName", required = false) String productSeqOrName,
                                                         @RequestParam(name = "nickName", required = false) String nickName) {
        WaitDealQueryDto queryParam = new WaitDealQueryDto();
        queryParam.setNickName(nickName);
        queryParam.setProductSeqOrName(productSeqOrName);
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            queryParam.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        return AjaxResult.success(microCompleteReportFacadeService.exportCompleteReportWaitDealRecord(receivedBy, queryParam));
    }

    /**
     * 统计待处理的完工指标
     *
     * @return
     */
    @GetMapping("/waitDeal/statistics")
    public MicroWaitCompleteReportIndexResult statisticsWaitCompleteReportIndex() {
        WaitDealQueryDto queryDto = new WaitDealQueryDto();
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            queryDto.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            queryDto.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        return microCompleteReportFacadeService.statisticsWaitCompleteReportIndex(queryDto);
    }

    /**
     * 查询完工报告单列表（KU易记）-完工报告维度
     */
    @GetMapping("/finishDeal/list")
    public TableDataInfo finishDealList(MicroCompleteReportParam queryParam) {
        startPage();
        String appSign = SecurityUtils.getApplicationSign();
        if (StringUtils.hasText(appSign)) {
            queryParam.setSourceChannel(appSign);
        }
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        List<MicroCompleteReportGroupResult> list = microCompleteReportFacadeService.finishDealList(queryParam);
        return getDataTable(list);
    }

    /**
     * 查询完工报告单列表（KU易记）-产品维度
     */
    @GetMapping("/finishDeal/productSide/list")
    public TableDataInfo finishDealProductSideList(MicroCompleteReportProductSideParam queryParam) {
        startPage();
        String appSign = SecurityUtils.getApplicationSign();
        if (StringUtils.hasText(appSign)) {
            queryParam.setSourceChannel(appSign);
        }
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        List<MicroCompleteReportProductGroupResult> list = microCompleteReportFacadeService.finishDealProductSideList(queryParam);
        return getDataTable(list);
    }

    /**
     * 查询完工报告单列表（工易派）-完工报告维度
     */
    @GetMapping("/finishDeal/plan/list")
    public TableDataInfo finishDealPlanList(MicroCompleteReportParam queryParam) {
        startPage();
        String appSign = SecurityUtils.getApplicationSign();
        if (StringUtils.hasText(appSign)) {
            queryParam.setSourceChannel(appSign);
        }
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        List<MicroCompleteReportGroupPlanResult> list = microCompleteReportFacadeService.finishDealPlanList(queryParam);
        return getDataTable(list);
    }

    /**
     * 已完成-完工报告导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "完工报告模块", businessType = BusinessType.EXPORT)
    @GetMapping("/finishDeal/export")
    public AjaxResult exportCompleteReportFinishDealRecord(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                           @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                           @RequestParam(name = "receivedBy") String receivedBy) {
        if (null != startDate) {
            startDate = DateUtil.beginOfDay(startDate).toJdkDate();
        }
        if (null != endDate) {
            endDate = DateUtil.endOfDay(endDate).toJdkDate();
        }
        return AjaxResult.success(microCompleteReportFacadeService.exportCompleteReportFinishDealRecord(startDate, endDate, receivedBy));
    }

    /**
     * 获取完工报告单详细信息-按照产品分组汇总
     */
    @GetMapping(value = "/group/{reportNo}")
    public AjaxResult getDetailGroupInfo(@PathVariable("reportNo") String reportNo) {
        return AjaxResult.success(microCompleteReportFacadeService.getDetailGroupInfo(reportNo));
    }

    /**
     * 获取完工报告单详细信息--完工报告维度
     */
    @GetMapping(value = "/{reportNo}")
    public AjaxResult getDetailInfo(@PathVariable("reportNo") String reportNo) {
        return AjaxResult.success(microCompleteReportFacadeService.getDetailInfo(reportNo));
    }

    /**
     * 获取完工报告单详细信息列表（KU易记）--产品维度
     */
    @GetMapping("/productSide/detailList")
    public TableDataInfo getDetailInfoForProductSide(MicroCompleteReportBasicParam queryParam) {
        startPage();
        String appSign = SecurityUtils.getApplicationSign();
        if (StringUtils.hasText(appSign)) {
            queryParam.setSourceChannel(appSign);
        }
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        List<MicroCompleteReportProductSideDetailResult> list = microCompleteReportFacadeService.getDetailInfoForProductSide(queryParam);
        return getDataTable(list);
    }

    /**
     * 新增完工报告单
     */
    @Log(title = "完工报告模块", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@RequestBody List<MicroCompleteReport> microCompleteReports) {
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            microCompleteReportFacadeService.addCompleteReport(microCompleteReports);
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            microCompleteReportFacadeService.addPlanCompleteReport(microCompleteReports);
        }
        return AjaxResult.success();
    }

    /**
     * 撤销完工报告单前置校验
     */
    @GetMapping("/cancelcheck/{reportNo}")
    public AjaxResult cancelCheck(@PathVariable("reportNo") String reportNo) {
        microCompleteReportFacadeService.cancelCheck(reportNo);
        return AjaxResult.success();
    }

    /**
     * 撤销完工报告单（KU易记）
     */
    @Log(title = "完工报告模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/cancel/{reportNo}")
    public AjaxResult cancel(@PathVariable("reportNo") String reportNo) {
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            microCompleteReportFacadeService.cancelCompleteReport(reportNo);
        }
        return AjaxResult.success();
    }

    /**
     * 撤销完工报告单前置校验（工易派）
     */
    @GetMapping("/cancelcheck/plan/{id}")
    public AjaxResult cancelCheckPlan(@PathVariable("id") Long id) {
        microCompleteReportFacadeService.cancelCheckPlan(id);
        return AjaxResult.success();
    }

    /**
     * 撤销完工报告单（工易派）
     */
    @Log(title = "完工报告模块", businessType = BusinessType.DELETE)
    @DeleteMapping("/cancel/plan/{id}")
    public AjaxResult cancelPlan(@PathVariable("id") Long id) {
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            microCompleteReportFacadeService.cancelPlanCompleteReport(id);
        }
        return AjaxResult.success();
    }

    /**
     * 按天统计完工报告情况
     *
     * @param queryParam
     * @return
     */
    @GetMapping("/statistics/day")
    public AjaxResult statisticsCompleteNumGroupByDay(MicroCompleteReportStatisticsParam queryParam) {
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        return AjaxResult.success(microCompleteReportService.selectMicroCompleteNumGroupByDay(queryParam));
    }

    /**
     * 完工统计导出发送邮件
     *
     * @param startDate
     * @param endDate
     * @param receivedBy
     * @return
     */
    @Log(title = "完工报告模块", businessType = BusinessType.EXPORT)
    @GetMapping("/statistics/export")
    public AjaxResult exportCompleteReportStatisticsRecord(@RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                           @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                           @RequestParam(name = "receivedBy") String receivedBy) {
        if (null != startDate) {
            startDate = DateUtil.beginOfDay(startDate).toJdkDate();
        }
        if (null != endDate) {
            endDate = DateUtil.endOfDay(endDate).toJdkDate();
        }
        return AjaxResult.success(microCompleteReportFacadeService.exportCompleteReportStatisticsRecord(startDate, endDate, receivedBy));
    }

    /**
     * 按产品统计完工报告情况
     *
     * @param queryParam
     * @return
     */
    @GetMapping("/statistics/product")
    public TableDataInfo statisticsCompleteNumGroupByProduct(MicroCompleteReportStatisticsParam queryParam) {
        startPage();
        if (null != queryParam.getStartDate()) {
            queryParam.setStartDate(DateUtil.beginOfDay(queryParam.getStartDate()).toJdkDate());
        }
        if (null != queryParam.getEndDate()) {
            queryParam.setEndDate(DateUtil.endOfDay(queryParam.getEndDate()).toJdkDate());
        }
        List<MicroCompleteReportStatisticsProductResult> list = microCompleteReportFacadeService.statisticsCompleteNumGroupByProduct(queryParam);
        return getDataTable(list);
    }

}
