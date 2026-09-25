/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 邀请码加入租户入参（安全版本）
 *
 * <p>decouple-from-ops-platform-cleanup (C.17)：不允许前端传 phone / openId。
 * 后端 wxMiniAppLogin 已通过微信 API 验证过手机号和 openId，结果存在 Redis session
 * （key: {@code wxLogin:verified:<sessionId>}），join-tenant 直接从 session 读。
 *
 * <p>邀请码本身在 Redis 里（{@code inviteCode:<code>} → MicroInviteCodeInfo，
 * 由 MicroLoginServiceImpl.genInviteCode 写入），里面带 customerCode / databaseName /
 * dataSource / roleCode / appCode。
 *
 * <p>安全属性：
 * <ul>
 *   <li>前端无法伪造手机号 / openId</li>
 *   <li>无法跳过微信授权直接用邀请码</li>
 *   <li>session 5 min TTL，一次性消费（消费后立即删）</li>
 * </ul>
 *
 * @author cosmo-hhim-open Team
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JoinTenantByInviteParam {
    /** 微信登录会话 ID（wxMiniAppLogin 0-租户场景下返的 sessionId） */
    private String sessionId;
    /** 邀请码（6 位） */
    private String inviteCode;
    /** 用户在目标租户中的姓名（昵称） */
    private String nickName;
    /** 应用标识（前端从 manifest 读出，传 body） */
    private String applicationSign;
    /** 平台类型（前端从 systemInfo 读出，传 body） */
    private String platformType;
}
