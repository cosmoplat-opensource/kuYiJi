/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.dto.integration.MicroCustomerSuggestionDTO;
import com.cosmo.hhim.micro.application.service.integration.CustomerSuggestionFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 * 技术预研团队接入接口
 */
@RestController
@RequestMapping("/suggestion")
public class MicroCustomerSuggestionController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private CustomerSuggestionFacadeService suggestionService;

    /**
     *
     */
    @GetMapping("/list")
    public TableDataInfo getSuggestionList(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return suggestionService.suggestionList(pageNum, pageSize);
    }

    /**
     *
     */
    @PostMapping("/add")
    public AjaxResult getProcessChain(@RequestBody MicroCustomerSuggestionDTO suggestionDTO) {
        return suggestionService.submitNewSuggestion(suggestionDTO) > 0 ? AjaxResult.success() : AjaxResult.error();
    }

}
