/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.constant;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/22
 */
public class RedisKeys {

    public static final String INVITE_CODE = "inviteCode:";
    public static final String IMPORT_CERTIFICATE_CODE = "importCertificateCode:";
    public static final String LAST_TRIGGER_DATE = "lastTriggerDate:";
    public static final String TODAY_FIRST_ENTER_PROGRAM = "todayFirstEnterProgram:";
    public static final String LOCK = "lock:";

    // 登录 token 统一使用 Constants.LOGIN_MICRO_TOKEN_KEY = "login:token:"
    // 格式: login:token:{username}:{appSign}:{deviceType}:{token}
    // 由 MicroAccessAuthFilterCondition 读取，MicroAuthServiceImpl.issueTokenAndBuildResult 写入

    // decouple-from-ops-platform：多租户选择临时 token 
    public static final String TEMP_TOKEN = "micro_v2:temp:";

    // CertificateInterceptor 用于 PC 端凭证存储（非登录 token） 
    public static final String CERTIFICATE_TOKEN = "micro_v2:login:";

    // decouple-from-ops-platform-cleanup (C.16): 微信登录验证通过后保存的 session（5 min）
    // 供 register-tenant 读手机号用 —— 前端不传 phone，避免伪造 
    public static final String WX_LOGIN_VERIFIED = "wxLogin:verified:";

    // 短信验证码登录：验证码缓存（5 min TTL） 
    public static final String SMS_LOGIN_CODE = "smsLogin:code:";

    /**
     * 在制品库存-微应用业务域划分相关
     */
    public enum MicroRegion { 
        // 登陆模块
        LOGIN_MODULE("micro:login:"),
        // 内容提示模块
        CONTENT_TIP_MODULE("micro:contentTip:"),
        // 产成品库存模块
        FINISH_STORAGE_MODULE("micro:finishStorage:"),
        // 工序库存模块
        PROCESS_STORAGE_MODULE("micro:processStorage:"),
        // 基础模块
        BASIC_MODULE("micro:basic:");

        private String key;

        MicroRegion(String key) {
            this.key = key;

        }

        public String value(String value) {
            return this.key + value;
        }
    }

}
