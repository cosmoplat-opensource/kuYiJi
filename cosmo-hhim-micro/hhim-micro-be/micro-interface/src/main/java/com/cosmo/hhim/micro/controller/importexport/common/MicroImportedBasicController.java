/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.importexport.common;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.service.base.IMicroImportedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/5/9
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/basic/import")
@RequiredArgsConstructor
public class MicroImportedBasicController {

    private final IMicroImportedService microImportedService;

    /**
     * 获取导入临时链接
     *
     * @return
     */
    @GetMapping("/importLinkTemp")
    public AjaxResult genImportLinkTemp(
            @RequestParam("taskType") @Pattern(regexp = "^[a-zA-Z0-9_]{1,64}$", message = "taskType包含非法字符") String taskType) {
        // 校验任务类型参数（JSR303 @Pattern 参数校验）：仅允许字母数字下划线，防止拼入导入链接时产生注入/反射型 XSS
        return AjaxResult.success(microImportedService.genImportLinkTemp(taskType));
    }

}
