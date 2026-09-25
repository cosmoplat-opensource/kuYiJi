/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.factory;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppQrCodeService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@Slf4j
public class RemoteWxMiniAppQrCodeFallbackFactory implements FallbackFactory<RemoteWxMiniAppQrCodeService> {
    @Override
    public RemoteWxMiniAppQrCodeService create(Throwable throwable) {
        return new RemoteWxMiniAppQrCodeService() {
            @Override
            public APIResponse<QRCodeInfo> getMiniAppQrCode(QRCodeParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序获取小程序码接口失败：" + throwable, 500);
            }

            @Override
            public APIResponse<QRCodeInfo> getMiniAppUnLimitQrCode(UnlimitedQRCodeParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序获取小程序码（无限制）接口失败：" + throwable, 500);
            }

            @Override
            public APIResponse<UrlLinkInfo> getMiniAppUrlLink(UrlLinkParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序Url Link Feign接口失败：" + throwable, 500);
            }

            @Override
            public APIResponse<SchemeCodeInfo> getMiniAppScheme(SchemeCodeParam param) {
                return APIResponse.fail("调用远程服务，调用微信小程序Scheme Feign接口失败：" + throwable, 500);
            }
        };
    }
}
