/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.*;

import java.util.Map;

/**
 * 微应用登录服务接口
 *
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
public interface IMicroLoginService {

    /**
     * 生成邀请码
     */
    WxMiniAppInviteCodeResult genInviteCode(Map<String, String> headerMap, WxMiniAppInviteCodeParam param); 

    /**
     * 解析邀请码
     */
    InviteCodeInfoResult parseInviteCode(String inviteCode); 

    /**
     * 通过phoneCode换取微信真实手机号
     */
    String phoneCodeToPhoneNumber(Map<String, String> headerMap, String phoneCode); 


}
