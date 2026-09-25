/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.QRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.SchemeCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UrlLinkParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.SchemeCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UrlLinkInfo;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
public interface IMiniAppQRCodeService {

    /**
     * 获取小程序码
     * @param param
     * @return
     */
    APIResponse<QRCodeInfo> getMiniAppQrCode(QRCodeParam param);

    /**
     * 获取小程序码（无限制）
     * @param param
     * @return
     */
    APIResponse<QRCodeInfo> getMiniAppUnLimitQrCode(UnlimitedQRCodeParam param);

    /**
     * 获取微信小程序Url Link
     * @param param
     * @return
     */
    APIResponse<UrlLinkInfo> getMiniAppUrlLink(UrlLinkParam param);

    /**
     * 获取微信小程序scheme
     * @param param
     * @return
     */
    APIResponse<SchemeCodeInfo> getMiniAppScheme(SchemeCodeParam param);

}
