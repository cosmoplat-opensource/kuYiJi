/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import com.cosmo.hhim.common.core.constant.enums.CommonConstant;
import com.cosmo.hhim.common.core.domain.JwtTokenData;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.exception.JwtNotLoginException;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 */
public class JwtUtil {

    // key：账号id
    public static final String LOGIN_ID = "loginId";

    // 登录设备类型
    public static final String DEVICE_TYPE = "deviceType";

    // key：有效截止期 (时间戳)
    public static final String EFF = "eff";

    // 随机值
    public static final String RANDOM = "rd";

    // 当有效期被设为此值时，代表永不过期
    public static final long NEVER_EXPIRE = -1;

    // 表示系统中不存在这个缓存 (在对不存在的key获取剩余存活时间时返回此值)
    public static final long NOT_VALUE_EXPIRE = -2;

    // JWT 密钥最小长度（字节）：过短易被暴力破解，建议不少于 32 字符
    public static final int MIN_SECRET_LENGTH = 32;

    /**
     * 校验 JWT 密钥：非空且长度不少于 {@link #MIN_SECRET_LENGTH}，否则直接抛出异常（fail-fast）
     */
    public static void validateSecret(String keyt) {
        if (!StringUtils.hasText(keyt) || keyt.getBytes().length < MIN_SECRET_LENGTH) {
            throw new CustomException("请配置jwt秘钥（长度不少于" + MIN_SECRET_LENGTH + "字符）");
        }
    }

    // ------------------------------------------- 创建token -----------------------------------------------------------

    /**
     * 创建jwt token（简单业务封装）
     */
    public static <T extends JwtTokenData> String createToken(T t, String keyt) {
        validateSecret(keyt);
        return createToken(t.getDeviceType().getKey(), t.getLoginId(), t.getTimeout(), MapBeanUtil.beanToMap(t), keyt);
    }

    /**
     * 创建jwt token
     *
     * @param loginId    账号id
     * @param deviceType 设备类型
     * @param timeout    token有效期 (单位：s)
     * @param extraData  扩展数据
     * @param keyt       秘钥
     * @return jwt-token
     */
    public static String createToken(String deviceType, Object loginId, long timeout, Map<String, Object> extraData, String keyt) {
        validateSecret(keyt);

        // 计算有效期
        long effTime = timeout;
        if (timeout != NEVER_EXPIRE) {
            effTime = timeout * 1000 + System.currentTimeMillis();
        }

        // 创建token
        return JWT.create()
                .setPayload(LOGIN_ID, loginId)
                .setPayload(DEVICE_TYPE, deviceType)
                .setPayload(EFF, effTime)
                .setPayload(RANDOM, getRandomString(32)) // 混入随机字符
                .addPayloads(extraData)
                .setKey(keyt.getBytes()).sign();
    }

    // ----------------------------------------------- 解析token ---------------------------------------------------------

    /**
     * 获取jwt数据载荷（简单业务类型封装）
     */
    public static <T extends JwtTokenData> T getJwtTokenData(String token, String keyt, Class<T> clazz) {
        JSONObject payloads = getPayloads(token, keyt);
        return payloads.toBean(clazz);
    }

    /**
     * jwt解析（校验签名和有效期）
     *
     * @param token Jwt-Token值
     * @param keyt  秘钥
     * @return 解析后的jwt 对象
     */
    public static JWT parseToken(String token, String keyt) {
        if (token == null) {
            throw JwtNotLoginException.newInstance(null, JwtNotLoginException.NOT_TOKEN);
        }

        // 解析
        JWT jwt = null;
        try {
            jwt = JWT.of(token);
        } catch (JWTException e) {
            throw JwtNotLoginException.newInstance(null, JwtNotLoginException.INVALID_TOKEN, token);
        }
        JSONObject payloads = jwt.getPayloads();

        // 校验Token签名
        if (!jwt.setKey(keyt.getBytes()).verify()) {
            throw JwtNotLoginException.newInstance(payloads.getStr(DEVICE_TYPE), JwtNotLoginException.INVALID_TOKEN, token);
        }

        // 校验Token有效期
        Long effTime = payloads.getLong(EFF, 0L);
        if (effTime != NEVER_EXPIRE) {
            if (effTime < System.currentTimeMillis()) {
                throw JwtNotLoginException.newInstance(payloads.getStr(DEVICE_TYPE), JwtNotLoginException.TOKEN_TIMEOUT, token);
            }
        }

        return jwt;
    }

    /**
     * 获取jwt数据载荷 （校验签名和有效期）
     *
     * @param token token值
     * @param keyt  秘钥
     * @return 载荷
     */
    public static JSONObject getPayloads(String token, String keyt) {
        return parseToken(token, keyt).getPayloads();
    }

    /**
     * 获取jwt数据载荷 （只校验签名，不校验有效期）
     *
     * @param token token值
     * @param keyt  秘钥
     * @return 载荷
     */
    public static JSONObject getPayloadsNotCheck(String token, String keyt) {
        try {
            JWT jwt = JWT.of(token);
            JSONObject payloads = jwt.getPayloads();

            // 校验Token签名
            validateSecret(keyt);
            if (!jwt.setKey(keyt.getBytes()).verify()) {
                throw JwtNotLoginException.newInstance(payloads.getStr(DEVICE_TYPE), JwtNotLoginException.INVALID_TOKEN, token);
            }

            return payloads;
        } catch (JWTException e) {
            return new JSONObject();
        }
    }

    /**
     * 获取jwt代表的账号id
     *
     * @param token Token值
     * @param keyt  秘钥
     * @return 值
     */
    public static Object getLoginId(String token, String keyt) {
        return getPayloads(token, keyt).get(LOGIN_ID);
    }

    /**
     * 获取jwt剩余有效期
     *
     * @param token JwtToken值
     * @param keyt  秘钥
     * @return 值
     */
    public static long getTimeout(String token, String keyt) {
        if (token == null) {
            return NOT_VALUE_EXPIRE;
        }

        // 解析数据
        JWT jwt = null;
        try {
            jwt = JWT.of(token);
        } catch (JWTException e) {
            return NOT_VALUE_EXPIRE;
        }
        JSONObject payloads = jwt.getPayloads();

        // 签名无效
        validateSecret(keyt);
        if (!jwt.setKey(keyt.getBytes()).verify()) {
            return NOT_VALUE_EXPIRE;
        }

        // 被设置为：永不过期
        Long effTime = payloads.get(EFF, Long.class);
        if (effTime == NEVER_EXPIRE) {
            return NEVER_EXPIRE;
        }
        // 已经超时
        if (effTime < System.currentTimeMillis()) {
            return NOT_VALUE_EXPIRE;
        }

        // 计算timeout (转化为以秒为单位的有效时间)
        return (effTime - System.currentTimeMillis()) / 1000;
    }

    /**
     * 生成指定长度的随机字符串
     *
     * @param length 字符串的长度
     * @return 一个随机字符串
     */
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

}
