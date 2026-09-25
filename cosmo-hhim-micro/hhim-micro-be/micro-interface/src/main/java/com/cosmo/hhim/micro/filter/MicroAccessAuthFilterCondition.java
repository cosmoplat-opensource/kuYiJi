/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.filter;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.cosmo.hhim.common.security.accessauth.filter.CustomizedAccessAuthFilterCondition;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroRole;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserCompleteInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-11-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MicroAccessAuthFilterCondition implements CustomizedAccessAuthFilterCondition {

    private final RedisCache redisCache;

    @Override
    public LoginUser getLoginUser(HttpServletRequest request) {
        String type = request.getHeader(CacheConstants.DETAILS_TYPE);
        String appSign = request.getHeader(Constants.APPLICATION_SIGN);
        String username = this.getUsername(request);
        String token = this.getToken(request);

        if (!StringUtils.hasText(type) || !StringUtils.hasText(appSign) || !StringUtils.hasText(username) || !StringUtils.hasText(token)) {
            return null;
        }

        Object cacheObject = redisCache.getCacheObject(getTokenRedisKey(token, username, type, appSign));
        MicroUserCompleteInfo microUserCompleteInfo = resolveCacheObject(cacheObject);
        if (microUserCompleteInfo == null) {
            return null;
        }
        if (microUserCompleteInfo.getMicroRoles() == null || microUserCompleteInfo.getMicroUser() == null) {
            log.warn("缓存的用户完整信息不完整，microRoles={}, microUser={}", microUserCompleteInfo.getMicroRoles(), microUserCompleteInfo.getMicroUser());
            return null;
        }
        Set<String> rolesSet = microUserCompleteInfo.getMicroRoles().stream().map(MicroRole::getRoleCode).collect(Collectors.toSet());

        LoginUser loginUser = new LoginUser();
        loginUser.setUsername(username);
        loginUser.setUserid(microUserCompleteInfo.getMicroUser().getId());
        loginUser.setRoles(rolesSet);
        // SecurityUtils.getUserId() 从 ThreadContext.user_id 读取，decouple 后网关不再注入，在此补齐
        ThreadContext.put(CacheConstants.DETAILS_USER_ID, String.valueOf(microUserCompleteInfo.getMicroUser().getId()));
        return loginUser;
    }

    @Override
    public boolean isSkipRequestFilter(HttpServletRequest request) {
        String applicationSign = request.getHeader(Constants.APPLICATION_SIGN);
        String authorization = request.getHeader(CacheConstants.HEADER);

        // 1.请求应用标识如果非KU易记/工易派，则跳过
        boolean isMicroProcess = CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(applicationSign);
        boolean isMicroPlan = CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(applicationSign);
        if (!StringUtils.hasText(applicationSign) || (!isMicroProcess && !isMicroPlan)) { 
            return true;
        }

        // 2.请求header中未携带token信息，则跳过
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(CacheConstants.TOKEN_PREFIX)) {
            return true;
        }
        return false;
    }

    /**
     * 获取redis的token key
     *
     * @param token
     * @param username
     * @param deviceType
     * @return
     */
    private String getTokenRedisKey(String token, String username, String deviceType, String appSign) {
        return Constants.LOGIN_MICRO_TOKEN_KEY + username + ":" + appSign + ":" + deviceType + ":" + token;
    }

    /**
     * 获取请求token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(CacheConstants.HEADER);
        if (StringUtils.hasText(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.replace(CacheConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 获取用户名
     *
     * @param request
     * @return
     */
    private String getUsername(HttpServletRequest request) {
        String username = request.getHeader(CacheConstants.DETAILS_USERNAME);
        if (!StringUtils.hasText(username)) {
            return null;
        }
        return ServletUtils.urlDecode(username);
    }

    /**
     * DevTools RestartClassLoader 兼容：Redis 反序列化与运行时代码使用不同 ClassLoader 导致 instanceof 失效。
     * 通过 FastJson 序列化后重新解析，确保对象由当前 ClassLoader 加载。
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
}
