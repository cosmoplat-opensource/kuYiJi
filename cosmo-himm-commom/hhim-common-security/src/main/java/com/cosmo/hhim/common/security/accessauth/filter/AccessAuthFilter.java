/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.accessauth.filter;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.MediaType;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.accessauth.cache.ControllerMethodsCache;
import com.cosmo.hhim.common.security.accessauth.request.wrapper.OverrideParamAndHeaderRequestWrapper;
import com.cosmo.hhim.common.security.accessauth.request.wrapper.ReuseHttpRequest;
import com.cosmo.hhim.common.security.accessauth.request.wrapper.ReuseHttpServletRequest;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.cosmo.hhim.common.core.constant.CacheConstants.REQUEST_PATH_SEPARATOR;


/**
 * @author cosmo-hhim-open Team
 * @description 请求访问控制servlet filter
 * @createTime 2022-10-28
 */
@Slf4j
public class AccessAuthFilter implements Filter {

    @Autowired
    private ControllerMethodsCache controllerMethodsCache;

    @Autowired
    private CustomizedAccessAuthFilterCondition customizedAccessAuthFilterCondition;

    @Override
    public void destroy() {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ReuseHttpRequest request = new ReuseHttpServletRequest((HttpServletRequest) servletRequest);
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 打印请求日志
        printRequestLog(request);

        // 拿到请求URL所映射的Controller的方法
        Method method = controllerMethodsCache.getMethod(request);

        // 请求过滤
        if (customizedAccessAuthFilterCondition.isSkipRequestFilter(request) || null == method
                || (!method.isAnnotationPresent(AccessAuth.class) && !method.getDeclaringClass().isAnnotationPresent(AccessAuth.class))) {
            OverrideParamAndHeaderRequestWrapper requestWrapper = OverrideParamAndHeaderRequestWrapper.buildRequest(request);
            requestWrapper.addParameter(Constants.DO_AUTH_FILTER, "false");
            filterChain.doFilter(requestWrapper, response);
        } else {
            List<String> allowAccessRoles = getAllowAccessRoles(method);
            LoginUser loginUser = customizedAccessAuthFilterCondition.getLoginUser(request);
            if (null == loginUser) {
                log.warn("访问权限控制----->当前登录态无效或已过期！");
                responseLoginExpired(response);
            } else if (CollectionUtils.isEmpty(allowAccessRoles) || !isContainAccessRole(loginUser, allowAccessRoles)) {
                log.warn("访问权限控制----->当前登录用户身份不具备此接口访问权限！当前用户username：{}, 当前用户身份集合：{}, 所访问方法所需身份权限：{}",
                        loginUser.getUsername(), JSON.toJSONString(loginUser.getRoles()), JSON.toJSONString(allowAccessRoles));
                responseNoAccessAuth(response);
            } else {
                log.info("访问权限控制----->接口访问权限认证通过！当前用户username：{}, 当前用户身份集合：{}, 所访问方法所需身份权限：{}",
                        loginUser.getUsername(), JSON.toJSONString(loginUser.getRoles()), JSON.toJSONString(allowAccessRoles));

                OverrideParamAndHeaderRequestWrapper requestWrapper = OverrideParamAndHeaderRequestWrapper.buildRequest(request);
                requestWrapper.addParameter(Constants.DO_AUTH_FILTER, "true");
                filterChain.doFilter(requestWrapper, response);
            }
        }
    }

    /**
     * 判断当前登录用户是否包含可访问的角色
     *
     * @param loginUser
     * @param allowAccessRoles
     * @return
     */
    private boolean isContainAccessRole(LoginUser loginUser, List<String> allowAccessRoles) {
        if (null == loginUser || CollectionUtils.isEmpty(loginUser.getRoles())) {
            log.warn("当前登录用户缺失权限认证判断依据核心数据！！！");
            return false;
        }
        for (String allowAccessRole : allowAccessRoles) {
            for (String roleKey : loginUser.getRoles()) {
                if (roleKey.equals(allowAccessRole)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取允许访问的角色集合
     *
     * @param method
     * @return
     */
    private List<String> getAllowAccessRoles(Method method) {
        // 桥接方法
        final Method userDeclaredMethod = BridgeMethodResolver.findBridgedMethod(method);

        // 获取最终执行方法上的AccessAuth注解属性信息
        AccessAuth accessAuth = AnnotatedElementUtils.findMergedAnnotation(userDeclaredMethod, AccessAuth.class);
        if (accessAuth == null) {
            accessAuth = AnnotatedElementUtils.findMergedAnnotation(userDeclaredMethod.getDeclaringClass(), AccessAuth.class);
        }
        return Arrays.asList(accessAuth.allowAccessRoles());
    }

    /**
     * 设置无权访问响应信息
     *
     * @param response
     * @throws IOException
     */
    private void responseNoAccessAuth(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 等价于 setContentType("application/json")（静态扫描"JavaScript劫持"规则可识别形式）
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON);
        // JavaScript劫持防护：禁止浏览器对 JSON 响应做 MIME 嗅探/脚本加载
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getWriter().write(JSON.toJSONString(AjaxResult.error(911, "当前登录用户无权访问！")));
    }

    private void responseLoginExpired(HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // 等价于 setContentType("application/json")（静态扫描"JavaScript劫持"规则可识别形式）
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON);
        // JavaScript劫持防护：禁止浏览器对 JSON 响应做 MIME 嗅探/脚本加载
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.getWriter().write(JSON.toJSONString(AjaxResult.error(408, "登录已过期，请重新登录")));
    }


    /**
     * 获取请求的URL信息
     *
     * @param request
     * @return
     */
    private String getPath(HttpServletRequest request) {
        // 直接取请求路径（getRequestURI 本身不含查询串，避免 new URI 解析带来的注入面）
        return request.getRequestURI();
    }

    /**
     * 打印web请求信息
     * @param request
     */
    private void printRequestLog(ReuseHttpRequest request) {
        try {
            String requestPath = request.getMethod() + REQUEST_PATH_SEPARATOR + getPath(request);
            String requestBody = JSON.toJSONString(request.getBody());
            log.info("web请求信息======》requestPath:{}, requestBody:{}", requestPath, requestBody);
        } catch (Exception exception) {
            log.error("access auth filter error", exception);
        }

    }
}
