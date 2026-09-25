/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.common.core.web.page.TableSupport;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.application.service.base.IMicroStaffCalendarFacadeService;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffProcessSeqInfoForProduct;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkCalendarDetailParam;
import com.cosmo.hhim.micro.base.domain.entity.common.StaffWorkStatisticParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroStaffCalendarService;
import com.cosmo.hhim.micro.infrastructure.enums.SubmitTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;


/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-03
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/staff/calendar")
public class MicroStaffCalendarController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroStaffCalendarService microStaffCalendarService;
    @Autowired
    private IMicroStaffCalendarFacadeService calendarFacadeService;

    /**
     * 查询员工记工统计信息
     *
     * @return
     */
    @GetMapping("/statistic")
    public AjaxResult queryStaffWorkStatistic(@Valid StaffWorkStatisticParam param) {
        param.genQueryTimeRange();
        // 获取应用标示，确定报工方式
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        return AjaxResult.success(calendarFacadeService.summaryStaffStatistics(param));
    }

    /**
     * 查询员工记工日历统计明细
     *
     * @return
     */
    @GetMapping("/calendarDetail")
    public AjaxResult queryStaffCalendarDetail(@Valid StaffWorkCalendarDetailParam param) {
        param.genQueryTimeRange();
        // 获取应用标示，确定报工方式
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        return AjaxResult.success(microStaffCalendarService.queryStaffCalendarDetail(param));
    }

    /**
     * 按照产品种类分组查询员工工序明细信息
     *
     * @param param
     * @return
     */
    @GetMapping("/processSeqsForProduct")
    public TableDataInfo queryProcessSeqDetailForProduct(@Valid StaffWorkStatisticParam param) {
        param.genQueryTimeRange();
        // 获取应用标示，确定报工方式
        String appSign = SecurityUtils.getApplicationSign();
        if (CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.KU_YI_JI_SUBMIT.getCode());
        } else if (CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign)) {
            param.setSubmitType(SubmitTypeEnum.GONG_YI_PAI_SUBMIT.getCode());
        }
        List<StaffProcessSeqInfoForProduct> list = microStaffCalendarService.queryProcessSeqDetailForProduct(TableSupport.buildPageRequest(), param);
        return getDataTable(list);
    }

}
