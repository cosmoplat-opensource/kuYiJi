/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;

import java.util.List;

/**
 * 微信用户信息Service接口
 *
 * @author cosmo-hhim-open Team
 */
public interface IHyzzWechatUserService {

    /**
     * 添加微信用户信息
     *
     * @param param
     * @return
     */
    APIResponse<Boolean> addWechatUserInfo(WechatUserInfoParam param);

    /**
     * 批量添加微信用户信息
     *
     * @param paramList
     * @return
     */
    APIResponse<Boolean> batchAddWechatUserInfo(List<WechatUserInfoParam> paramList);

    /**
     * 根据openid查询微信用户以及所绑定的账户信息
     *
     * @param openid
     * @return
     */
    APIResponse<WechatUserInfoResult> queryWechatUserInfoByOpenId(String openid);

    /**
     * 根据openid和租户编码查询所绑定的用户信息
     *
     * @param openid
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> queryWxBindUserByOpenIdAndTenant(String openid);

    /**
     * 根据openid查询所绑定的用户信息
     *
     * @param openid
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> queryWxBindUserByOpenId(String openid);

    /**
     * 根据openid查询微信用户基本信息
     *
     * @param openid
     * @return
     */
    APIResponse<HyzzWechatUser> queryWxUserBasicInfoByOpenId(String openid);

    /**
     * 根据openid + username + userIdentify + tenantCode查询所绑定的用户信息
     *
     * @return
     */
    APIResponse<HyzzWechatUserBind> queryWechatUserBindInfo(WechatUserDetailInfoParam param);

    /**
     * 根据用户账号查询所绑定的微信信息
     *
     * @param usernames
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(List<String> usernames);

    /**
     * 根据用户账号查询所绑定的微信信息（所有租户下查询）
     * @param usernames
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> queryAllTenantWechatUserBindInfoByUsernames(List<String> usernames);

    /**
     * 根据用户账号 + 身份查询所绑定的微信信息
     *
     * @param username
     * @param userIdentity
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByCondition(String username, String userIdentity);

    /**
     * 根据openId和账户信息解绑微信和账号绑定关系
     *
     * @param param
     * @return
     */
    APIResponse<Boolean> unbindWechatUser(WechatUnbindParam param);

    /**
     * 根据openId和账户信息以及租户对微信账号信息解绑
     * @param param
     * @return
     */
    APIResponse<Boolean> unbindTenantWechatUser(WechatUnbindTenantParam param);

    /**
     * 根据指定账号的微信绑定信息
     *
     * @param username
     * @return
     */
    APIResponse<Boolean> unbindWechatUserByUserName(String username);

    /**
     * 更新最新登录的时间（返回上次登录的时间）
     *
     * @return
     */
    APIResponse<HyzzWechatUser> updateLoginTime(String openid);

    /**
     * 更新微信用户绑定信息
     *
     * @param param
     * @return
     */
    APIResponse<Boolean> updateWechatUserBindInfo(WechatUserBindUpdateParam param);

    /**
     * 根据微信公众号AppId查询租户信息
     * @param appId
     * @return
     */
    APIResponse<String> getTenantByAppId(String appId);

    /**
     * 更新微信用户OpenId信息
     *
     * @param param
     * @return
     */
    APIResponse<Boolean> updateWechatUserOpenId(WechatUserOpenIdUpdateParam param);

    /**
     * 查询微信与企业用户绑定信息列表
     * @param hyzzWechatUserBind
     * @return
     */
    APIResponse<List<HyzzWechatUserBind>> selectHyzzWechatUserBindList(HyzzWechatUserBind hyzzWechatUserBind);
}
