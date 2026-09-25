/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.text.Convert;

/**
 * 客户端工具类
 *
 * @author cosmo-hhim-open Team
 */
public class ServletUtils
{
    /**
     * 获取String参数
     */
    public static String getParameter(String name)
    {
        if(getRequest()!=null){
            return getRequest().getParameter(name);
        }else{
            return "";
        }

    }

    /**
     * 获取String参数
     */
    public static String getParameter(String name, String defaultValue)
    {
        if(getRequest()!=null){
            return Convert.toStr(getRequest().getParameter(name), defaultValue);
        }else{
            return "";
        }

    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name)
    {
        if(getRequest()!=null){
            return Convert.toInt(getRequest().getParameter(name));
        }else{
            return 0;
        }

    }

    /**
     * 获取Integer参数
     */
    public static Integer getParameterToInt(String name, Integer defaultValue)
    {
        if(getRequest()!=null){
            return Convert.toInt(getRequest().getParameter(name), defaultValue);
        }else{
            return 0;
        }

    }

    /**
     * 获取request
     */
    public static HttpServletRequest getRequest() throws NullPointerException {
        if (getRequestAttributes() == null) {
            return null;
        }
        return getRequestAttributes().getRequest();
    }

    /**
     * 获取response
     */
    public static HttpServletResponse getResponse()
    {
        if(getRequestAttributes()!=null){
            return getRequestAttributes().getResponse();
        }
        return null;
    }

    /**
     * 获取session
     */
    public static HttpSession getSession()
    {
        return getRequest().getSession();
    }

    public static ServletRequestAttributes getRequestAttributes()throws NullPointerException
    {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
    }

    public static Map<String, String> getHeaders(HttpServletRequest request)
    {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null)
        {
            while (enumeration.hasMoreElements())
            {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将字符串渲染到客户端
     *
     * @param response 渲染对象
     * @param string 待渲染的字符串
     * @return null
     */
    public static String renderString(HttpServletResponse response, String string)
    {
        try
        {
            response.setStatus(200);
            // 等价于 setContentType("application/json")（静态扫描"JavaScript劫持"规则可识别形式）
            response.setHeader("Content-Type", "application/json");
            response.setCharacterEncoding("utf-8");
            // JavaScript劫持防护：禁止浏览器对 JSON 响应做 MIME 嗅探/脚本加载
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.getWriter().print(string);
        }
        catch (IOException e)
        {

        }
        return null;
    }

    /**
     * 是否是Ajax异步请求
     *
     * @param request
     */
    public static boolean isAjaxRequest(HttpServletRequest request)
    {
        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1)
        {
            return true;
        }

        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.indexOf("XMLHttpRequest") != -1)
        {
            return true;
        }

        String uri = request.getRequestURI();
        if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml"))
        {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        if (StringUtils.inStringIgnoreCase(ajax, "json", "xml"))
        {
            return true;
        }
        return false;
    }

    /**
     * 内容编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str)
    {
        try
        {
            return URLEncoder.encode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    /**
     * 内容解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str)
    {
        try
        {
            return URLDecoder.decode(str, Constants.UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            return "";
        }
    }

    /**
     * 获取header
     */
    public static String getHeader(String name) {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes == null ? null : attributes.getRequest().getHeader(name);
    }

    public static String getUserName() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes == null ? null : attributes.getRequest().getHeader(CacheConstants.DETAILS_USERNAME);
    }

    public static String getNickName() {
        ServletRequestAttributes attributes = getRequestAttributes();
        return attributes == null ? null : urlDecode(attributes.getRequest().getHeader(CacheConstants.NICK_NAME));
    }

    public static String getToken() {
        String token = getHeader(CacheConstants.HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.replace(CacheConstants.TOKEN_PREFIX, "");
        }
        return token;
    }


}
