/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.filter;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.redis.service.RedisCache;
import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.micro.base.domain.entity.common.MicroUserCompleteInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在 AccessAuthFilter 之前运行，从 Redis token 解析 userId 并注入到请求头。
 *
 * <p>decouple 后网关注入 user_id 失效，WebFilter.preHandle 读不到 user_id
 * 导致 DBControlUtil.setDbAndSchema 传 0L 覆盖 ThreadContext。
 *
 * <p>此 Filter 在请求进入 AccessAuthFilter 之前补齐 user_id header，
 * 后续所有组件（AccessAuthFilter、WebFilter、SecurityUtils）都能正常读到。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@Order(-1000) // 在 AccessAuthFilter 之前执行 
public class UserContextFilter implements Filter {

    private final RedisCache redisCache;

    public UserContextFilter(RedisCache redisCache) {
        this.redisCache = redisCache;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletRequest wrappedRequest = wrapWithUserId(request);
        filterChain.doFilter(wrappedRequest, servletResponse);
    }

    private HttpServletRequest wrapWithUserId(HttpServletRequest request) {
        // 非本应用请求 → 透传
        String appSign = request.getHeader(Constants.APPLICATION_SIGN);
        boolean isMicroProcess = CommonConstant.ApplicationSignEnum.MICRO_PROCESS.getKey().equals(appSign);
        boolean isMicroPlan = CommonConstant.ApplicationSignEnum.MICRO_PLAN.getKey().equals(appSign);
        if (!StringUtils.hasText(appSign) || (!isMicroProcess && !isMicroPlan)) { 
            return request;
        }

        // 获取 Bearer token
        String authorization = request.getHeader(CacheConstants.HEADER);
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(CacheConstants.TOKEN_PREFIX)) {
            return request;
        }
        String token = authorization.substring(CacheConstants.TOKEN_PREFIX.length());

        // 读取其他 key 元素
        String username = request.getHeader(CacheConstants.DETAILS_USERNAME);
        if (!StringUtils.hasText(username)) {
            return request;
        }
        String type = request.getHeader(CacheConstants.DETAILS_TYPE);
        if (!StringUtils.hasText(type)) {
            return request;
        }

        // 从 Redis 读取 token 解析 userId。
        // 安全：永远以服务端 Redis 中已验证的 token 为准，不信任客户端传入的任何 user_id。
        try {
            String redisKey = Constants.LOGIN_MICRO_TOKEN_KEY + ServletUtils.urlDecode(username)
                    + ":" + appSign + ":" + type + ":" + token;
            Object cacheObject = redisCache.getCacheObject(redisKey);
            MicroUserCompleteInfo info = resolveCacheObject(cacheObject);
            if (info != null && info.getMicroUser() != null && info.getMicroUser().getId() != null) {
                String userId = String.valueOf(info.getMicroUser().getId());
                // 始终包装请求，用 token 中的 userId 覆盖客户端可能伪造的值
                return new UserIdHeaderRequestWrapper(request, userId);
            }
        } catch (Exception e) {
            log.debug("UserContextFilter: 解析 userId 失败: {}", e.getMessage());
        }
        return request;
    }

    /**
     * 简单的 HttpServletRequestWrapper，只覆盖 getHeader 相关方法注入 user_id。
     */
    private static class UserIdHeaderRequestWrapper extends HttpServletRequestWrapper {
        private final String userId;

        UserIdHeaderRequestWrapper(HttpServletRequest request, String userId) {
            super(request);
            this.userId = userId;
        }

        @Override
        public String getHeader(String name) {
            if (CacheConstants.DETAILS_USER_ID.equals(name)) {
                return userId;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (CacheConstants.DETAILS_USER_ID.equals(name)) {
                return Collections.enumeration(Collections.singletonList(userId));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            if (!names.contains(CacheConstants.DETAILS_USER_ID)) {
                names.add(CacheConstants.DETAILS_USER_ID);
            }
            return Collections.enumeration(names);
        }
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
        // ClassLoader 不一致时，JSON 中转让 FastJson 用当前 ClassLoader 重新加载
        try {
            String json = JSON.toJSONString(cacheObject);
            return JSON.parseObject(json, MicroUserCompleteInfo.class);
        } catch (Exception e) {
            log.warn("resolveCacheObject failed: {}", e.getMessage());
            return null;
        }
    }
}
