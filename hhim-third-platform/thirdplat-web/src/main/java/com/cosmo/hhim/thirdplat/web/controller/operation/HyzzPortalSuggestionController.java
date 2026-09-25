/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;


import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzFaqHistoryEntity;
import com.cosmo.hhim.thirdplat.api.operation.domain.HyzzPortalSuggestion;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzPortalSuggestionService;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import java.util.List;

/**
 * 门户的意见反馈Controller
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/operation/api/suggestion")
public class HyzzPortalSuggestionController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IHyzzPortalSuggestionService hyzzPortalSuggestionService;

    /**
     * 查询门户的意见反馈列表
     */
    @GetMapping("/list")
    public APIResponse<TableDataInfo> list(HyzzPortalSuggestion hyzzPortalSuggestion) {
        PageHelper.startPage(hyzzPortalSuggestion.getPageNum(), hyzzPortalSuggestion.getPageSize(), null);
        List<HyzzPortalSuggestion> list = hyzzPortalSuggestionService.selectHyzzPortalSuggestionList(hyzzPortalSuggestion);
        return APIResponse.success(getDataTable(list));
    }

    /**
     * 查询意见反馈列表(以问题/回答方式返回)
     * 根据提出问题人+租户+来源
     */
    @PostMapping("/chatHistory")
    public APIResponse<TableDataInfo> chatHistory(@RequestBody HyzzPortalSuggestion hyzzPortalSuggestion) {
        PageHelper.startPage(hyzzPortalSuggestion.getPageNum(), hyzzPortalSuggestion.getPageSize(), null);
        List<HyzzFaqHistoryEntity> list = hyzzPortalSuggestionService.selectChatHistory(hyzzPortalSuggestion);
        return APIResponse.success(getDataTable(list));
    }


    /**
     * 获取门户的意见反馈详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return AjaxResult.success(hyzzPortalSuggestionService.selectHyzzPortalSuggestionById(id));
    }

    /**
     * 新增门户的意见反馈
     */
    @PostMapping
    public AjaxResult add(@RequestBody HyzzPortalSuggestion hyzzPortalSuggestion) {
        hyzzPortalSuggestion.setCreateTime(DateUtils.getNowDate());
        return toAjax(hyzzPortalSuggestionService.addHyzzPortalSuggestion(hyzzPortalSuggestion));
    }


}
