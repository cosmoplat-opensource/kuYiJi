/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.follow;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.dto.base.MicroFollowDTO;
import com.cosmo.hhim.micro.application.service.base.IMicroFollowFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/follow")
public class MicroFollowController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroFollowFacadeService facadeService;

    /**
     * 获取关注列表
     */
    @GetMapping("/list")
    public AjaxResult findFollowList(@RequestParam String followType) {
        return AjaxResult.success(facadeService.findFollow(followType));
    }

    /**
     * 关注某对象
     */
    @PostMapping("/do")
    public AjaxResult follow(@RequestBody MicroFollowDTO followDTO) {
        return AjaxResult.success(facadeService.follow(followDTO));
    }

    /**
     * 取关某对象
     */
    @PostMapping("/undo")
    public AjaxResult unfollowed(@RequestBody MicroFollowDTO followDTO) {
        return AjaxResult.success(facadeService.unfollowed(followDTO));
    }
}
