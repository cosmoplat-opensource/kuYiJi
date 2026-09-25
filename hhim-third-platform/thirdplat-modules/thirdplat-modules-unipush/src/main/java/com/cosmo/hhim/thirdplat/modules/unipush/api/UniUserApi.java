/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.api;

import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.user.BadgeParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.user.DeviceStatusResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.user.UserInfoResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.user.UserStatusResult;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.TokenHeaderInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 用户API
 * @createTime 2021-09-22
 */
@BaseRequest(
        baseURL = "${uniBaseUrl}${uniAppId}",
        headers = {
                "Accept-Charset: ${uniEncoding}",
                "Content-Type: ${uniContentType}"
        },
        interceptor = TokenHeaderInterceptor.class
)
@UniPushResponseException
public interface UniUserApi {

    /**
     * 【用户】查询用户状态
     *
     * @param cIds
     * @return
     */
    @Get(url = "/user/status/${cids}")
    ForestResponse<UniPushResponseResult<Map<String, UserStatusResult>>> queryUserStatus(@Var("cids") String cIds);

    /**
     * 【用户】查询设备状态
     *
     * @param cIds
     * @return
     */
    @Get(url = "/user/deviceStatus/${cids}")
    ForestResponse<UniPushResponseResult<Map<String, DeviceStatusResult>>> queryDeviceStatus(@Var("cids") String cIds);


    /**
     * 【用户】查询用户信息
     *
     * @param cIds
     * @return
     */
    @Get(url = "/user/detail/${cids}")
    ForestResponse<UniPushResponseResult<UserInfoResult>> queryUserInfo(@Var("cids") String cIds);

    /**
     * 【用户】设置角标(仅支持IOS)
     * 通过cid通知个推服务器当前iOS设备的角标情况。
     *
     * @param cids
     * @param badgeParam
     * @return
     */
    @Post(url = "/user/badge/cid/${cids}")
    ForestResponse<UniPushResponseResult<String>> setIosBadge(@Var("cids") String cids, @JSONBody BadgeParam badgeParam);
}
