/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.login;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.log.annotation.Log;
import com.cosmo.hhim.common.log.enums.BusinessType;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.base.domain.entity.common.GuideNodeChangeStatusParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroExperienceGuideService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;

/**
 * 体验引导节点配置Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2022-11-01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/experience/guide")
public class MicroExperienceGuideController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroExperienceGuideService microExperienceGuideService;

    /**
     * 获取体验引导标识
     *
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.WORKER_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @GetMapping("/guideFlag")
    public AjaxResult getGuideFlag() {
        return AjaxResult.success(microExperienceGuideService.getGuideFlag());
    }

    /**
     * 根据引导组编码查询引导节点列表
     *
     * @param groupCode
     * @return
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    @GetMapping("/guideList")
    public AjaxResult getGuideList(@RequestParam("groupCode") String groupCode) {
        return AjaxResult.success(microExperienceGuideService.getGuideList(groupCode));
    }

    /**
     * 更新节点状态
     *
     * @param param
     * @return
     */
    @Log(title = "体验引导模块", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    @PutMapping("/updateStatus")
    public AjaxResult updateNodeStatus(@Valid @RequestBody GuideNodeChangeStatusParam param) {
        return AjaxResult.success(microExperienceGuideService.updateNodeStatus(param));
    }

    /**
     * 跳过体验引导
     *
     * @return
     */
    @Log(title = "体验引导模块", businessType = BusinessType.UPDATE)
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    @PutMapping("/skipGuide")
    public AjaxResult skipGuide(@RequestBody(required = false) List<String> guideGroupCodes) {
        return AjaxResult.success(microExperienceGuideService.skipGuide(guideGroupCodes));
    }


}
