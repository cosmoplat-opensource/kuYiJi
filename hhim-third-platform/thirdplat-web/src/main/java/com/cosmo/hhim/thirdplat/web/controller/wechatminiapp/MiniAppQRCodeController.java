/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.wechatminiapp.IMiniAppQRCodeService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@Slf4j
@RestController
@RequestMapping("/wechat/miniapp/qrCode")
public class MiniAppQRCodeController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IMiniAppQRCodeService miniAppQRCodeService;

    /**
     * 获取小程序码
     *
     * @param param
     * @return
     */
    @GetMapping("/getQrCode")
    public APIResponse<QRCodeInfo> getMiniAppQrCode(QRCodeParam param) {
        return miniAppQRCodeService.getMiniAppQrCode(param);
    }

    /**
     * 获取小程序码(无限制)
     *
     * @param param
     * @return
     */
    @GetMapping("/getUnlimitQrCode")
    public APIResponse<QRCodeInfo> getMiniAppUnLimitQrCode(UnlimitedQRCodeParam param) {
        return miniAppQRCodeService.getMiniAppUnLimitQrCode(param);
    }

    /**
     * 获取小程序Url Link
     *
     * @param param
     * @return
     */
    @GetMapping("/getUrlLink")
    public APIResponse<UrlLinkInfo> getMiniAppUrlLink(UrlLinkParam param) {
        return miniAppQRCodeService.getMiniAppUrlLink(param);
    }

    /**
     * 获取小程序scheme
     *
     * @param param
     * @return
     */
    @GetMapping("/getScheme")
    public APIResponse<SchemeCodeInfo> getMiniAppScheme(SchemeCodeParam param) {
        return miniAppQRCodeService.getMiniAppScheme(param);
    }

}
