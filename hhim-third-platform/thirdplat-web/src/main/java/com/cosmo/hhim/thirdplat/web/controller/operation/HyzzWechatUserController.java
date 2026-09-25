/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.controller.operation;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.thirdplat.api.operation.domain.*;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.service.operation.IHyzzWechatUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.validation.Valid;
import java.util.List;

/**
 * 微信用户信息Controller
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/operation/wechatuser")
public class HyzzWechatUserController extends BaseController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    @Autowired
    private IHyzzWechatUserService hyzzWechatUserService;

    /**
     * 添加微信用户(存在则更新)
     *
     * @param param
     * @return
     */
    @PostMapping("/add")
    public APIResponse<Boolean> addWechatUserInfo(@Valid @RequestBody WechatUserInfoParam param) {
        return hyzzWechatUserService.addWechatUserInfo(param);
    }

    /**
     * 批量添加微信用户(存在则更新)
     *
     * @param paramList
     * @return
     */
    @PostMapping("/batchAdd")
    public APIResponse<Boolean> batchAddWechatUserInfo(@RequestBody List<WechatUserInfoParam> paramList) {
        return hyzzWechatUserService.batchAddWechatUserInfo(paramList);
    }


    /**
     * 根据openid查询微信用户以及所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/queryUser")
    public APIResponse<WechatUserInfoResult> queryWechatUserInfoByOpenId(String openid) {
        return hyzzWechatUserService.queryWechatUserInfoByOpenId(openid);
    }

    /**
     * 根据openid查询所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/queryBindUsers")
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfosByOpenId(String openid) {
        return hyzzWechatUserService.queryWxBindUserByOpenId(openid);
    }

    /**
     * 根据openid和租户编码查询所绑定的账户信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/queryTenantBindUsers")
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfosByOpenIdAndTenant(String openid) {
        return hyzzWechatUserService.queryWxBindUserByOpenIdAndTenant(openid);
    }

    /**
     * 根据openid查询微信用户基本信息
     *
     * @param openid
     * @return
     */
    @GetMapping("/queryWxUserBasicInfo")
    public APIResponse<HyzzWechatUser> queryWxUserBasicInfoByOpenId(String openid) {
        return hyzzWechatUserService.queryWxUserBasicInfoByOpenId(openid);
    }


    /**
     * 根据用户账号或手机号查询所绑定的微信信息
     *（当前租户下查询）
     * @param
     * @return
     */
    @PostMapping("/queryUserBindInfo")
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(@RequestBody List<String> usernames) {
        return hyzzWechatUserService.queryWechatUserBindInfoByUsernames(usernames);
    }

    /**
     * 根据用户账号或手机号查询所绑定的微信信息
     *（所有租户下查询）
     * @param
     * @return
     */
    @PostMapping("/queryAllTenantUserBindInfo")
    public APIResponse<List<HyzzWechatUserBind>> queryAllTenantWechatUserBindInfoByUsernames(@RequestBody List<String> usernames) {
        return hyzzWechatUserService.queryAllTenantWechatUserBindInfoByUsernames(usernames);
    }

    /**
     * 根据用户账号或手机号 + 身份查询所绑定的微信信息
     *
     * @param
     * @return
     */
    @GetMapping("/queryUserBindInfoByCondition")
    public APIResponse<List<HyzzWechatUserBind>> queryWechatUserBindInfoByUsernames(@RequestParam("username") String username, @RequestParam("userIdentity") String userIdentity) {
        return hyzzWechatUserService.queryWechatUserBindInfoByCondition(username, userIdentity);
    }

    /**
     * 根据openid + username + userIdentify查询微信绑定的用户信息
     *
     * @param param
     * @return
     */
    @GetMapping("/queryBindUser")
    public APIResponse<HyzzWechatUserBind> queryWechatUserBindInfo(@Valid WechatUserDetailInfoParam param) {
        return hyzzWechatUserService.queryWechatUserBindInfo(param);
    }


    /**
     * 更新最新登录的时间（返回上次登录的时间）
     *
     * @return
     */
    @PutMapping("/updateLoginTime")
    public APIResponse<HyzzWechatUser> updateLoginTime(String openid) {
        return hyzzWechatUserService.updateLoginTime(openid);
    }

    /**
     * 解绑微信用户与账号的绑定关系
     *
     * @param param
     * @return
     */
    @DeleteMapping("/unbind")
    public APIResponse<Boolean> unbindWechatUser(@Valid @RequestBody WechatUnbindParam param) {
        return hyzzWechatUserService.unbindWechatUser(param);
    }

    /**
     * 解绑微信用户与账号的绑定关系
     *
     * @param param
     * @return
     */
    @DeleteMapping("/tenant/unbind")
    public APIResponse<Boolean> unbindTenantWechatUser(@Valid @RequestBody WechatUnbindTenantParam param) {
        return hyzzWechatUserService.unbindTenantWechatUser(param);
    }

    /**
     * 解绑微信用户与账号的绑定关系
     *
     * @param username
     * @return
     */
    @DeleteMapping("/unbindByUserName")
    public APIResponse<Boolean> unbindWechatUserByUserName(@RequestParam("username") String username) {
        return hyzzWechatUserService.unbindWechatUserByUserName(username);
    }

    /**
     * 更新微信用户与账号的绑定信息
     *
     * @param param
     * @return
     */
    @PutMapping("/updateWechatUserBindInfo")
    public APIResponse<Boolean> updateWechatUserBindInfo(@Valid @RequestBody WechatUserBindUpdateParam param) {
        return hyzzWechatUserService.updateWechatUserBindInfo(param);
    }

    /**
     * 根据微信公众号AppId查询租户信息
     *
     * @param appId
     * @return
     */
    @GetMapping("/getTenantByAppId")
    public APIResponse<String> getTenantByAppId(String appId) {
        return hyzzWechatUserService.getTenantByAppId(appId);
    }

    /**
     * 更新微信用户及其绑定关系openid
     * @param param
     * @return
     */
    @PutMapping("/updateWechatUserOpenId")
    public APIResponse<Boolean> updateWechatUserOpenId(@RequestBody WechatUserOpenIdUpdateParam param){
        logger.info("更新微信用户及其绑定关系openid请求参数:{}", JSON.toJSONString(param));
        return hyzzWechatUserService.updateWechatUserOpenId(param);
    }

    /**
     * 查询微信与企业用户绑定信息列表
     *
     * @param
     * @return
     */
    @GetMapping("/selectHyzzWechatUserBindList")
    public APIResponse<List<HyzzWechatUserBind>> selectHyzzWechatUserBindList(HyzzWechatUserBind hyzzWechatUserBind) {
        return hyzzWechatUserService.selectHyzzWechatUserBindList(hyzzWechatUserBind);
    }

}
