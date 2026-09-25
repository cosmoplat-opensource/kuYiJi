/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.service.common.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.DateUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.*;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroLoginService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroUserService;
import com.cosmo.hhim.micro.infrastructure.constant.CommonConstants;
import com.cosmo.hhim.micro.infrastructure.constant.RedisKeys;
import com.cosmo.hhim.micro.infrastructure.enums.RoleCodeEnum;
import com.cosmo.hhim.micro.infrastructure.util.DateUtil;
import com.cosmo.hhim.micro.infrastructure.util.RequestUtils;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppQrCodeService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.RemoteWxMiniAppUserInfoService;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UnlimitedQRCodeParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in.UserPhoneNumberParam;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.QRCodeInfo;
import com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out.UserPhoneInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.text.MessageFormat;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 微应用登录服务
 *
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@RefreshScope
public class MicroLoginServiceImpl implements IMicroLoginService {

    private final IMicroTenantService microTenantService;
    private final RemoteWxMiniAppUserInfoService remoteWxMiniAppUserInfoService;
    private final RemoteWxMiniAppQrCodeService remoteWxMiniAppQrCodeService;
    private final RedisCache redisCache;
    private final IMicroUserService microUserService;

    private static final String BASE_CHAR = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

    @Value("${settings.tokenEffectiveTime:604800}")
    private Integer tokenEffectiveTime;

    /**
     * 调用三方平台根据应用标识查询应用编码
     */
    public String remoteQueryAppCode(String appSign) {
        Map<String, Object> cacheMap = redisCache.getCacheMap(CommonConstants.REDIS_APP_SIGN_CODE_MAPPING_KEY);
        return CollectionUtils.isEmpty(cacheMap) ? "" : (String) cacheMap.get(appSign);
    }

    /**
     * 生成邀请码
     */
    @Override
    public WxMiniAppInviteCodeResult genInviteCode(Map<String, String> headerMap, WxMiniAppInviteCodeParam param) {
        WxMiniAppInviteCodeResult result = new WxMiniAppInviteCodeResult();

        // 1.校验角色的合法性
        if (null == RoleCodeEnum.getEnum(param.getRoleCode())) {
            throw new CustomException("角色不存在！请校验入参的合法性！");
        }

        // 判断当前角色是否有分享指定角色的权限
        if (!isHaveRoleShareAuth(param.getRoleCode())) {
            throw new CustomException("当前用户无权限分享所指定的角色！");
        }

        String appCode = remoteQueryAppCode(headerMap.get(Constants.APPLICATION_SIGN));

        // 2.生成邀请码
        String customerCode = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
        MicroInviteCodeInfo microInviteCodeInfo = new MicroInviteCodeInfo();
        microInviteCodeInfo.setCustomerCode(customerCode);
        microInviteCodeInfo.setDatabaseName((String) ThreadContext.get(Constants.TARGET_SCHEMA));
        microInviteCodeInfo.setDataSource((String) ThreadContext.get(Constants.TARGET_DS));
        microInviteCodeInfo.setRoleCode(RoleCodeEnum.getEnum(param.getRoleCode()));
        microInviteCodeInfo.setExpireTime(param.getValidTime() * 3600);
        microInviteCodeInfo.setAppCode(appCode);

        String inviteCode = RandomUtil.randomString(BASE_CHAR, 6);
        String redisKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.INVITE_CODE + inviteCode);
        redisCache.setCacheObject(redisKey, JSON.toJSONString(microInviteCodeInfo), param.getValidTime(), TimeUnit.MINUTES);

        // 3.调用三方平台生成小程序码
        UnlimitedQRCodeParam requestParam = new UnlimitedQRCodeParam();
        if (StringUtils.hasText(param.getInviteToPage())) {
            requestParam.setPage(param.getInviteToPage());
        }
        if (StringUtils.hasText(param.getInviteToEnv())) {
            requestParam.setEnv_version(param.getInviteToEnv());
        }
        requestParam.setCheck_path(param.getCheckPath());
        String sceneEncode = MessageFormat.format("code={0}&role={1}", inviteCode, param.getRoleCode());
        requestParam.setScene(sceneEncode);

        QRCodeInfo qrCodeInfo = remoteWxMiniAppQrCodeService.getMiniAppUnLimitQrCode(requestParam).getData();
        if (null == qrCodeInfo || null == qrCodeInfo.getBuffer() || qrCodeInfo.getBuffer().length == 0) {
            throw new CustomException("调用三方平台生成小程序码失败！");
        }

        // 查询租户名称（查本地 micro_tenant）
        String tenantName = null;
        try {
            MicroTenant tenant = microTenantService.getByCode(customerCode);
            if (tenant != null) {
                tenantName = tenant.getCustomerName();
            }
        } catch (Exception e) {
            log.error("查询租户名称失败, customerCode: {}", customerCode, e);
        }
        if (!StringUtils.hasText(tenantName)) {
            tenantName = "";
        }

        // 计算邀请码过期时间
        Date nowDate = DateUtils.getNowDate();
        Date expireTime = DateUtil.plus(nowDate, param.getValidTime(), ChronoUnit.MINUTES);

        result.setBuffer(qrCodeInfo.getBuffer());
        result.setInviteCode(inviteCode);
        result.setRoleCode(param.getRoleCode());
        result.setRoleName(RoleCodeEnum.getEnum(param.getRoleCode()).getDesc());
        result.setTenantCode(customerCode);
        result.setTenantName(tenantName);
        result.setInviteCodeExpireTime(expireTime);
        return result;
    }

    /**
     * 解析邀请码
     */
    @Override
    public InviteCodeInfoResult parseInviteCode(String inviteCode) {
        InviteCodeInfoResult result = new InviteCodeInfoResult();
        MicroInviteCodeInfo microInviteCodeInfo = getMicroInviteCodeInfo(inviteCode);
        BeanUtils.copyProperties(microInviteCodeInfo, result);
        result.setRoleName(microInviteCodeInfo.getRoleCode().getDesc());
        return result;
    }

    /**
     * 通过phoneCode换取微信真实手机号
     */
    @Override
    public String phoneCodeToPhoneNumber(Map<String, String> headerMap, String phoneCode) {
        RequestUtils.addRequestHeaders(headerMap);
        return remotePhoneCodeToPhoneNumber(phoneCode);
    }

    /**
     * 判断当前角色是否有分享指定角色的权限
     */
    private boolean isHaveRoleShareAuth(String shareRoleCode) {
        List<MicroRole> microRoles = microUserService.selectMicroRolesByUserId(SecurityUtils.getUserId());
        log.info("当前用户：{}, 当前用户拥有的角色：{}, 需要分享的角色：{}", SecurityUtils.getUserId(), JSON.toJSONString(microRoles), shareRoleCode);

        List<MicroRole> managerRoles = microRoles.stream()
                .filter(e -> RoleCodeEnum.getEnum(e.getRoleCode()) == RoleCodeEnum.MANAGER)
                .collect(Collectors.toList());
        if (!managerRoles.isEmpty()) {
            return true;
        }

        List<MicroRole> auditorRoles = microRoles.stream()
                .filter(e -> RoleCodeEnum.getEnum(e.getRoleCode()) == RoleCodeEnum.AUDITOR
                        || RoleCodeEnum.getEnum(e.getRoleCode()) == RoleCodeEnum.QUALITY_INSPECTOR)
                .collect(Collectors.toList());
        boolean isAuditorOrInspectorOrWorker = RoleCodeEnum.getEnum(shareRoleCode) == RoleCodeEnum.AUDITOR
                || RoleCodeEnum.getEnum(shareRoleCode) == RoleCodeEnum.QUALITY_INSPECTOR
                || RoleCodeEnum.getEnum(shareRoleCode) == RoleCodeEnum.WORKER;
        if (!auditorRoles.isEmpty() && isAuditorOrInspectorOrWorker) {
            return true;
        }

        List<MicroRole> workerRoles = microRoles.stream()
                .filter(e -> RoleCodeEnum.getEnum(e.getRoleCode()) == RoleCodeEnum.WORKER)
                .collect(Collectors.toList());
        if (!workerRoles.isEmpty() && RoleCodeEnum.getEnum(shareRoleCode) == RoleCodeEnum.WORKER) {
            return true;
        }
        return false;
    }

    /**
     * 邀请码换租户和角色信息
     */
    private MicroInviteCodeInfo getMicroInviteCodeInfo(String inviteCode) {
        String redisKey = RedisKeys.MicroRegion.LOGIN_MODULE.value(RedisKeys.INVITE_CODE + inviteCode);
        String inviteCodeInfoStr = redisCache.getCacheObject(redisKey);
        if (!StringUtils.hasText(inviteCodeInfoStr)) {
            throw new CustomException("邀请码无效！");
        }
        return JSON.parseObject(inviteCodeInfoStr, MicroInviteCodeInfo.class);
    }

    /**
     * 调用三方平台通过phoneCode换phoneNumber
     */
    private String remotePhoneCodeToPhoneNumber(String phoneCode) {
        UserPhoneNumberParam numberParam = new UserPhoneNumberParam();
        numberParam.setCode(phoneCode);
        APIResponse<UserPhoneInfo> phoneInfoAPIResponse = remoteWxMiniAppUserInfoService.getPhoneNumber(numberParam); 
        if (!phoneInfoAPIResponse.isSuccess()) {
            throw new CustomException("调用三方平台通过code换微信手机号失败！");
        }
        return phoneInfoAPIResponse.getData().getPhone_info().getPurePhoneNumber();
    }
}
