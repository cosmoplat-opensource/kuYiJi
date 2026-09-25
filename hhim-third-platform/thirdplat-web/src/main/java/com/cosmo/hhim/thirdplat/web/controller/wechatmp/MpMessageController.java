/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.MsgTemplateParam;
import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.MsgTemplateInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatmp.IMpMessageService;
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
import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Slf4j
@RestController
@RequestMapping("/wechatmp/message")
public class MpMessageController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMpMessageService mpMessageService;

    /**
     * 根据模板库中模板的编号获取模版ID
     * @param templateIdShort
     * @return
     */
    @GetMapping("/getTempId")
    public APIResponse<String> getTemplateId(String templateIdShort){
        return mpMessageService.getTemplateId(templateIdShort);
    }


    /**
     * 获取已添加至帐号下所有模板列表信息
     * @return
     */
    @GetMapping("/getTempList")
    public APIResponse<List<MsgTemplateInfoResult>> getTempList(){
        return mpMessageService.getTempList();
    }

    /**
     * 发送模版消息（通用）
     * @param param
     * @return
     */
    @PostMapping("/sendTempMessage")
    public APIResponse<String> sendTemplateMessage(@Valid @RequestBody MsgTemplateParam param){
        return mpMessageService.sendTemplateMessage(param);
    }




}
