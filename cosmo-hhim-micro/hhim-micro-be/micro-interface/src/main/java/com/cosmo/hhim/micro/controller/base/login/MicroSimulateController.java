/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.login;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.micro.base.domain.entity.common.SimulateLoginParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExperienceGuideService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @description 测试模拟使用
 * @createTime 2022-11-03
 */
@Slf4j
@RestController
@RequestMapping("/simulate")
@RequiredArgsConstructor
public class MicroSimulateController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroExperienceGuideService microExperienceGuideService;

    /**
     * 模拟登录（已下线）
     */
    @Deprecated
    @PostMapping("/login")
    public AjaxResult wxMiniAppSimulateLogin(HttpServletRequest request, HttpServletResponse response,
                                             @Valid @RequestBody SimulateLoginParam param) {
        log.warn("[deprecated] /simulate/login 已下线");
        return AjaxResult.error("模拟登录接口已下线");
    }

    /**
     * 重置体验
     *
     * @return
     */
    @Log(title = "测试模拟模块", businessType = BusinessType.DELETE)
    @PutMapping("/resetGuide")
    public AjaxResult resetExperienceGuide(@RequestBody List<String> guideGroupCodes) {
        return AjaxResult.success(microExperienceGuideService.resetExperienceGuide(guideGroupCodes));
    }

}
