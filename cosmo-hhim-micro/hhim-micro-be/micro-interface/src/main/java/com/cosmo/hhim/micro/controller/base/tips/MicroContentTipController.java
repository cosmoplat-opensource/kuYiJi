/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.tips;

import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.tips.intercept.anno.ContentTip;
import com.cosmo.hhim.micro.infrastructure.enums.ActionTypeEnums;
import com.cosmo.hhim.micro.infrastructure.enums.TipTriggerActionEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/8
 */
@Slf4j
@RestController
@RequestMapping("/tips")
public class MicroContentTipController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }


    /**
     * 进入小程序
     *
     * @return
     */
    @ContentTip(tipTriggerActions = TipTriggerActionEnum.ENTER_PROGRAM)
    @GetMapping("/enterMiniProgram")
    public AjaxResult enterMiniProgram() {
        log.info("触发动作--->进入小程序，UserId:{}", SecurityUtils.getUserId());
        return AjaxResult.success();
    }

    /**
     * 切换页面
     *
     * @return
     */
    @ContentTip(tipTriggerActions = TipTriggerActionEnum.SWITCH_PAGE)
    @GetMapping("/switchPage")
    public AjaxResult switchPage(@RequestParam("switchPageType") ActionTypeEnums.SwitchPageActionEnum switchPageType) {
        log.info("触发动作--->切换页面，UserId:{}", SecurityUtils.getUserId());
        return AjaxResult.success();
    }


    /**
     * 数据提交
     *
     * @return
     */
    @ContentTip(tipTriggerActions = TipTriggerActionEnum.SUBMIT_FINISH)
    @GetMapping("/submit")
    public AjaxResult submit(@RequestParam("submitActionType") ActionTypeEnums.SubmitActionEnum submitActionType) {
        log.info("触发动作--->数据提交，UserId:{}", SecurityUtils.getUserId());
        return AjaxResult.success();
    }

}
