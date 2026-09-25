/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.constant;

/**
 * 缓存的key 常量
 *
 * @author cosmo-hhim-open Team
 */
public class CacheConstants
{
    /**
     * 令牌自定义标识
     */
    public static final String HEADER = "Authorization";

    /**
     * 移动端令牌自定义标识
     */
    public static final String MOBILEHEADER = "MobileAuthorization";


    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "login_tokens:";


    /**
     * 用户ID字段
     */
    public static final String DETAILS_USER_ID = "user_id";

    /**
     * 用户名字段
     */
    public static final String DETAILS_USERNAME = "username";

    /**
     * 工厂
     */
    public static final String FACTORY_CODE = "factoryCode";

    /**
     * 请求类型
     */
    public static final String DETAILS_TYPE = "type";

    /**
     * 授权信息字段
     */
    public static final String AUTHORIZATION_HEADER = "authorization";

    /**
     * 昵称
     */

    public static final String NICK_NAME = "nickname";

    /**
     * 请求类型
     */
    public static final String TYPE = "type";


    /**
     * 仓库编码
     */
    public static final String WAREHOUSE_CODE = "whCode";


    /**
     * 数据权限权限缓存前缀
     */
    public final static String DATA_AUTHORITY_KEY = "data_authority:";

    /**
     * 本地多行业语言属性前缀
     */
    public final static String MULTI_LANG_KEY = "multi_lang_key:";

    /**
     * 本地多行业语言属性前缀
     */
    public final static String THIRD_INTERFACE_KEY = "third_interface_key:";


    public static final String REQUEST_PATH_SEPARATOR = "-->";

    /**
     * 试用者手机号
     */
    public static final String TRIAL_PHONE = "trial_phone";

}
