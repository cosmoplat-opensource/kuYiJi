/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
public interface IMiniAppBaseService {

    /**
     * 微信小程序登录
     * @param jsCode
     * @return
     */
    APIResponse<Code2SessionInfo> wxMiniAppLoginIn(String jsCode);

}
