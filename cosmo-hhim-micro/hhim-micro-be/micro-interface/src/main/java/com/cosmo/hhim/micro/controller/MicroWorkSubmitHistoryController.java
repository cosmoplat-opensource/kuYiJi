/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.base.domain.service.submit.IMicroWorkSubmitHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author cosmo-hhim-open Team
 * @description: 报工历史记录Controller
 * @date 2022/11/29 3:07 下午
 */
@RestController
@RequestMapping("/submit/history")
public class MicroWorkSubmitHistoryController extends BaseController {

    @Autowired
    private IMicroWorkSubmitHistoryService microWorkSubmitHistoryService;

    @GetMapping("/recordDetail")
    public AjaxResult historyRecordDetail(@RequestParam(name = "submitNo") String submitNo) {
        return AjaxResult.success(microWorkSubmitHistoryService.selectMicroWorkSubmitHistoryListBySubmitNo(submitNo));
    }
}
