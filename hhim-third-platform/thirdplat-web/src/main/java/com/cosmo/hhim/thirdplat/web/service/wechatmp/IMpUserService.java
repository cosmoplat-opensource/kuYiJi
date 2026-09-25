/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.out.WxMpUserInfoResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
public interface IMpUserService {

    /**
     * 查询微信公众号粉丝基本信息
     * @param openid
     * @return
     */
    APIResponse<WxMpUserInfoResult> getMpUserInfo(String openid);

}
