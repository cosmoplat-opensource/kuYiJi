/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.CreateMenuParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@RestController
@RequestMapping("/wechatmp/menu")
public class MpMenuController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMpMenuService mpMenuService;

    /**
     * 创建自定义菜单
     * @param param
     * @return
     */
    @PostMapping("/create")
    public APIResponse<Boolean> createMenu(@Valid @RequestBody CreateMenuParam param){
        return mpMenuService.createMenu(param);
    }

}
