/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatminiapp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
public interface IMiniAppUserInfoService {

    /**
     * 获取手机号
     * @param param
     * @return
     */
    APIResponse<UserPhoneInfo> getPhoneNumber(UserPhoneNumberParam param);

}
