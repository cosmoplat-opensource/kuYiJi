/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.integration;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.micro.application.dto.integration.MicroUserEventTrackingDTO;
import com.cosmo.hhim.micro.application.service.integration.IMicroUserEventTrackingFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletRequest;


/**
 * 用户行为事件埋点Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-05-23
 */
@RestController
@RequestMapping("/tracking")
public class MicroUserEventTrackingController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroUserEventTrackingFacadeService trackingFacadeService;

    /**
     * 新增用户行为事件埋点
     */
    @PostMapping("/collect")
    public void collect(@RequestBody MicroUserEventTrackingDTO trackingDTO, HttpServletRequest request) {
        trackingFacadeService.collectUserEvent(trackingDTO, request);
    }
}
