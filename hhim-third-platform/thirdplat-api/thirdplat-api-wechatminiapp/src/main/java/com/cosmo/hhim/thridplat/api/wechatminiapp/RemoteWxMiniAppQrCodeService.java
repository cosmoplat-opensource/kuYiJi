/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.factory.RemoteWxMiniAppQrCodeFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@FeignClient(contextId = "RemoteWxMiniAppQrCodeService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWxMiniAppQrCodeFallbackFactory.class)
public interface RemoteWxMiniAppQrCodeService {

    /**
     * 获取小程序码
     *
     * @param param
     * @return
     */
    @GetMapping("/wechat/miniapp/qrCode/getQrCode")
    APIResponse<QRCodeInfo> getMiniAppQrCode(@SpringQueryMap QRCodeParam param);

    /**
     * 获取小程序码(无限制)
     *
     * @param param
     * @return
     */
    @GetMapping("/wechat/miniapp/qrCode/getUnlimitQrCode")
    APIResponse<QRCodeInfo> getMiniAppUnLimitQrCode(@SpringQueryMap UnlimitedQRCodeParam param);

    /**
     * 获取小程序Url Link
     *
     * @param param
     * @return
     */
    @GetMapping("/wechat/miniapp/qrCode/getUrlLink")
    APIResponse<UrlLinkInfo> getMiniAppUrlLink(@SpringQueryMap UrlLinkParam param);

    /**
     * 获取小程序scheme
     *
     * @param param
     * @return
     */
    @GetMapping("/wechat/miniapp/qrCode/getScheme")
    APIResponse<SchemeCodeInfo> getMiniAppScheme(@SpringQueryMap SchemeCodeParam param);

}
