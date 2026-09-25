/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

/**
 * 通用常量信息
 *
 * @author cosmo-hhim-open Team
 */
public class Constants {
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 成功标记
     */
    public static final Integer SUCCESS = 200;

    /**
     * 失败标记
     */
    public static final Integer FAIL = 500;

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 当前记录起始索引-feign
     */
    public static final String PAGE_NUM_FEIGN = "pageNumFeign";

    /**
     * 每页显示记录数-feign
     */
    public static final String PAGE_SIZE_FEIGN = "pageSizeFeign";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或者 "asc".
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 验证码有效期（分钟）
     */
    public static final long CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌有效期（分钟）
     */
    public final static long TOKEN_EXPIRE = 10080;

    /**
     * 手机令牌有效期（分钟）
     */
    public final static long MOBILE_TOKEN_EXPIRE = 10080;

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config_portal:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict_portal:";

    /**
     * 资源映射路径 前缀
     */
    public static final String RESOURCE_PREFIX = "/profile";

    public static final String TARGET_DS = "target_ds";
    public static final String TARGET_SCHEMA = "target_schema";
    public static final String TARGET_CUSTOMER = "target_customer";
    public static final String TARGET_CUSTOMER_NAME = "target_customer_name";
    public static final String NICKNAME = "nickname";
    public static final String WXMP_OPENID = "wxMp_openId";
    public static final String WXMP_APPID = "wxMp_appId";
    public static final String PLATFORM_TYPE = "platform_type"; // 平台类型
    public static final String APPLICATION_SIGN = "application_sign"; // 应用标识

    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";
    public static final String LOGIN_MICRO_TOKEN_KEY = "micro_tokens:";

    /**
     * 登录用户 code
     */
    public static final String CHECK_TOKEN = "check_token:";

    public static final String TRACEID = "traceId";

    public static final String SKYWALKING_TRACEID = "skywalking_traceId";

    /**
     * 二维码内容分割符
     */
    public static final String QC_SPLIT = "#!";


    /**
     * 在当前业务是否是超级管理员
     */
    public static final String MAIN_ACCOUNT_FLAG = "mainAccountFlag";

    /**
     * 环境
     */
    public static final String PROFILES_ACTIVE_DEV = "dev";
    public static final String PROFILES_ACTIVE_TEST = "test";
    public static final String PROFILES_ACTIVE_PRE = "pre";
    public static final String PROFILES_ACTIVE_PROD = "prod";

    /**
     * 是否完成了访问权限filter过滤
     */
    public static final String DO_AUTH_FILTER = "doAuthFilter";

    public static final String NO_AUTH_ERROR = "noAuthError";

    public static final String EXPORT_LOCK = "async:export:";
    public static final String IMPORT_LOCK = "async:import:";
}
