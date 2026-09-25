/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.ai;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.service.ai.ChatSessionService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 问数 · 会话管理 Controller（5 接口）
 *
 * <p>会话 CRUD 与消息读取；与前端「问一问」会话记录/切换/重命名/删除/续聊对齐。
 * 全部走后端① 已有的 user 上下文与租户隔离。
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/ai/chat")
public class ChatSessionController extends BaseController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 创建会话
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @PostMapping("/session")
    public AjaxResult create() {
        return AjaxResult.success(chatSessionService.createSession());
    }

    /**
     * 会话列表（最后消息时间倒序）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/session/list")
    public AjaxResult list() {
        return AjaxResult.success(chatSessionService.list());
    }

    /**
     * 会话消息（含 route/evidence/clarify JSON）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/session/{sessionId}/messages")
    public AjaxResult messages(@PathVariable("sessionId") String sessionId) {
        return AjaxResult.success(chatSessionService.messages(sessionId));
    }

    /**
     * 重命名会话
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @PostMapping("/session/{sessionId}/rename")
    public AjaxResult rename(@PathVariable("sessionId") String sessionId, @RequestBody Map<String, String> body) {
        chatSessionService.rename(sessionId, body.get("title"));
        return AjaxResult.success();
    }

    /**
     * 删除会话（软删）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @DeleteMapping("/session/{sessionId}")
    public AjaxResult remove(@PathVariable("sessionId") String sessionId) {
        chatSessionService.remove(sessionId);
        return AjaxResult.success();
    }
}
