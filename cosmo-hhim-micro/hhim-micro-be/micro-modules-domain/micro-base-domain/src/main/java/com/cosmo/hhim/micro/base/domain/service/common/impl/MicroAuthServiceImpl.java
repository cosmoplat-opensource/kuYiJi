/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.common.core.exception.CustomException;

import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.core.utils.utils.DBControlUtil;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.micro.base.domain.entity.common.JoinTenantByInviteParam;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroInviteCodeInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroTenant;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUser;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserCompleteInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserPlatformRe;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserRoleRe;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserTenantIndex;
import com.cosmo.hhim.micro.base.domain.entity.common.RegisterTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SelectTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SmsLoginParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SwitchTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.UserBaseInfo;
import com.cosmo.hhim.micro.base.domain.entity.common.WxMiniAppLoginParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.RemoteSmsService;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.micro.base.domain.service.common.IAppConfigService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAuthService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.base.domain.service.common.IUserTenantIndexService;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.ActiveFlagEnum;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppBaseService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppUserInfoService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.Code2SessionInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * decouple-from-ops-platform：本地化认证流程实现。
 * 完全不依赖 im-api-operation。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class MicroAuthServiceImpl implements IMicroAuthService {

    /** 万能验证码已下线（decouple-from-ops-platform-cleanup）：删除通用 magic code 通道，仅保留 Redis 校验 */
    // private static final String UNIVERSAL_SMS_CODE = "123456";

    /** 临时 token 有效期：5 分钟（足够 picker 选租户用） */
    private static final long TEMP_TOKEN_TTL_SECONDS = 300L;

    /** 短信验证码有效期：5 分钟 */
    private static final long SMS_CODE_TTL_SECONDS = 300L;

    /** 短信发送间隔：60 秒 */
    private static final long SMS_SEND_INTERVAL_SECONDS = 60L;

    /** tenantCode 字符池（避免 0/O/1/l 等易混字符） */
    private static final String TENANT_CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IMicroTenantService microTenantService;

    @Autowired
    private IUserTenantIndexService userTenantIndexService;

    @Autowired
    private IMicroUserService microUserService;

    @Autowired
    private IAppConfigService appConfigService;

    @Autowired
    private RemoteWxMiniAppBaseService remoteWxMiniAppBaseService;

    @Autowired
    private RemoteWxMiniAppUserInfoService remoteWxMiniAppUserInfoService;

    @Autowired
    private RemoteSmsService remoteSmsService;

    @Value("${settings.tokenEffectiveTime:604800}")
    private Integer tokenEffectiveTime;

    @Value("${sms.loginVerificationCodeTempleteId:10759705}")
    private String loginVerificationCodeTempleteId;

    // ============================================================
    //  多租户选择
    // ============================================================

    @Override
    public UserBaseInfo selectTenant(SelectTenantParam param) {
        if (param == null || !StringUtils.hasText(param.getTempToken()) || !StringUtils.hasText(param.getTenantCode())) {
            throw new CustomException("参数不合法");
        }

        // 1. 校验 tempToken
        String tempKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.TEMP_TOKEN + param.getTempToken());
        String phone = redisCache.getCacheObject(tempKey);
        if (!StringUtils.hasText(phone)) {
            throw new CustomException("TEMP_TOKEN_INVALID", 401);
        }
        // 删除 tempToken（一次性）
        redisCache.deleteObject(tempKey);

        // 2. 校验 (phone, tenantCode) 关联
        MicroUserTenantIndex idx = userTenantIndexService.getByPhoneAndCode(phone, param.getTenantCode());
        if (idx == null) {
            throw new CustomException("TENANT_NOT_LINKED", 403);
        }

        // 3. 设 thread context（单库场景下 schema/datasource 写死）
        DBControlUtil.setDbAndSchema("db0", "im_micro", param.getTenantCode());
        ThreadContext.put(Constants.TARGET_CUSTOMER, param.getTenantCode());
        ThreadContext.put(Constants.TARGET_DS, "db0");
        ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

        // 4. 查 micro_user
        MicroUser microUser = microUserService.findMicroUserCompleteInfo(null, phone);
        if (microUser == null) {
            throw new CustomException("用户不存在！", 404);
        }

        // 5. 发正式 token
        return issueTokenAndBuildResult(phone, param.getTenantCode(), microUser,
                resolveAppSign(), resolveDeviceType());
    }

    // ============================================================
    //  自助注册开租户
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBaseInfo registerTenant(RegisterTenantParam param, Map<String, String> headerMap) {
        if (param == null || !StringUtils.hasText(param.getTenantName())) {
            throw new CustomException("参数不合法：tenantName 必填");
        }

        final String phone;
        String openid = "";
        final String applicationSign;
        final String platformType;

        if (StringUtils.hasText(param.getSessionId())) {
            // === 路径 A：新用户 0-租户流程，从 Redis session 读已验证的手机号 ===
            // session 由 wxMiniAppLogin / smsLogin 在 0 租户场景下写入
            String sessionKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(
                    RedisKeys.WX_LOGIN_VERIFIED + param.getSessionId());
            String sessionJson = redisCache.getCacheObject(sessionKey);
            if (!StringUtils.hasText(sessionJson)) {
                log.warn("register-tenant: sessionId={} 无效或已过期", param.getSessionId());
                throw new CustomException("SESSION_INVALID_OR_EXPIRED：请重新登录", 401);
            }
            WxLoginVerifiedSession session = JSON.parseObject(sessionJson, WxLoginVerifiedSession.class);
            phone = session.phone;
            openid = session.openid;
            applicationSign = StringUtils.hasText(param.getApplicationSign())
                    ? param.getApplicationSign()
                    : session.applicationSign;
            platformType = StringUtils.hasText(param.getPlatformType())
                    ? param.getPlatformType()
                    : session.platformType;
            log.info("register-tenant: 从 session 读出 phone={}, sessionId={}", phone, param.getSessionId());
        } else if (headerMap != null) {
            // === 路径 B：已登录用户，从 Redis token 解析 phone + openid ===
            ResolvedIdentity resolved = resolveIdentityFromToken(headerMap);
            phone = resolved.phone;
            openid = resolved.openid != null ? resolved.openid : "";
            applicationSign = resolved.appSign;
            platformType = resolved.deviceType;
            log.info("register-tenant: 已登录用户 phone={}, openid={}", phone, openid);
        } else {
            throw new CustomException("参数不合法：sessionId 或登录态至少提供一个", 400);
        }

        // 7.1.2 生成 tenantCode（6 位短 ID）
        String tenantCode = generateTenantCode();

        Date now = DateUtils.getNowDate();

        // 7.1.3 INSERT micro_tenant
        MicroTenant tenant = new MicroTenant();
        tenant.setTenantCode(tenantCode);
        tenant.setTenantName(param.getTenantName());
        tenant.setCustomerName(param.getCustomerName());
        tenant.setStatus(1);
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);
        try {
            microTenantService.insert(tenant);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            // 极端情况：生成的 tenantCode 冲突，重试一次
            tenantCode = generateTenantCode();
            tenant.setTenantCode(tenantCode);
            microTenantService.insert(tenant);
        }

        // 设 thread context，让后续 INSERT 自动打 tenant_code
        DBControlUtil.setDbAndSchema("db0", "im_micro", tenantCode);
        ThreadContext.put(Constants.TARGET_CUSTOMER, tenantCode);
        ThreadContext.put(Constants.TARGET_DS, "db0");
        ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

        // 7.1.4 INSERT micro_user（admin 角色）
        MicroUser admin = new MicroUser();
        admin.setTenantCode(tenantCode);
        // decouple C.16: phone 来自 session，不再读 param
        admin.setUserName(phone);
        admin.setPhonenumber(phone);
        // decouple C.18: nickName 兜底 —— 前端 customerName 已必填（uni-forms + JS 双重校验），
        // 这里再退到 phone 防 DB 抛 "Field 'nick_name' doesn't have a default value"
        String adminNick = StringUtils.hasText(param.getCustomerName())
                ? param.getCustomerName().trim()
                : phone;
        admin.setNickName(adminNick);
        admin.setCreatedBy(phone);
        admin.setCreatedDate(now);

        // decouple C.19: 不再依赖 saveIfAbsentMicroUserCompleteInfo 内部行为，
        // 自己在外部显式写入 micro_user + micro_user_role_re，避免"用户无对应角色信息" 500
        Long localUserId = insertMicroUserAndAdminRoleRe(tenantCode, admin,
                RoleCodeEnum.MANAGER.getCode(), RoleCodeEnum.MANAGER.getDesc(), now);

        // 仍调一次 saveIfAbsentMicroUserCompleteInfo 写 micro_user_platform_re（应用关联）
        // —— 它内部对已存在的 user 是 no-op
        microUserService.saveIfAbsentMicroUserCompleteInfo(
                buildAdminCompleteInfo(admin, tenantCode, phone, now, applicationSign, platformType));

        log.info("register-tenant: localUserId={} 角色关联已显式写入", localUserId);

        // 7.1.5 INSERT micro_user_tenant_index
        MicroUserTenantIndex idx = new MicroUserTenantIndex();
        // decouple C.16: phone 来自 session
        idx.setPhone(phone);
        idx.setTenantCode(tenantCode);
        idx.setLocalUserId(localUserId);
        idx.setIsPrimary(1);
        idx.setCreatedAt(now);
        try {
            userTenantIndexService.insert(idx);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            throw new CustomException("TENANT_ALREADY_LINKED", 409);
        }

        // 7.1.6 发正式 token
        // 注册阶段 ThreadContext/SecurityContext 可能未设置，不能走 findMicroUserCompleteInfo 重新查询；
        // 与 joinTenantByInvite 同款处理：DB 已写入角色但 admin 内存对象未填充，
        // 手动补上 microRoles，防止 issueTokenAndBuildResult 把空角色写入 Redis → 注册后直接进系统 911 无权访问
        MicroRole adminRole = new MicroRole();
        adminRole.setRoleCode(RoleCodeEnum.MANAGER.getCode());
        adminRole.setRoleName(RoleCodeEnum.MANAGER.getDesc());
        admin.setMicroRoles(Arrays.asList(adminRole));

        return issueTokenAndBuildResult(phone, tenantCode, admin,
                resolveAppSign(), resolveDeviceType());
    }

    // ============================================================
    //  邀请码加入现有租户（decouple C.17）
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBaseInfo joinTenantByInvite(JoinTenantByInviteParam param, Map<String, String> headerMap) {
        if (param == null
                || !StringUtils.hasText(param.getInviteCode())
                || !StringUtils.hasText(param.getNickName())) {
            throw new CustomException("参数不合法：inviteCode + nickName 必填");
        }

        final String phone;
        String openid = "";
        final String applicationSign;
        final String platformType;

        if (StringUtils.hasText(param.getSessionId())) {
            // === 路径 A：新用户 0-租户流程，从 Redis session 读已验证的手机号 ===
            String sessionKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(
                    RedisKeys.WX_LOGIN_VERIFIED + param.getSessionId());
            String sessionJson = redisCache.getCacheObject(sessionKey);
            if (!StringUtils.hasText(sessionJson)) {
                log.warn("join-tenant: sessionId={} 无效或已过期", param.getSessionId());
                throw new CustomException("SESSION_INVALID_OR_EXPIRED：请重新登录", 401);
            }
            WxLoginVerifiedSession session = JSON.parseObject(sessionJson, WxLoginVerifiedSession.class);
            phone = session.phone;
            openid = session.openid;
            applicationSign = StringUtils.hasText(param.getApplicationSign())
                    ? param.getApplicationSign()
                    : session.applicationSign;
            platformType = StringUtils.hasText(param.getPlatformType())
                    ? param.getPlatformType()
                    : session.platformType;
            log.info("join-tenant: 从 session 读出 phone={}, sessionId={}", phone, param.getSessionId());
        } else if (headerMap != null) {
            // === 路径 B：已登录用户，从 Redis token 解析 phone + openid ===
            ResolvedIdentity resolved = resolveIdentityFromToken(headerMap);
            phone = resolved.phone;
            openid = resolved.openid != null ? resolved.openid : "";
            applicationSign = resolved.appSign;
            platformType = resolved.deviceType;
            log.info("join-tenant: 已登录用户 phone={}, openid={}", phone, openid);
        } else {
            throw new CustomException("参数不合法：sessionId 或登录态至少提供一个", 400);
        }

        // 2. 查邀请码（沿用 MicroLoginServiceImpl.genInviteCode 写入的 Redis key）
        String inviteCodeKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.INVITE_CODE + param.getInviteCode());
        String inviteJson = redisCache.getCacheObject(inviteCodeKey);
        if (!StringUtils.hasText(inviteJson)) {
            log.warn("join-tenant: inviteCode={} 无效或已过期", param.getInviteCode());
            throw new CustomException("INVITE_CODE_INVALID：邀请码无效或已过期", 400);
        }
        MicroInviteCodeInfo invite = JSON.parseObject(inviteJson, MicroInviteCodeInfo.class);
        final String tenantCode = invite.getCustomerCode();
        log.info("join-tenant: sessionId={}, inviteCode={}, tenantCode={}, role={}",
                param.getSessionId(), param.getInviteCode(), tenantCode, invite.getRoleCode());

        // 3. 校验该手机号是否已属于该租户
        MicroUserTenantIndex existing = userTenantIndexService.getByPhoneAndCode(phone, tenantCode);
        if (existing != null) {
            throw new CustomException("TENANT_ALREADY_LINKED：您已属于该租户，请直接登录", 409);
        }

        // 4. 设 thread context（target = 邀请码指向的租户）
        DBControlUtil.setDbAndSchema(
                StringUtils.hasText(invite.getDataSource()) ? invite.getDataSource() : "db0",
                StringUtils.hasText(invite.getDatabaseName()) ? invite.getDatabaseName() : "im_micro",
                tenantCode);
        ThreadContext.put(Constants.TARGET_CUSTOMER, tenantCode);
        ThreadContext.put(Constants.TARGET_DS,
                StringUtils.hasText(invite.getDataSource()) ? invite.getDataSource() : "db0");
        ThreadContext.put(Constants.TARGET_SCHEMA,
                StringUtils.hasText(invite.getDatabaseName()) ? invite.getDatabaseName() : "im_micro");

        // 5. INSERT micro_user + 角色 + 平台关联
        Date now = DateUtils.getNowDate();
        MicroUser newUser = new MicroUser();
        newUser.setTenantCode(tenantCode);
        newUser.setUserName(phone);
        newUser.setPhonenumber(phone);
        newUser.setNickName(param.getNickName());
        newUser.setCreatedBy(phone);
        newUser.setCreatedDate(now);

        // decouple C.19: 邀请码场景下，角色从 inviteCode 决定（worker/auditor/quality_inspector）
        String joinRoleCode = invite.getRoleCode() != null ? invite.getRoleCode().getCode() : RoleCodeEnum.WORKER.getCode();
        String joinRoleName = invite.getRoleCode() != null ? invite.getRoleCode().getDesc() : RoleCodeEnum.WORKER.getDesc();

        // 显式写 user + role + role_re（不再依赖 saveIfAbsentMicroUserCompleteInfo 内部行为）
        Long localUserId = insertMicroUserAndAdminRoleRe(tenantCode, newUser, joinRoleCode, joinRoleName, now);

        // 写 micro_user_platform_re（saveIfAbsentMicroUserCompleteInfo 对已存在 user 是 no-op，只跑 platform_re 分支）
        microUserService.saveIfAbsentMicroUserCompleteInfo(
                buildInviteJoinCompleteInfo(newUser, tenantCode, openid, phone, now,
                        applicationSign, platformType, invite));

        // 6. INSERT micro_user_tenant_index
        MicroUserTenantIndex idx = new MicroUserTenantIndex();
        idx.setPhone(phone);
        idx.setTenantCode(tenantCode);
        idx.setLocalUserId(localUserId);
        idx.setIsPrimary(0); // 加入的租户非主 
        idx.setCreatedAt(now);
        userTenantIndexService.insert(idx);

        // 7. 一次性消费 session（防重放，仅 session 路径）
        if (StringUtils.hasText(param.getSessionId())) {
            String sessionKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(
                    RedisKeys.WX_LOGIN_VERIFIED + param.getSessionId());
            redisCache.deleteObject(sessionKey);
        }

        // DB 已写入角色但 newUser 内存对象未填充，手动补上 microRoles 防止 issueTokenAndBuildResult 写入 null 到 Redis
        MicroRole joinRole = new MicroRole();
        joinRole.setRoleCode(joinRoleCode);
        joinRole.setRoleName(joinRoleName);
        newUser.setMicroRoles(Arrays.asList(joinRole));

        // 8. 发正式 token
        return issueTokenAndBuildResult(phone, tenantCode, newUser,
                resolveAppSign(), resolveDeviceType());
    }

    // ============================================================
    //  已登录用户切换租户
    // ============================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserBaseInfo switchTenant(Map<String, String> headerMap, SwitchTenantParam param) {
        if (param == null || !StringUtils.hasText(param.getTenantCode())) {
            throw new CustomException("参数不合法：tenantCode 必填", 400);
        }

        // 1. 从 headerMap 手动解析 token（不走 @AccessAuth，避免 ThreadContext 被预设为当前租户）
        ResolvedIdentity resolved = resolveIdentityFromToken(headerMap);
        String phone = resolved.phone;
        String appSign = resolved.appSign;
        String deviceType = resolved.deviceType;

        // 2. 校验目标租户是否已关联当前用户（micro_user_tenant_index 无租户隔离，安全）
        MicroUserTenantIndex idx = userTenantIndexService.getByPhoneAndCode(phone, param.getTenantCode());
        if (idx == null) {
            throw new CustomException("您未关联该租户，请先加入", 403);
        }

        // 4. 【关键】先设目标租户 ThreadContext，再查 DB ——
        //    不走 @AccessAuth，ThreadContext 此时是干净的，不会有旧租户残留
        DBControlUtil.setDbAndSchema("db0", "im_micro", param.getTenantCode());
        ThreadContext.put(Constants.TARGET_CUSTOMER, param.getTenantCode());
        ThreadContext.put(Constants.TARGET_DS, "db0");
        ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

        // 5. 查目标租户下的 micro_user
        MicroUser targetUser = microUserService.findMicroUserCompleteInfo(null, phone);
        if (targetUser == null) {
            throw new CustomException("目标租户下用户数据异常", 500);
        }

        // 6. 签发新 token（目标租户）
        log.info("switch-tenant: phone={} 从 {} 切换到 {}",
                phone, "老的", param.getTenantCode());
        return issueTokenAndBuildResult(phone, param.getTenantCode(), targetUser,
                appSign, deviceType);
    }

    /**
     * DevTools RestartClassLoader 兼容：与 UserContextFilter 保持一致的序列化兜底。
     * Redis 反序列化对象可能由不同 ClassLoader 加载，导致 instanceof 失效时用 FastJson 中转。
     */
    private static MicroUserCompleteInfo resolveCacheObject(Object cacheObject) {
        if (cacheObject == null) {
            return null;
        }
        if (cacheObject instanceof MicroUserCompleteInfo) {
            return (MicroUserCompleteInfo) cacheObject;
        }
        try {
            String json = JSON.toJSONString(cacheObject);
            return JSON.parseObject(json, MicroUserCompleteInfo.class);
        } catch (Exception e) {
            log.warn("resolveCacheObject failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 Redis token 解析 (phone, openid, appSign, deviceType)。
     * 用于 register-tenant / join-tenant / switch-tenant 中已登录用户路径，
     * 避免在白名单端点上调 @AccessAuth 或 DB 查询。
     *
     * <p>如果 Redis token 中没有 openid（MicroUserPlatformRe.openId 为空），
     * 则通过 micro_user_tenant_index + micro_user_platform_re 跨租户遍历查找。
     */
    private ResolvedIdentity resolveIdentityFromToken(Map<String, String> headerMap) {
        String token = headerMap.get(CacheConstants.HEADER);
        if (StringUtils.hasText(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.substring(CacheConstants.TOKEN_PREFIX.length());
        }
        String username = headerMap.get(CacheConstants.DETAILS_USERNAME);
        String appSign = headerMap.getOrDefault(Constants.APPLICATION_SIGN,
                CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey());
        String deviceType = headerMap.getOrDefault(CacheConstants.DETAILS_TYPE,
                CommonConstant.DeviceType.WXMINIAPP.getKey());

        if (!StringUtils.hasText(token) || !StringUtils.hasText(username)) {
            throw new CustomException("未登录或登录态无效", 401);
        }

        String redisKey = Constants.LOGIN_MICRO_TOKEN_KEY + username
                + ":" + appSign + ":" + deviceType + ":" + token;
        Object cacheObject = redisCache.getCacheObject(redisKey);
        if (cacheObject == null) {
            throw new CustomException("登录态已过期，请重新登录", 401);
        }
        MicroUserCompleteInfo info = resolveCacheObject(cacheObject);
        if (info == null || info.getMicroUser() == null) {
            throw new CustomException("登录态已过期，请重新登录", 401);
        }

        String openid = "";
        if (info.getMicroUserPlatformRe() != null
                && StringUtils.hasText(info.getMicroUserPlatformRe().getOpenId())) {
            openid = info.getMicroUserPlatformRe().getOpenId();
        }

        // fallback: Redis token 中 openid 为空时，跨租户查找
        if (!StringUtils.hasText(openid)) {
            openid = findOpenIdAcrossTenants(username, appSign, deviceType);
        }

        ResolvedIdentity resolved = new ResolvedIdentity();
        resolved.phone = info.getMicroUser().getPhonenumber();
        resolved.openid = openid;
        resolved.appSign = appSign;
        resolved.deviceType = deviceType;
        return resolved;
    }

    /**
     * 跨租户遍历当前用户已有的 openid（通过 micro_user_tenant_index → 各租户的 micro_user_platform_re）。
     */
    private String findOpenIdAcrossTenants(String phone, String appSign, String deviceType) {
        List<MicroUserTenantIndex> idxs = userTenantIndexService.listByPhone(phone);
        if (CollectionUtils.isEmpty(idxs)) {
            return "";
        }
        for (MicroUserTenantIndex idx : idxs) {
            String tenantCode = idx.getTenantCode();
            try {
                DBControlUtil.setDbAndSchema("db0", "im_micro", tenantCode);
                ThreadContext.put(Constants.TARGET_CUSTOMER, tenantCode);
                ThreadContext.put(Constants.TARGET_DS, "db0");
                ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

                MicroUser mu = microUserService.findMicroUserCompleteInfo(null, phone);
                if (mu != null && mu.getMicroUserPlatformRe() != null
                        && StringUtils.hasText(mu.getMicroUserPlatformRe().getOpenId())) {
                    log.info("findOpenIdAcrossTenants: phone={} 从 tenant={} 找到 openid", phone, tenantCode);
                    return mu.getMicroUserPlatformRe().getOpenId();
                }
            } catch (Exception e) {
                log.warn("findOpenIdAcrossTenants: tenant={} 查询失败: {}", tenantCode, e.getMessage());
            }
        }
        log.warn("findOpenIdAcrossTenants: phone={} 所有租户均未找到 openid", phone);
        return "";
    }

    private static class ResolvedIdentity {
        String phone;
        String openid;
        String appSign;
        String deviceType;
    }

    /**
     * decouple C.17：邀请码加入租户时构造 user complete info
     * 角色从 invite.code 取
     */
    private MicroUserCompleteInfo buildInviteJoinCompleteInfo(
            MicroUser newUser, String tenantCode, String openid, String phone, Date now,
            String applicationSign, String platformType, MicroInviteCodeInfo invite) {
        MicroUserCompleteInfo info = new MicroUserCompleteInfo();
        info.setCustomer(tenantCode);
        info.setCustomerName(newUser.getTenantName());
        info.setSchema(StringUtils.hasText(invite.getDatabaseName()) ? invite.getDatabaseName() : "im_micro");
        info.setDataSource(StringUtils.hasText(invite.getDataSource()) ? invite.getDataSource() : "db0");
        info.setMicroUser(newUser);

        MicroRole role = new MicroRole();
        role.setTenantCode(tenantCode);
        role.setRoleCode(invite.getRoleCode() != null ? invite.getRoleCode().getCode() : RoleCodeEnum.WORKER.getCode());
        role.setRoleName(invite.getRoleCode() != null ? invite.getRoleCode().getDesc() : RoleCodeEnum.WORKER.getDesc());
        role.setStatus(ActiveFlagEnum.NORMAL.getCode());
        role.setCreatedBy(phone);
        role.setCreatedDate(now);
        info.setMicroRoles(Arrays.asList(role));

        // 仅当 openid 非空时才写 MicroUserPlatformRe，避免 MyBatis 动态 SQL 跳过 open_id
        // 导致 INSERT 缺少 NOT NULL 列（schema 级 open_id 无默认值）
        if (StringUtils.hasText(openid)) {
            MicroUserPlatformRe re = new MicroUserPlatformRe();
            re.setTenantCode(tenantCode);
            re.setOpenId(openid);
            re.setApplicationSign(applicationSign);
            re.setPlatformType(platformType);
            re.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
            re.setCreatedBy(phone);
            re.setCreatedDate(now);
            info.setMicroUserPlatformRe(re);
        }

        return info;
    }

    // ============================================================
    //  微信小程序登录（V2 - 接管原 MicroLoginServiceImpl.wxMiniAppLogin）
    // ============================================================

    @Override
    public UserBaseInfo wxMiniAppLoginV2(Map<String, String> headerMap, WxMiniAppLoginParam param) {
        // 1. 强校验 phoneCode（decouple 决策：必填）
        if (!StringUtils.hasText(param.getPhoneCode())) {
            log.warn("wxMiniAppLoginV2: phoneCode 为空");
            throw new CustomException("PHONE_REQUIRED", 400);
        }
        param.checkParam();

        // 2. 微信 code2Session → openid
        String openid = queryCode2SessionOpenid(param.getCode());

        // 3. phoneCode → phone
        String phone = remotePhoneCodeToPhoneNumber(param.getPhoneCode());

        // decouple-from-ops-platform-cleanup (C.16): 从 headerMap 提取 applicationSign + platformType
        // （前端 wxMiniAppLogin 调用时通过 RequestUtils.addRequestHeaders 注入）
        String applicationSign = headerMap.getOrDefault("application_sign", "micro_process");
        String platformType = headerMap.getOrDefault("type", "wechatMiniApp");

        // 4. 查该手机号关联的所有租户
        // 注：hhim-common-datasource 的 SchemaIntercept 已升级（2026-06-05），
        // schema/customer 为空时不再抛异常，而是 log.warn + 原样执行。
        // 业务侧不再需要手动 setDbAndSchema 兜底。
        List<MicroUserTenantIndex> linkedTenants = userTenantIndexService.listByPhone(phone);

        // 5. 0 租户 → 把验证过的手机号写入 session，返 sessionId 让前端去 register-tenant
        // decouple-from-ops-platform-cleanup (C.16): 不抛 403，让前端用 session 继续注册
        if (CollectionUtils.isEmpty(linkedTenants)) {
            log.warn("wxMiniAppLoginV2: phone={} 无关联租户，进入注册流", phone);
            return buildNeedsRegisterResponse(phone, openid, applicationSign, platformType);
        }

        // 6. 1 租户 → 直接发 token
        if (linkedTenants.size() == 1) {
            MicroUserTenantIndex idx = linkedTenants.get(0);
            // 设置 thread context（单库场景：db0 / im_micro）
            DBControlUtil.setDbAndSchema("db0", "im_micro", idx.getTenantCode());
            ThreadContext.put(Constants.TARGET_CUSTOMER, idx.getTenantCode());
            ThreadContext.put(Constants.TARGET_DS, "db0");
            ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

            MicroUser microUser = microUserService.findMicroUserCompleteInfo(null, phone);
            if (microUser == null) {
                // 数据脏状态兜底：tenant_index 记录存在但 micro_user 实际不存在
                // 不要再抛 USER_NOT_FOUND 让前端死循环，跳到 register-tenant 走开通新租户
                log.warn("wxMiniAppLoginV2: phone={} tenant_index 有 1 条但 micro_user 缺失，降级到注册流 tenant={}",
                        phone, idx.getTenantCode());
                return buildNeedsRegisterResponse(phone, openid, applicationSign, platformType);
            }
            log.info("wxMiniAppLoginV2: phone={} 单租户直发 token tenant={}", phone, idx.getTenantCode());
            return issueTokenAndBuildResult(phone, idx.getTenantCode(), microUser,
                    applicationSign, platformType);
        }

        // 7. N 租户 → 返 tempToken + tenantList（前端弹 picker）
        String tempToken = issueTempToken(phone);
        UserBaseInfo result = new UserBaseInfo();
        result.setMultiTenant(true);
        result.setTempToken(tempToken);
        result.setPhoneNumber(phone);
        result.setOpenId(openid);
        result.setTenantList(buildTenantOptions(phone));
        log.info("wxMiniAppLoginV2: phone={} 多租户，发放 tempToken + {} 个选项", phone, result.getTenantList().size());
        return result;
    }

    // ============================================================
    //  短信验证码登录
    // ============================================================

    @Override
    public void sendSmsLoginCode(String phoneNumber) {
        if (!StringUtils.hasText(phoneNumber)) {
            throw new CustomException("手机号不能为空", 400);
        }

        // 限流：检查是否在 60 秒内已发送过
        String rateLimitKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(
                RedisKeys.SMS_LOGIN_CODE + "rate:" + phoneNumber);
        String rateLimitVal = redisCache.getCacheObject(rateLimitKey);
        if (StringUtils.hasText(rateLimitVal)) {
            throw new CustomException("SMS_CODE_RATE_LIMIT：请60秒后重试", 429);
        }

        // 生成 6 位数字验证码（100000~999999，SecureRandom 防预测）
        String code = String.valueOf(RandomUtil.randomInt(100000, 1000000));
        log.info("sendSmsLoginCode: phoneNumber={}, code={}", phoneNumber, code);

        // 验证码存入 Redis（5 min TTL）
        String codeKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.SMS_LOGIN_CODE + phoneNumber);
        redisCache.setCacheObject(codeKey, code, (int) SMS_CODE_TTL_SECONDS, TimeUnit.SECONDS);

        // 限流标记存入 Redis（60 秒 TTL）
        redisCache.setCacheObject(rateLimitKey, "1", (int) SMS_SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 发送短信
        try {
            SendSmsParam sendSmsParam = new SendSmsParam();
            sendSmsParam.setTempId(loginVerificationCodeTempleteId);
            sendSmsParam.setReceiver(phoneNumber);
            // 模板参数：验证码
            java.util.Map<String, String> params = new java.util.HashMap<>();
            params.put("code", code);
            params.put("minute", "5");
            sendSmsParam.setParams(params);
            com.cosmo.hhim.thirdplat.common.domain.APIResponse<Boolean> apiResponse = remoteSmsService.sendSms(sendSmsParam);
            if (!apiResponse.isSuccess()) {
                log.warn("sendSmsLoginCode: SMS发送失败 phoneNumber={}", phoneNumber);
                throw new CustomException("验证码发送失败，请稍后重试", 500);
            }
        } catch (Exception e) {
            log.error("sendSmsLoginCode: SMS发送异常 phoneNumber={}", phoneNumber, e);
            throw new CustomException("验证码发送失败，请稍后重试", 500);
        }

        log.info("sendSmsLoginCode: SMS已发送 phoneNumber={}", phoneNumber);
    }

    @Override
    public UserBaseInfo smsLogin(Map<String, String> headerMap, SmsLoginParam param) {
        if (param == null || !StringUtils.hasText(param.getPhoneNumber())
                || !StringUtils.hasText(param.getSmsCode())) {
            throw new CustomException("参数不合法：phoneNumber + smsCode 必填", 400);
        }

        String phoneNumber = param.getPhoneNumber();
        String smsCode = param.getSmsCode();
        boolean isH5 = Boolean.TRUE.equals(param.getIsH5());

        // 1. 非 H5 先做微信 code2Session（失败抛 502，短信验证码不动，可重试）
        String openid = null;
        if (isH5) {
            log.info("smsLogin: H5端登录 phone={}", phoneNumber);
        } else {
            if (!StringUtils.hasText(param.getCode())) {
                throw new CustomException("微信小程序登录临时code不允许为空！", 400);
            }
            openid = queryCode2SessionOpenid(param.getCode());
        }

        // 2. 校验短信验证码（万能验证码通道已下线，仅 Redis 校验）
        //    放在 code2Session 之后：微信 code 失败时验证码仍在 Redis 中，重试不会出现"第一次失败、第二次过期"
        String codeKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.SMS_LOGIN_CODE + phoneNumber);
        String cachedCode = redisCache.getCacheObject(codeKey);
        if (!StringUtils.hasText(cachedCode)) {
            log.warn("smsLogin: phoneNumber={} 验证码不存在或已过期", phoneNumber);
            throw new CustomException("SMS_CODE_EXPIRED：验证码已过期，请重新获取", 401);
        }
        if (!cachedCode.equals(smsCode)) {
            log.warn("smsLogin: phoneNumber={} 验证码不匹配", phoneNumber);
            throw new CustomException("SMS_CODE_INVALID：验证码错误", 401);
        }
        // 微信身份与短信验证码均有效，此时才消费验证码（防重放）
        redisCache.deleteObject(codeKey);
        // 同时删除限流标记（允许立即重新发送）
        String rateLimitKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(
                RedisKeys.SMS_LOGIN_CODE + "rate:" + phoneNumber);
        redisCache.deleteObject(rateLimitKey);

        // 3. applicationSign + platformType
        String applicationSign = headerMap.getOrDefault("application_sign", "micro_process");
        String platformType = headerMap.getOrDefault("type", "wechatMiniApp");

        // 4. 查该手机号关联的所有租户（复用 wxMiniAppLoginV2 的后半段逻辑）
        List<MicroUserTenantIndex> linkedTenants = userTenantIndexService.listByPhone(phoneNumber);

        // 5. 0 租户 → 进入注册流
        if (CollectionUtils.isEmpty(linkedTenants)) {
            log.warn("smsLogin: phone={} 无关联租户，进入注册流", phoneNumber);
            return buildNeedsRegisterResponse(phoneNumber, openid, applicationSign, platformType);
        }

        // 6. 1 租户 → 直接发 token
        if (linkedTenants.size() == 1) {
            MicroUserTenantIndex idx = linkedTenants.get(0);
            DBControlUtil.setDbAndSchema("db0", "im_micro", idx.getTenantCode());
            ThreadContext.put(Constants.TARGET_CUSTOMER, idx.getTenantCode());
            ThreadContext.put(Constants.TARGET_DS, "db0");
            ThreadContext.put(Constants.TARGET_SCHEMA, "im_micro");

            MicroUser microUser = microUserService.findMicroUserCompleteInfo(null, phoneNumber);
            if (microUser == null) {
                log.warn("smsLogin: phone={} tenant_index 有 1 条但 micro_user 缺失，降级到注册流 tenant={}",
                        phoneNumber, idx.getTenantCode());
                return buildNeedsRegisterResponse(phoneNumber, openid, applicationSign, platformType);
            }
            log.info("smsLogin: phone={} 单租户直发 token tenant={}", phoneNumber, idx.getTenantCode());
            return issueTokenAndBuildResult(phoneNumber, idx.getTenantCode(), microUser,
                    applicationSign, platformType);
        }

        // 7. N 租户 → 返 tempToken + tenantList
        String tempToken = issueTempToken(phoneNumber);
        UserBaseInfo result = new UserBaseInfo();
        result.setMultiTenant(true);
        result.setTempToken(tempToken);
        result.setPhoneNumber(phoneNumber);
        result.setOpenId(openid);
        result.setTenantList(buildTenantOptions(phoneNumber));
        log.info("smsLogin: phone={} 多租户，发放 tempToken + {} 个选项", phoneNumber, result.getTenantList().size());
        return result;
    }

    /**
     * 微信小程序 code2Session（通过 hhim-third-platform 调微信 API）
     */
    private String queryCode2SessionOpenid(String code) {
        APIResponse<Code2SessionInfo> r = remoteWxMiniAppBaseService.wxMiniAppLoginIn(code);
        if (!r.isSuccess() || r.getData() == null || r.getData().getOpenid() == null) {
            log.error("wxMiniAppLoginV2: code2Session 失败 success={}, data={}", r.isSuccess(), r.getData());
            throw new CustomException("WECHAT_SESSION_FAILED: code2Session 返回空", 502);
        }
        return r.getData().getOpenid();
    }

    /**
     * phoneCode → 微信真实手机号（通过 hhim-third-platform 调微信 API）
     */
    private String remotePhoneCodeToPhoneNumber(String phoneCode) {
        UserPhoneNumberParam numberParam = new UserPhoneNumberParam();
        numberParam.setCode(phoneCode);
        APIResponse<UserPhoneInfo> r = remoteWxMiniAppUserInfoService.getPhoneNumber(numberParam);
        if (!r.isSuccess() || r.getData() == null || r.getData().getPhone_info() == null) {
            log.error("wxMiniAppLoginV2: getPhoneNumber 失败 success={}, data={}", r.isSuccess(), r.getData());
            throw new CustomException("WECHAT_PHONE_FAILED: getPhoneNumber 返回空", 502);
        }
        return r.getData().getPhone_info().getPurePhoneNumber();
    }

    // ============================================================
    //  0 租户场景 → 把验证过的手机号存 session，返 sessionId 给前端
    // ============================================================

    /** wxLogin:verified session 有效期（5 分钟） */
    private static final long WX_LOGIN_VERIFIED_TTL_SECONDS = 300L;

    /**
     * decouple-from-ops-platform-cleanup (C.16): 0 租户场景的响应
     *
     * <p>微信 API 已验证过 phone = 真手机号，openid = 真 openid。
     * 把这两个关键值存 Redis session（5 min TTL），返 sessionId。
     * 前端用 sessionId 调 /login/register-tenant，register-tenant 从 session 读 phone，避免伪造。
     *
     * <p>响应里 multiTenant = "needsRegister"（前端据此跳注册页）
     */
    private UserBaseInfo buildNeedsRegisterResponse(String phone, String openid, String applicationSign, String platformType) {
        String sessionId = UUID.randomUUID().toString();
        WxLoginVerifiedSession session = new WxLoginVerifiedSession();
        session.phone = phone;
        session.openid = openid;
        session.applicationSign = applicationSign;
        session.platformType = platformType;
        session.createdAt = System.currentTimeMillis() / 1000;
        redisCache.setCacheObject(
                RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.WX_LOGIN_VERIFIED + sessionId),
                JSON.toJSONString(session),
                (int) WX_LOGIN_VERIFIED_TTL_SECONDS,
                TimeUnit.SECONDS);

        log.info("wxMiniAppLoginV2: phone={} 0 租户，写入 sessionId={}（5min TTL）", phone, sessionId);

        UserBaseInfo result = new UserBaseInfo();
        result.setMultiTenant(false);
        result.setTempToken(sessionId);  // 复用 tempToken 字段（语义："继续注册"凭据） 
        result.setPhoneNumber(phone);    // 前端可读，但 register-tenant 不接受 
        result.setOpenId(openid);
        result.setTenantList(new ArrayList<>());
        // 多 1 个字段指示前端这是"需注册"场景
        //（用 UserBaseInfo 已有的字段：multiTenant=false + tenantList=[]，前端看这俩就能判断）
        return result;
    }

    /** Redis 存的 session 结构 */
    public static class WxLoginVerifiedSession {
        public String phone;
        public String openid;
        public String applicationSign;
        public String platformType;
        public Long createdAt;
    }

    // ============================================================
    //  工具方法
    // ============================================================

    /**
     * 从请求头获取设备类型（type），与 MicroAccessAuthFilterCondition 读的 header 一致。
     * 不依赖 SecurityUtils（selectTenant/registerTenant 阶段 SecurityContext 可能尚未设置）。
     */
    private String resolveDeviceType() {
        String type = getRequestHeader(CacheConstants.DETAILS_TYPE);
        if (org.springframework.util.StringUtils.hasText(type)) {
            return type;
        }
        return CommonConstant.DeviceType.WXMINIAPP.getKey();
    }

    /**
     * 从请求头获取应用标识（application_sign），与 MicroAccessAuthFilterCondition 读的 header 一致。
     * 不依赖 SecurityUtils（selectTenant/registerTenant 阶段 SecurityContext 可能尚未设置）。
     */
    private String resolveAppSign() {
        String appSign = getRequestHeader(Constants.APPLICATION_SIGN);
        if (org.springframework.util.StringUtils.hasText(appSign)) {
            return appSign;
        }
        return CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey();
    }

    private String getRequestHeader(String name) {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                (org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader(name);
            }
        } catch (Exception e) {
            log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
        }
        return null;
    }

    /**
     * 由登录/选择/注册流程统一调用：发 token + 写 Redis + 构造返回
     */
    private UserBaseInfo issueTokenAndBuildResult(String phone, String tenantCode, MicroUser microUser,
                                                   String appSign, String deviceType) {
        String token = UUID.randomUUID().toString();
        long curTimeStamp = System.currentTimeMillis() / 1000;
        long expireTimeStamp = curTimeStamp + tokenEffectiveTime;

        MicroUserCompleteInfo userCompleteInfo = new MicroUserCompleteInfo();
        userCompleteInfo.setCustomer(tenantCode);
        userCompleteInfo.setCustomerName(microUser.getTenantName());
        userCompleteInfo.setMicroUser(microUser);
        userCompleteInfo.setMicroRoles(microUser.getMicroRoles() != null ? microUser.getMicroRoles() : new ArrayList<>());
        userCompleteInfo.setMicroUserPlatformRe(microUser.getMicroUserPlatformRe());
        userCompleteInfo.setMicroUserAppRes(microUser.getMicroUserAppRes() != null ? microUser.getMicroUserAppRes() : new ArrayList<>());
        userCompleteInfo.setSchema("im_micro");
        userCompleteInfo.setDataSource("db0");
        userCompleteInfo.setLoginTime(curTimeStamp);
        userCompleteInfo.setExpireTime(expireTimeStamp);
        userCompleteInfo.setToken(token);

        // key 格式与 MicroAccessAuthFilterCondition.getTokenRedisKey 保持一致
        String redisKey = Constants.LOGIN_MICRO_TOKEN_KEY + microUser.getUserName()
                + ":" + appSign + ":" + deviceType + ":" + token;
        redisCache.setCacheObject(redisKey, userCompleteInfo, tokenEffectiveTime, TimeUnit.SECONDS);

        // 写入用户缓存信息，供 genImportLinkTemp 等接口使用
        String userCacheKey = CommonConstants.USER_CACHE_INFO + appSign + ":" + tenantCode + ":" + microUser.getId();
        redisCache.setCacheObject(userCacheKey, microUser, tokenEffectiveTime, TimeUnit.SECONDS);

        // 构造 UserBaseInfo + tenantList
        UserBaseInfo result = new UserBaseInfo();
        result.setToken(token);
        result.setUserName(microUser.getUserName());
        result.setPhoneNumber(phone);
        result.setOpenId(microUser.getPhonenumber());
        if (microUser.getMicroRoles() != null && !microUser.getMicroRoles().isEmpty()) {
            result.setRoleCode(microUser.getMicroRoles().get(0).getRoleCode());
        }
        result.setTenantList(buildTenantOptions(phone));
        return result;
    }

    private List<UserBaseInfo.TenantOption> buildTenantOptions(String phone) {
        List<MicroUserTenantIndex> idxs = userTenantIndexService.listByPhone(phone);
        if (CollectionUtils.isEmpty(idxs)) {
            return new ArrayList<>();
        }
        return idxs.stream().map(idx -> {
            UserBaseInfo.TenantOption opt = new UserBaseInfo.TenantOption();
            opt.setTenantCode(idx.getTenantCode());
            MicroTenant t = microTenantService.getByCode(idx.getTenantCode());
            if (t != null) {
                opt.setTenantName(t.getTenantName());
                opt.setCustomerName(t.getCustomerName());
            }
            opt.setIsPrimary(idx.getIsPrimary());
            return opt;
        }).collect(Collectors.toList());
    }

    /**
     * 给定 phone，写入 tempToken → phone 映射（5 分钟）
     */
    public String issueTempToken(String phone) {
        String tempToken = UUID.randomUUID().toString();
        String tempKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.TEMP_TOKEN + tempToken);
        // setCacheObject(String, T, Integer, TimeUnit) — long TTL 转 Integer
        redisCache.setCacheObject(tempKey, phone, (int) TEMP_TOKEN_TTL_SECONDS, TimeUnit.SECONDS);
        return tempToken;
    }

    private String generateTenantCode() {
        return RandomUtil.randomString(TENANT_CODE_CHARS, 6);
    }

    /**
     * decouple C.19: 注册用户 + 显式写入管理员角色关联
     *
     * <p>不依赖 saveIfAbsentMicroUserCompleteInfo 内部行为，直接做 3 件事：
     * <ol>
     *   <li>INSERT micro_user（拿 useGeneratedKeys 回填的 id）</li>
     *   <li>SELECT micro_role WHERE tenant_code=? AND role_code=?；不存在就 INSERT</li>
     *   <li>INSERT micro_user_role_re (tenant_code, user_id, role_id)</li>
     * </ol>
     *
     * <p>uniq_user_tenant (tenant_code, user_name) 保证租户内用户名唯一。
     * 每个 (用户, 租户) 组合独立一行 micro_user，不再跨租户复用。
     * MyBatis-Plus 拦截器自动注入 tenant_code 是正确的、需要的。
     */
    private Long insertMicroUserAndAdminRoleRe(String tenantCode, MicroUser admin, String roleCode, String roleName, Date now) {
        // 1. INSERT micro_user（租户内唯一：uniq_user_tenant ON tenant_code + user_name）
        Long userId = null;
        try {
            int userInsertRows = microUserService.insertMicroUser(admin);
            if (userInsertRows <= 0) {
                throw new CustomException("创建用户失败：INSERT micro_user 影响行数为 0");
            }
            userId = admin.getId();
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            // 并发场景：同租户内两个请求同时注册同一 user_name，撞 uniq_user_tenant
            // 回查一次拿到 userId
            log.warn("register-tenant: INSERT 撞 uniq_user_tenant，回查 userName={} tenantCode={}",
                    admin.getUserName(), tenantCode);
            MicroUser reloaded = microUserService.selectUserBaseInfo(admin.getUserName(), null);
            if (reloaded == null || reloaded.getId() == null) {
                throw new CustomException("创建用户失败：uniq_user_tenant 冲突且回查失败");
            }
            userId = reloaded.getId();
            admin.setId(userId);
        }
        if (userId == null) {
            // 极端情况：useGeneratedKeys 没回填（极少数 MyBatis + Druid 组合），按 userName 回查
            log.warn("register-tenant: useGeneratedKeys 未回填，按 userName={} 回查", admin.getUserName());
            MicroUser reloaded = microUserService.selectUserBaseInfo(admin.getUserName(), null);
            if (reloaded == null || reloaded.getId() == null) {
                throw new CustomException("创建用户失败：无法回查 id");
            }
            userId = reloaded.getId();
            admin.setId(userId);
        }

        // 2. 查/插 micro_role（带 tenant_code 作用域，避免跨租户复用）
        Long roleId = selectOrInsertRole(tenantCode, roleCode, roleName, now);

        // 3. INSERT micro_user_role_re
        MicroUserRoleRe roleRe = new MicroUserRoleRe();
        roleRe.setTenantCode(tenantCode);
        roleRe.setUserId(userId);
        roleRe.setRoleId(roleId);
        try {
            microUserService.insertMicroUserRoleRe(roleRe);
        } catch (org.springframework.dao.DuplicateKeyException dup) {
            // 关联已存在（重试场景）→ 当作成功
            log.info("register-tenant: micro_user_role_re 已存在 (userId={}, roleId={})", userId, roleId);
        }
        log.info("register-tenant: 显式写角色关联 userId={} roleId={} (tenant={}, code={})",
                userId, roleId, tenantCode, roleCode);
        return userId;
    }

    /**
     * 查 (tenant_code, role_code) 对应的 role；不存在则插入并返回新 id
     */
    private Long selectOrInsertRole(String tenantCode, String roleCode, String roleName, Date now) {
        // 用通用 selectMicroRoleList（带 tenant_code + role_code 过滤）查
        MicroRole query = new MicroRole();
        query.setTenantCode(tenantCode);
        query.setRoleCode(roleCode);
        java.util.List<MicroRole> existing = microUserService.selectMicroRoleList(query);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0).getId();
        }
        MicroRole newRole = new MicroRole();
        newRole.setTenantCode(tenantCode);
        newRole.setRoleCode(roleCode);
        newRole.setRoleName(roleName);
        newRole.setStatus("0");
        newRole.setCreatedBy(tenantCode);
        newRole.setCreatedDate(now);
        int rows = microUserService.insertMicroRole(newRole);
        if (rows <= 0 || newRole.getId() == null) {
            throw new CustomException("创建角色失败");
        }
        return newRole.getId();
    }

    private MicroUserCompleteInfo buildAdminCompleteInfo(MicroUser admin, String tenantCode, String openid, Date now, String applicationSign, String platformType) {
        MicroUserCompleteInfo info = new MicroUserCompleteInfo();
        info.setCustomer(tenantCode);
        info.setCustomerName(admin.getTenantName());
        info.setSchema("im_micro");
        info.setDataSource("db0");
        info.setMicroUser(admin);

        MicroRole role = new MicroRole();
        role.setTenantCode(tenantCode);
        role.setRoleCode(RoleCodeEnum.MANAGER.getCode());
        role.setRoleName(RoleCodeEnum.MANAGER.getDesc());
        role.setStatus(ActiveFlagEnum.NORMAL.getCode());
        role.setCreatedBy(admin.getUserName());
        role.setCreatedDate(now);
        info.setMicroRoles(Arrays.asList(role));

        // 仅当 openid 非空时才写 MicroUserPlatformRe，避免 MyBatis 动态 SQL 跳过 open_id
        // 导致 INSERT 缺少 NOT NULL 列（schema 级 open_id 无默认值）
        if (StringUtils.hasText(openid)) {
            MicroUserPlatformRe re = new MicroUserPlatformRe();
            re.setTenantCode(tenantCode);
            re.setUserId(admin.getId());
            re.setOpenId(openid);
            // decouple-from-ops-platform-cleanup (C.14): applicationSign + platformType 由前端动态传入
            re.setApplicationSign(applicationSign);
            re.setPlatformType(platformType);
            re.setActiveFlag(ActiveFlagEnum.NORMAL.getCode());
            re.setCreatedBy(admin.getUserName());
            re.setCreatedDate(now);
            info.setMicroUserPlatformRe(re);
        }

        return info;
    }
}
