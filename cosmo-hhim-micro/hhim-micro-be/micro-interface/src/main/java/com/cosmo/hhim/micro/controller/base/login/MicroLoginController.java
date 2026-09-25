/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.login;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.redis.distributedlock.annotation.SubmitLock;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.base.domain.entity.common.InviteCodeInfoResult;
import com.cosmo.hhim.micro.base.domain.entity.common.SmsLoginParam;
import com.cosmo.hhim.micro.base.domain.entity.common.UserBaseInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppInviteCodeParam;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppInviteCodeResult;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppLoginParam;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppRegisterParam;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAuthService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroLoginService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import com.cosmo.hhim.micro.infrastructure.util.RequestUtils;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

import static com.cosmo.hhim.micro.infrastructure.util.RequestUtils.extractRequestHeader;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Slf4j
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class MicroLoginController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroLoginService microLoginService;
    // decouple-from-ops-platform-cleanup (C.1.4): 新登录链路 
    private final IMicroAuthService microAuthService;

    @Deprecated
    @GetMapping("/mobileapp/token")
    public AjaxResult mobileAppToken(@RequestParam String uucToken, HttpServletRequest request) {
        log.warn("[deprecated] /login/mobileapp/token 已下线；请走微信小程序登录 V2");
        return AjaxResult.error("该接口已下线，请使用微信小程序登录");
    }

    /**
     * 微信小程序登录
     * 注意：白名单
     * 调用时机：token不存在 或 token过期
     *
     * @return
     */
    @PostMapping("/wxminiapp/login")
    public AjaxResult wxMiniAppLogin(HttpServletRequest request, HttpServletResponse response,
                                     @Valid @RequestBody WxMiniAppLoginParam param) {
        log.info("微信小程序---登录>>> param:{}", JSON.toJSONString(param));
        param.checkParam();
        UserBaseInfo userBaseInfo = microAuthService.wxMiniAppLoginV2(extractRequestHeader(request), param);
        log.info("微信小程序---登录>>> result:{}", JSON.toJSONString(userBaseInfo));
        return AjaxResult.success(userBaseInfo);
    }

    /**
     * 微信小程序注册
     * 注意：白名单
     * 调用时机：小程序登录接口返回的token为空
     *
     * @return
     */
    @Deprecated
    @PostMapping("/wxminiapp/register")
    public AjaxResult wxMiniAppRegister(HttpServletRequest request, HttpServletResponse response,
                                        @Valid @RequestBody WxMiniAppRegisterParam param) {
        log.warn("[deprecated] /login/wxminiapp/register 已下线；请走自主注册 /login/register-tenant");
        return AjaxResult.error("该接口已下线，请使用自助注册");
    }

    /**
     * 生成邀请码
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE,
            MicroBusinessConstants.WORKER_ROLE, MicroBusinessConstants.QUALITY_INSPECTOR})
    @PostMapping("/wxminiapp/invitecode")
    public AjaxResult genInviteCode(HttpServletRequest request, @Valid @RequestBody WxMiniAppInviteCodeParam param) {
        log.info("微信小程序---生成邀请码>>> param:{}", JSON.toJSONString(param));
        // 针对app需要调用微信小程序接口生成菊花码，故此接口重置请求头中的type为小程序的type
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put(CacheConstants.DETAILS_TYPE, CommonConstant.DeviceType.WXMINIAPP.getKey());
        RequestUtils.addRequestHeaders(headerMap);

        WxMiniAppInviteCodeResult result = microLoginService.genInviteCode(extractRequestHeader(request), param);
        log.info("微信小程序---生成邀请码>>> result:{}", JSON.toJSONString(result));
        return AjaxResult.success(result);
    }

    /**
     * 解析邀请码
     *
     * @param inviteCode
     * @return
     */
    @GetMapping("/wxminiapp/parseinvitecode")
    public AjaxResult parseInviteCode(@RequestParam("inviteCode") String inviteCode) {
        log.info("微信小程序---解析邀请码>>> param:{}", inviteCode);
        InviteCodeInfoResult result = microLoginService.parseInviteCode(inviteCode);
        log.info("微信小程序---解析邀请码>>> result:{}", JSON.toJSONString(result));
        return AjaxResult.success(result);
    }

    /**
     * 通过phoneCode换取微信真实手机号
     * 注意：白名单
     *
     * @param phoneCode
     * @return
     */
    @GetMapping("/codeToPhone")
    public AjaxResult phoneCodeToPhoneNumber(HttpServletRequest request, @RequestParam("phoneCode") String phoneCode) {
        return AjaxResult.success(microLoginService.phoneCodeToPhoneNumber(extractRequestHeader(request), phoneCode));
    }


    // ============================================================
    //  短信验证码登录
    // ============================================================

    /**
     * 发送短信验证码（验证码登录）
     * 注意：白名单
     *
     * @param param 包含 phoneNumber
     * @return
     */
    @PostMapping("/sms/sendCode")
    public AjaxResult sendSmsLoginCode(@RequestBody Map<String, String> param) {
        String phoneNumber = param.get("phoneNumber");
        log.info("短信验证码---发送>>> phoneNumber:{}", phoneNumber);
        microAuthService.sendSmsLoginCode(phoneNumber);
        log.info("短信验证码---发送成功");
        return AjaxResult.success();
    }

    /**
     * 短信验证码登录
     * 注意：白名单
     * 调用时机：用户输入手机号+验证码登录
     *
     * @param param 包含 phoneNumber + smsCode + wxCode
     * @return
     */
    @PostMapping("/sms/login")
    public AjaxResult smsLogin(HttpServletRequest request,
                               @Valid @RequestBody SmsLoginParam param) {
        log.info("短信验证码---登录>>> phoneNumber:{}", param.getPhoneNumber());
        UserBaseInfo userBaseInfo = microAuthService.smsLogin(extractRequestHeader(request), param);
        log.info("短信验证码---登录>>> result:{}", JSON.toJSONString(userBaseInfo));
        return AjaxResult.success(userBaseInfo);
    }

}
