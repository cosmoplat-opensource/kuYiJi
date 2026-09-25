/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.test;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.application.service.integration.IMicroReportFacadeService;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.Date;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/6/9
 */
@RestController
@RequestMapping("/test/zyh")
public class TestController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMicroReportFacadeService microReportfacadeService;

    /**
     * 发送生产日报/周报
     */
    @GetMapping("/notice/send")
    public AjaxResult sendWxMiniProgram(@RequestParam("businessSign") NotifyEnums.NoticeBusinessSignEnum businessSign) {
        microReportfacadeService.pushMessageByNoticeConfig(new Date(), businessSign);
        return AjaxResult.success();
    }

}
