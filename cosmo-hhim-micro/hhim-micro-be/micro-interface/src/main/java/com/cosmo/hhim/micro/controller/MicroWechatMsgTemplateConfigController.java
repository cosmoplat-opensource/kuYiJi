/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.micro.infrastructure.enums.NotifyEnums;
import com.cosmo.hhim.micro.integration.domain.entity.MicroWechatMsgTemplateConfig;
import com.cosmo.hhim.micro.integration.domain.service.IMicroWechatMsgTemplateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 微信模版消息配置Controller
 *
 * @author cosmo-hhim-open Team
 * @date 2023-02-22
 */
@RestController
@RequestMapping("/wechatMsgTemplate")
public class MicroWechatMsgTemplateConfigController {
    @Autowired
    private IMicroWechatMsgTemplateConfigService microWechatMsgTemplateConfigService;

    /**
     * 查询微信模版消息配置列表
     */
    @GetMapping("/getMsgTemplateInfos")
    public AjaxResult getMsgTemplateInfos(HttpServletRequest request) {
        String deviceType = request.getHeader(CacheConstants.DETAILS_TYPE);
        String applicationSign = request.getHeader(Constants.APPLICATION_SIGN);
        if (!StringUtils.hasText(deviceType) || !StringUtils.hasText(applicationSign)) {
            throw new CustomException("请求Header中请携带服务标识、应用标识信息！");
        }
        MicroWechatMsgTemplateConfig param = new MicroWechatMsgTemplateConfig();
        param.setServiceSign(applicationSign);
        param.setTemplateType(NotifyEnums.NoticeChannelEnum.getEnumBySign(deviceType).getCode());
        List<MicroWechatMsgTemplateConfig> list = microWechatMsgTemplateConfigService.selectMicroWechatMsgTemplateConfigList(param);
        return AjaxResult.success(list);
    }
}
