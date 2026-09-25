/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 自助注册租户入参（安全版本）
 *
 * <p>decouple-from-ops-platform-cleanup (C.16)：不允许前端传 phone / smsCode。
 * 后端 wxMiniAppLogin 已通过微信 API 验证过手机号，验证结果存在 Redis session
 * （key: {@code wxLogin:verified:<sessionId>}），register-tenant 直接从 session 读。
 *
 * <p>这样：
 * <ul>
 *   <li>前端无法伪造任何手机号</li>
 *   <li>无法跳过微信授权直接注册</li>
 *   <li>smsCode 不再需要（dev mock 也不需要）</li>
 * </ul>
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterTenantParam {
    /** 微信登录会话 ID（wxMiniAppLogin 0-租户场景下返的 sessionId） */
    private String sessionId;
    private String tenantName;
    private String customerName;
    /** 应用标识（前端从 manifest 读出，传 body） */
    private String applicationSign;
    /** 平台类型（前端从 systemInfo 读出，传 body） */
    private String platformType;
}
