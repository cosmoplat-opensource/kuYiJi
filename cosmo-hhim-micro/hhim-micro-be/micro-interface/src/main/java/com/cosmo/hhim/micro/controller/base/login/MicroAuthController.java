/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.base.login;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.common.security.utils.SecurityUtils;
import com.cosmo.hhim.micro.base.domain.entity.common.JoinTenantByInviteParam;
import com.cosmo.hhim.micro.base.domain.entity.common.RegisterTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SelectTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.SwitchTenantParam;
import com.cosmo.hhim.micro.base.domain.entity.common.UserBaseInfo;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroAuthService;
import com.cosmo.hhim.micro.base.domain.service.common.IMicroTenantService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static com.cosmo.hhim.micro.infrastructure.util.RequestUtils.extractRequestHeader;

/**
 * decouple-from-ops-platform：本地化认证新流程
 *
 * - POST /login/select-tenant         多租户选择
 * - POST /login/register-tenant       自助注册开租户
 * - POST /login/register-tenant/invite 邀请码加入现有租户
 * - POST /login/switch-tenant         已登录用户切换租户
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@RestController
@RequestMapping("/login")
@RequiredArgsConstructor
public class MicroAuthController {

    /**
     * 表单/查询参数绑定防护：禁止绑定 class 相关属性（防 Mass Assignment / 类型混淆攻击）
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("class.*", "*.class");
    }

    private final IMicroAuthService microAuthService;

    private final IMicroTenantService microTenantService;

    @PostMapping("/select-tenant")
    public AjaxResult selectTenant(@Valid @RequestBody SelectTenantParam param) {
        log.info("select-tenant param: {}", param);
        UserBaseInfo result = microAuthService.selectTenant(param);
        return AjaxResult.success(result);
    }

    @PostMapping("/register-tenant")
    public AjaxResult registerTenant(HttpServletRequest request,
                                     @Valid @RequestBody RegisterTenantParam param) {
        log.info("register-tenant param: {}", param);
        Map<String, String> headerMap = buildAuthHeaderMap(request);
        UserBaseInfo result = microAuthService.registerTenant(param, headerMap);
        return AjaxResult.success(result);
    }

    /**
     * decouple-from-ops-platform-cleanup (C.17): 邀请码加入现有租户
     *
     * <p>安全：手机号 / openid 优先从 session 读，已登录用户从 Redis token 获取。
     * 邀请码对应租户从 Redis 读。
     */
    @PostMapping("/register-tenant/invite")
    public AjaxResult registerTenantByInvite(HttpServletRequest request,
                                             @Valid @RequestBody JoinTenantByInviteParam param) {
        log.info("register-tenant/invite param: {}", param);
        Map<String, String> headerMap = buildAuthHeaderMap(request);
        UserBaseInfo result = microAuthService.joinTenantByInvite(param, headerMap);
        return AjaxResult.success(result);
    }

    /**
     * 已登录用户切换租户
     *
     * <p>手动校验 token（不走 @AccessAuth），避免 AccessAuthFilter 预设 ThreadContext
     * 导致 SchemaIntercept 读到旧租户，与 selectTenant / wxMiniAppLoginV2 模式一致。
     */
    @PostMapping("/switch-tenant")
    public AjaxResult switchTenant(HttpServletRequest request,
                                   @Valid @RequestBody SwitchTenantParam param) {
        log.info("switch-tenant param: {}", param);
        Map<String, String> headerMap = buildAuthHeaderMap(request);
        UserBaseInfo result = microAuthService.switchTenant(headerMap, param);
        log.info("switch-tenant result: userName={}, roleCode={}", result.getUserName(), result.getRoleCode());
        return AjaxResult.success(result);
    }

    /**
     * 解散租户（仅企业管理员可操作）
     *
     * <p>将租户状态标记为停用，清理用户-租户索引及 Redis token。
     * 操作人必须是该租户的管理员角色。
     */
    @PostMapping("/tenant/dissolve")
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE})
    public AjaxResult dissolveTenant(HttpServletRequest request) {
        String tenantCode = String.valueOf(ThreadContext.get(Constants.TARGET_CUSTOMER));
        String operatedBy = SecurityUtils.getUserId().toString();
        log.info("解散租户>>> tenantCode:{}, operatedBy:{}", tenantCode, operatedBy);
        microTenantService.dissolve(tenantCode, operatedBy);
        log.info("解散租户>>> 完成 tenantCode:{}", tenantCode);
        return AjaxResult.success();
    }

    /**
     * 构建完整的认证 headerMap（含 Authorization + username + type + application_sign）。
     * 如果请求中没有 Authorization header（未登录），返回 null。
     *
     * @return headerMap（已登录）或 null（未登录）
     */
    private Map<String, String> buildAuthHeaderMap(HttpServletRequest request) {
        String auth = request.getHeader(CacheConstants.HEADER);
        if (!org.springframework.util.StringUtils.hasText(auth)) {
            return null;
        }
        Map<String, String> headerMap = extractRequestHeader(request);
        headerMap.put(CacheConstants.HEADER, auth);
        String username = request.getHeader(CacheConstants.DETAILS_USERNAME);
        if (org.springframework.util.StringUtils.hasText(username)) {
            headerMap.put(CacheConstants.DETAILS_USERNAME, username);
        }
        return headerMap;
    }
}
