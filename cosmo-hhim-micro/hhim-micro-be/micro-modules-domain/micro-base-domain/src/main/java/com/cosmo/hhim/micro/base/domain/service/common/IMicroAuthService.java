/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common;

import com.cosmo.hhim.micro.base.domain.entity.common.JoinTenantByInviteParam;
import com.cosmo.hhim.micro.base.domain.entity.common.RegisterTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SelectTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SmsLoginParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SwitchTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.UserBaseInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppLoginParam;

import java.util.Map;

/**
 * decouple-from-ops-platform：本地化认证流程（多租户选择 + 自助注册开租户）。
 *
 * 旧 {@link IMicroLoginService} 强依赖 im-api-operation，本接口为新增入口，
 * 用于替换登录链路中的远程调用。
 *
 * @author cosmo-hhim-open Team
 */
public interface IMicroAuthService {

    /**
     * 多租户选择：用 tempToken + 选定 tenantCode 换正式 token
     */
    UserBaseInfo selectTenant(SelectTenantParam param);

    /**
     * 自助注册开租户
     *
     * @param param 注册参数（sessionId 0-租户流程必填，已登录用户可选）
     * @param headerMap 请求头（已登录用户路径用，未登录传 null 走 sessionId 0-租户流程）
     */
    UserBaseInfo registerTenant(RegisterTenantParam param, Map<String, String> headerMap);

    /**
     * decouple-from-ops-platform-cleanup (C.17): 用邀请码加入现有租户
     *
     * <p>安全设计：手机号优先从 session 读（不收 body），与 register-tenant 一致。
     * 邀请码对应租户的 MicroInviteCodeInfo 仍然走 Redis（沿用旧 genInviteCode 写入的 key）。
     *
     * @param param 加入参数（sessionId 0-租户流程必填，已登录用户可选）
     * @param headerMap 请求头（已登录用户路径用，未登录传 null 走 sessionId 0-租户流程）
     */
    UserBaseInfo joinTenantByInvite(JoinTenantByInviteParam param, Map<String, String> headerMap);

    /**
     * 已登录用户切换租户：手动校验 token → 设目标租户 ThreadContext → 签新 token
     *
     * <p>不走 @AccessAuth，避免 AccessAuthFilter 预设 ThreadContext 导致
     * SchemaIntercept 读到当前租户而非目标租户。
     */
    UserBaseInfo switchTenant(Map<String, String> headerMap, SwitchTenantParam param);

    /**
     * decouple-from-ops-platform-cleanup (C.1.3): 接管 MicroLoginServiceImpl.wxMiniAppLogin 主链路
     *
     * - 强校验 phoneCode 必填
     * - 不调 im-api-operation（RemoteCustomerService / RemoteMicroApplicationConfigService）
     * - 多租户：返回 multiTenant=true + tempToken + tenantList（前端弹 picker）
     * - 单租户：直接 issueTokenAndBuildResult
     * - 0 租户：抛 NO_TENANT(403)
     */
    UserBaseInfo wxMiniAppLoginV2(Map<String, String> headerMap, WxMiniAppLoginParam param);

    /**
     * 发送短信验证码（用于验证码登录）
     *
     * @param phoneNumber 手机号
     */
    void sendSmsLoginCode(String phoneNumber);

    /**
     * 短信验证码登录
     *
     * @param headerMap 请求头
     * @param param 短信登录参数（含 phoneNumber + smsCode + wxCode）
     * @return 用户基础信息
     */
    UserBaseInfo smsLogin(Map<String, String> headerMap, SmsLoginParam param);
}
