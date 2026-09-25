/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.core.web.page.TableDataInfo;
import com.cosmo.hhim.micro.application.service.integration.IMicroFAQFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * 智能问答
 */
@RestController
@RequestMapping("/faq")
public class MicroFAQController extends BaseController { 

    @Autowired
    private IMicroFAQFacadeService faqFacadeService;

    /**
     * 猜你喜欢
     * keyword 用户点击关键词,根据关键词中答案列表过滤
     */
    @GetMapping("/guess")
    public AjaxResult guess(@RequestParam String keyword) {
        return AjaxResult.success(faqFacadeService.guessULike(keyword));
    }

    /**
     * 获取当前用户下拥有的关键词
     */
    @GetMapping("/keyword")
    public AjaxResult keyword() {
        return AjaxResult.success(faqFacadeService.keyword());
    }

    /**
     * 根据预设问题ID获取答案
     */
    @GetMapping("/answer")
    public AjaxResult answer(@RequestParam Long qId) {
        return AjaxResult.success(faqFacadeService.getFixedAnswer(qId));
    }

    /**
     * 根据用户输入的内容获取最相近的答案
     */
    @GetMapping("/post")
    public AjaxResult post(@RequestParam String content) {
        return AjaxResult.success(faqFacadeService.getMostSimilarAnswer(content));
    }

    /**
     * 根据用户输入的内容分词
     */
    @GetMapping("/segment")
    public AjaxResult segment(@RequestParam String content) {
        return AjaxResult.success(faqFacadeService.getSegmentWords(content));
    }

    /**
     * 获取聊天内容
     */
    @GetMapping("/chatHistory")
    public TableDataInfo chatHistory(@RequestParam Integer pageNum, @RequestParam Integer pageSize) {
        return faqFacadeService.getChatHistory(pageNum, pageSize);
    }
}
