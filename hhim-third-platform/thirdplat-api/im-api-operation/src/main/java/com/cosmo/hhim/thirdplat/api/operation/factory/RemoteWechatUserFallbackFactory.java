/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.factory;

import com.cosmo.hhim.thirdplat.api.operation.RemoteWechatUserService;
import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
public class RemoteWechatUserFallbackFactory implements FallbackFactory<RemoteWechatUserService> {
    @Override
    public RemoteWechatUserService create(Throwable throwable) {
        return new RemoteWechatUserService() {
            @Override
            public APIResponse<Boolean> addWechatUserInfo(WechatUserInfoParam param) {
                return APIResponse.fail("调用远程服务，添加微信用户(存在则更新)失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> batchAddWechatUserInfo(List<WechatUserInfoParam> paramList) {
                return APIResponse.fail("调用远程服务，批量添加微信用户(存在则更新)失败：" + throwable, 500);
            }

            @Override
            public APIResponse<WechatUserInfoResult> queryWechatUserInfoByOpenId(String openid) {
                return APIResponse.fail("调用远程服务，根据openid查询微信用户以及所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzWechatUser> queryWxUserBasicInfoByOpenId(String openid) {
                return APIResponse.fail("调用远程服务，根据openid查询微信用户基本信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByOpenId(String openid) {
                return APIResponse.fail("调用远程服务，根据openid查询所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByOpenIdAndTenant(String openid) {
                return APIResponse.fail("调用远程服务，根据openid和租户编码查询所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzWechatUserBind> queryWechatUserBindInfo(WechatUserDetailInfoParam param) {
                return APIResponse.fail("调用远程服务，根据openid+username+userIdentify查询微信用户以及所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(List<String> usernames) {
                return APIResponse.fail("调用远程服务，根据用户账号和租户编码查询微信用户以及所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> queryAllTenantWechatUserBindInfoByUsernames(List<String> usernames) {
                return APIResponse.fail("调用远程服务，根据用户账号查询微信用户以及所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByCondition(String username, String userIdentity) {
                return APIResponse.fail("调用远程服务，根据用户账号+身份查询微信用户以及所绑定的账户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<HyzzWechatUser> updateLoginTime(String openid) {
                return APIResponse.fail("调用远程服务，更新最新登录的时间（返回上次登录的时间）失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> unbindWechatUser(WechatUnbindParam param) {
                return APIResponse.fail("调用远程服务，解绑微信用户与账号的绑定关系失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> unbindTenantWechatUser(WechatUnbindTenantParam param) {
                return APIResponse.fail("调用远程服务，根据租户解绑微信用户与账号的绑定关系失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> unbindByUserName(String username) {
                return APIResponse.fail("调用远程服务，解绑指定用户账号的微信绑定信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> updateWechatUserBindInfo(WechatUserBindUpdateParam param) {
                return APIResponse.fail("调用远程服务，更新微信用户与账号的绑定信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<String> getTenantByAppId(String appId) {
                return APIResponse.fail("调用远程服务，根据微信公众号AppId查询租户信息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<Boolean> updateWechatUserOpenId(WechatUserOpenIdUpdateParam param) {
                return APIResponse.fail("调用远程服务，更新微信用户及账号绑定信息openid失败：" + throwable, 500);
            }

            @Override
            public APIResponse<List<HyzzWechatUserBind>> selectHyzzWechatUserBindList(HyzzWechatUserBind hyzzWechatUserBind) {
                return APIResponse.fail("调用远程服务，查询微信与企业用户绑定信息列表失败：" + throwable, 500);
            }


        };
    }
}
