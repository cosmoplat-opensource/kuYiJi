/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.security.handler.GlobalExceptionHandler;
import com.cosmo.hhim.common.security.pojo.LoginUser;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.utils.ip.IpUtils;
import com.cosmo.hhim.common.redis.service.RedisService;


/**
 * token验证处理
 * 
 * @author cosmo-hhim-open Team
 */
@Component
public class TokenService
{
    @Autowired
    private RedisService redisService;

    private final static long EXPIRE_TIME = Constants.TOKEN_EXPIRE * 600;

    private final static long MOBILE_EXPIRE_TIME = Constants.MOBILE_TOKEN_EXPIRE * 600;

    private final static String ACCESS_TOKEN = CacheConstants.LOGIN_TOKEN_KEY;



    protected static final long MILLIS_SECOND = 1000;
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    /**
     * 创建令牌
     */
    public Map<String, Object> createToken(LoginUser loginUser,String loginType)
    {
        if(StringUtils.isEmpty(loginUser.getSysUser().getUserName())){
            throw new CustomException("登录生成token时 用户名为空");
        }
        // 生成token
        String token = IdUtils.fastUUID();
        loginUser.setToken(token);
        loginUser.setOs(loginType);
        loginUser.setUserid(loginUser.getSysUser().getUserId());
        loginUser.setUsername(loginUser.getSysUser().getUserName());
        loginUser.setIpaddr(IpUtils.getIpAddr(ServletUtils.getRequest()));
        loginUser.setLoginType(loginType);
        refreshToken(loginUser);
        String checkCode = IdUtils.fastUUID();
        // 保存或更新用户token
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("access_token", token);
        map.put("expires_in", EXPIRE_TIME);
        map.put("username", loginUser.getSysUser().getUserName());
        map.put("target_customer", loginUser.getCustomer());
        map.put("check_code",checkCode);
        if(CommonConstant.DeviceType.PC.getKey().equals(loginType)){
            redisService.setCacheObject(Constants.CHECK_TOKEN+checkCode,loginUser,1L,TimeUnit.MINUTES);
        }
        redisService.setCacheObject(ACCESS_TOKEN+loginUser.getSysUser().getUserName()+":" + token, loginUser, MOBILE_EXPIRE_TIME, TimeUnit.SECONDS);
        return map;
    }



    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request)
    {
        return  SecurityUtils.getLoginUser();
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser)
    {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken()))
        {
            refreshToken(loginUser);
        }
    }

    public void delLoginUser(String token)
    {
        if (StringUtils.isNotEmpty(token))
        {
            String userKey = getTokenKey(token,SecurityUtils.getUsername());
            redisService.deleteObject(userKey);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser)
    {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + EXPIRE_TIME * MILLIS_SECOND);
    }

    private String getTokenKey(String token,String name)
    {
        return ACCESS_TOKEN +name+":"+ token;
    }






}