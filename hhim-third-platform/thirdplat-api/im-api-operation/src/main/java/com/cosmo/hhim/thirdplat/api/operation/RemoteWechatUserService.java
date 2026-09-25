/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation;

import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.api.operation.factory.RemoteWechatUserFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
@FeignClient(contextId = "RemoteWechatUserService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemoteWechatUserFallbackFactory.class)
public interface RemoteWechatUserService {
    /**
     * 添加微信用户(存在则更新)
     *
     * @param param
     * @return
     */
    @PostMapping("/operation/wechatuser/add")
    APIResponse<Boolean> addWechatUserInfo(WechatUserInfoParam param);

    /**
     * 添加微信用户(存在则更新)
     *
     * @param paramList
     * @return
     */
    @PostMapping("/operation/wechatuser/batchAdd")
    APIResponse<Boolean> batchAddWechatUserInfo(List<WechatUserInfoParam> paramList);

    /**
     * 根据openid查询微信用户以及所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/operation/wechatuser/queryUser")
    APIResponse<WechatUserInfoResult> queryWechatUserInfoByOpenId(@RequestParam("openid") String openid);

    /**
     * 根据openid查询微信用户基本信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/operation/wechatuser/queryWxUserBasicInfo")
    APIResponse<HyzzWechatUser> queryWxUserBasicInfoByOpenId(@RequestParam("openid") String openid);

    /**
     * 根据openid查询所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/operation/wechatuser/queryBindUsers")
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByOpenId(@RequestParam("openid") String openid);

    /**
     * 根据openid和租户编码查询所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/operation/wechatuser/queryTenantBindUsers")
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByOpenIdAndTenant(@RequestParam("openid") String openid);


    /**
     * 根据openid + username + userIdentify查询微信绑定的用户信息
     *
     * @param param
     * @return
     */
    @GetMapping("/operation/wechatuser/queryBindUser")
    APIResponse<HyzzWechatUserBind> queryWechatUserBindInfo(@SpringQueryMap WechatUserDetailInfoParam param);

    /**
     * 根据用户账号查询所绑定的微信信息
     *（当前租户下查询）
     * @param
     * @return
     */
    @PostMapping("/operation/wechatuser/queryUserBindInfo")
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(List<String> usernames);

    /**
     * 根据用户账号查询所绑定的微信信息
     *（所有租户下查询）
     * @param
     * @return
     */
    @PostMapping("/operation/wechatuser/queryAllTenantUserBindInfo")
    APIResponse<List<HyzzWechatUserBind>> queryAllTenantWechatUserBindInfoByUsernames(List<String> usernames);


    /**
     * 根据用户账号+身份查询所绑定的微信信息
     *
     * @param
     * @return
     */
    @GetMapping("/operation/wechatuser/queryUserBindInfoByCondition")
    APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByCondition(@RequestParam("username") String username, @RequestParam("userIdentity") String userIdentity);

    /**
     * 更新最新登录的时间（返回上次登录的时间）
     *
     * @return
     */
    @PutMapping("/operation/wechatuser/updateLoginTime")
    APIResponse<HyzzWechatUser> updateLoginTime(@RequestParam("openid") String openid);

    /**
     * 解绑微信用户与账号的绑定关系
     *
     * @param param
     * @return
     */
    @DeleteMapping("/operation/wechatuser/unbind")
    APIResponse<Boolean> unbindWechatUser(WechatUnbindParam param);

    /**
     * 根据租户解绑微信用户与账号的绑定关系
     *
     * @param param
     * @return
     */
    @DeleteMapping("/operation/wechatuser/tenant/unbind")
    APIResponse<Boolean> unbindTenantWechatUser(WechatUnbindTenantParam param);

    /**
     * 解绑指定用户账号的微信绑定信息
     * @param username
     * @return
     */
    @DeleteMapping("/operation/wechatuser/unbindByUserName")
    APIResponse<Boolean> unbindByUserName(@RequestParam("username") String username);

    /**
     * 更新微信用户与账号的绑定信息
     *
     * @param param
     * @return
     */
    @PutMapping("/operation/wechatuser/updateWechatUserBindInfo")
    APIResponse<Boolean> updateWechatUserBindInfo(WechatUserBindUpdateParam param);

    /**
     * 根据微信公众号AppId查询租户信息
     * @param appId
     * @return
     */
    @GetMapping("/operation/wechatuser/getTenantByAppId")
    APIResponse<String> getTenantByAppId(@RequestParam("appId") String appId);

    /**
     * 根据旧openId更新微信用户及用户绑定关系得openId
     * @param param
     * @return
     */
    @PutMapping("/operation/wechatuser/updateWechatUserOpenId")
    APIResponse<Boolean> updateWechatUserOpenId(WechatUserOpenIdUpdateParam param);

    /**
     * 查询微信与企业用户绑定信息列表
     * @param hyzzWechatUserBind
     * @return
     */
    @GetMapping("/operation/wechatuser/selectHyzzWechatUserBindList")
    APIResponse<List<HyzzWechatUserBind>> selectHyzzWechatUserBindList(@SpringQueryMap HyzzWechatUserBind hyzzWechatUserBind);
}
