/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.utils;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.HttpStatus;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.text.Convert;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.SpringUtils;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.pojo.SysDataAuthority;
import com.cosmo.hhim.common.security.pojo.SysRole;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限获取工具类
 *
 * @author cosmo-hhim-open Team
 */
public class SecurityUtils {
    private static final Logger log = LoggerFactory.getLogger(SecurityUtils.class);

    /**
     * 获取用户
     */
    public static String getUsername() {
        String userName = null;
        if (CheckObjectUtils.isNotEmpty(ServletUtils.getRequestAttributes()) && CheckObjectUtils.isNotEmpty(ServletUtils.getRequest()) && CheckObjectUtils
                .isNotEmpty(ServletUtils.getRequest().getHeader(CacheConstants.DETAILS_USERNAME))) {
            userName = ServletUtils.getRequest().getHeader(CacheConstants.DETAILS_USERNAME);
        }
        if (CheckObjectUtils.isEmpty(userName)) {
            userName = (String) ThreadContext.get(CacheConstants.DETAILS_USERNAME);
        }

        if(StringUtils.isEmpty(userName)){
            return "NA";
        }
        return ServletUtils.urlDecode(userName);
    }

    /**
     * 获取昵称
     */
    public static String getNickName() {
        String nickName = null;
        if (CheckObjectUtils.isNotEmpty(ServletUtils.getRequestAttributes()) && CheckObjectUtils.isNotEmpty(ServletUtils.getRequest()) && CheckObjectUtils
                .isNotEmpty(ServletUtils.getRequest().getHeader(CacheConstants.NICK_NAME))) {
            nickName = ServletUtils.getRequest().getHeader(CacheConstants.NICK_NAME);
        }
        if (CheckObjectUtils.isEmpty(nickName)) {
            nickName = (String) ThreadContext.get(CacheConstants.NICK_NAME);
        }

        if(StringUtils.isEmpty(nickName)){
            return "NA";
        }
        return ServletUtils.urlDecode(nickName);
    }

    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        String userId = null;
        if (CheckObjectUtils.isNotEmpty(ServletUtils.getRequestAttributes()) && CheckObjectUtils.isNotEmpty(ServletUtils.getRequest()) && CheckObjectUtils
                .isNotEmpty(ServletUtils.getRequest().getHeader(CacheConstants.DETAILS_USER_ID))) {
            userId = ServletUtils.getRequest().getHeader(CacheConstants.DETAILS_USER_ID);
        }
        if (CheckObjectUtils.isEmpty(userId)) {
            userId = (String) ThreadContext.get(CacheConstants.DETAILS_USER_ID);
        }
//        Object o = getLoginUser();
//        JSONObject json = (JSONObject) JSON.toJSON(o);
//        Object userId = json.get("userid");
        return Convert.toLong(userId);
    }

    /**
     * 获取试用者手机号
     * @return
     */
    public static String getTrialPhone(){
        String trialPhone = null;
        if (CheckObjectUtils.isNotEmpty(ServletUtils.getRequestAttributes()) && CheckObjectUtils.isNotEmpty(ServletUtils.getRequest()) && CheckObjectUtils
                .isNotEmpty(ServletUtils.getRequest().getHeader(CacheConstants.TRIAL_PHONE))) {
            trialPhone = ServletUtils.getRequest().getHeader(CacheConstants.TRIAL_PHONE);
        }
        if (CheckObjectUtils.isEmpty(trialPhone)) {
            trialPhone = (String) ThreadContext.get(CacheConstants.TRIAL_PHONE);
        }
        return trialPhone;
    }


    /**
     * 根据request获取请求token
     */
    public static String getToken() {
        String token;
        if (ServletUtils.getRequest() == null) {
            token = (String) ThreadContext.get("authorization");
        } else {
            token = ServletUtils.getRequest().getHeader(CacheConstants.HEADER);
        }
        if (StringUtils.isNotEmpty(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.replace(CacheConstants.TOKEN_PREFIX, "");
        }
        return token;
    }

    /**
     * 是否为管理员
     *
     * @param userId 用户ID
     * @return 结果
     */
    public static boolean isAdmin(Long userId) {
        return userId != null && 1L == userId;
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(password);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 获取用户
     **/
    public static LoginUser getLoginUser() {
        try {
            RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
            String name = getUsername();
            String token = getTokenKey(getToken(), name);
            if (StringUtils.isNotEmpty(token)) {
                LoginUser sysUser = (LoginUser) redisTemplate.opsForValue().get(token);
                if (sysUser == null) {
                    log.warn("查询用户token为null,token:{}", token);
                }
                return sysUser;
            }
        } catch (Exception e) {
            log.warn("获取用户信息异常{}", e);
            throw new CustomException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    public static String getTokenKey(String token, String name) {
        return Constants.LOGIN_TOKEN_KEY + name + ":" + token;
    }


    /**
     * 获取数据权限
     **/
    public static List<SysDataAuthority> getAuthority() {
        try {
            RedisTemplate redisTemplate = SpringUtils.getBean("redisTemplate");
            List<SysDataAuthority> authority = (List<SysDataAuthority>) redisTemplate.opsForValue().get(CacheConstants.DATA_AUTHORITY_KEY+getUsername());
                if (authority == null) {
                    log.warn("查询用户数据权限为null,用户名:{}", getUsername());
                    authority = new ArrayList<>();
                }
                return authority;
        } catch (Exception e) {
            log.warn("获取数据权限异常{}", e);
            throw new CustomException("获取数据权限异常", HttpStatus.UNAUTHORIZED);
        }
    }

    public static String getTokenKeyPortal(String token, String name) {
        return CacheConstants.LOGIN_TOKEN_KEY + name + ":" + token;
    }


    /**
     * 获取请求token
     */
    public static String getTokenPortal(HttpServletRequest request) {
        String token = request.getHeader(CacheConstants.HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(CacheConstants.TOKEN_PREFIX)) {
            token = token.replace(CacheConstants.TOKEN_PREFIX, "");
        }
        return token;
    }


    public static SysDataAuthority getUserAuthorityFactory() {
        List<SysDataAuthority> dataAuthority = SecurityUtils.getAuthority().stream().filter(s -> "cim".equals(s.getAppType())).collect(Collectors.toList());
        dataAuthority = dataAuthority.stream().filter(s -> "F".equals(s.getDataType())).collect(Collectors.toList());
        if (dataAuthority.isEmpty() || dataAuthority.size() == 0) {
            throw new CustomException("未获取到工厂信息！！");
        }
        if (dataAuthority.size() > 1) {
            String factoryCode = getHeaderFactory();
            dataAuthority = SecurityUtils.getAuthority().stream().filter(s -> factoryCode.equals(s.getDataCode())).collect(Collectors.toList());
        }
        return dataAuthority.get(0);
    }

    /**
     * @return java.lang.String
     * @author cosmo-hhim-open Team
     * @description 获取Header中工厂信息
     * @date 14:15 2022/1/11
     **/
    public static String getHeaderFactory() {
        String factoryCode = ServletUtils.getHeader(("factoryCode"));
        if(StringUtils.isEmpty(factoryCode) || "undefined".equals(factoryCode)){
            try{
                factoryCode = ThreadContext.get("factoryCode").toString();
            }catch (Exception e){
                throw new CustomException("未获取到工厂信息！！（-）");
            }
        }
        if (StringUtils.isEmpty(factoryCode) || "undefined".equals(factoryCode)) {
            throw new CustomException("未获取到工厂信息！！（-）");
        }
        return factoryCode;
    }

    public static List<SysDataAuthority> getUserAuthority(String AuthType) {
        List<SysDataAuthority> dataAuthority = SecurityUtils.getAuthority().stream().filter(s -> "cim".equals(s.getAppType())).collect(Collectors.toList());
        if (null != AuthType) {
            dataAuthority = dataAuthority.stream().filter(s -> AuthType.equals(s.getDataType())).collect(Collectors.toList());
        }
        return dataAuthority;
    }

    public static boolean isDataScope4th(){
        List<SysRole> loginUser = SecurityUtils.getLoginUser().getSysUser().getRoles();
        for (SysRole r : loginUser) {
            if(!"4".equals(r.getDataScope())){
                return false;
            }
        }
        return true;
    }

    public static String getApplicationSign(){
        String appSign = null;
        if (CheckObjectUtils.isNotEmpty(ServletUtils.getRequestAttributes()) && CheckObjectUtils.isNotEmpty(ServletUtils.getRequest()) && CheckObjectUtils
                .isNotEmpty(ServletUtils.getRequest().getHeader(Constants.APPLICATION_SIGN))) {
            appSign = ServletUtils.getRequest().getHeader(Constants.APPLICATION_SIGN);
        }
        if (CheckObjectUtils.isEmpty(appSign)) {
            appSign = (String) ThreadContext.get(Constants.APPLICATION_SIGN);
        }
        return appSign;
    }

}
