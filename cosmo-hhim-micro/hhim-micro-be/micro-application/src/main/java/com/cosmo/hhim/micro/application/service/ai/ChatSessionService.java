/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.exception.CustomException;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiChatMessage;
import com.cosmo.hhim.micro.base.domain.entity.ai.MicroAiChatSession;
import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiChatMessageMapper;
import com.cosmo.hhim.micro.base.domain.mapper.ai.MicroAiChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * AI 问数 · 会话存储服务
 *
 * <p>会话 CRUD（创建/列表/消息/重命名/删除）+ 消息写入（上限 20 条校验、状态维护）。
 * 表结构见仓库根 hhim-ai-chat-schema.sql；租户/用户取自 ThreadContext（与项目其它服务一致）。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    /** 会话消息上限（与前端约定一致；200 条约 100 轮问答，防止超出上下文） */
    public static final int MAX_MSGS = 200;

    /** 会话标题最大长度 */
    public static final int MAX_TITLE = 20;

    private final MicroAiChatSessionMapper sessionMapper;
    private final MicroAiChatMessageMapper messageMapper;

    /* ---------------- 会话 CRUD ---------------- */

    /** 创建会话（默认标题"新会话"，状态 10 进行中） */
    public MicroAiChatSession createSession() {
        String tenantCode = tenantCode();
        Long userId = userId();
        MicroAiChatSession s = new MicroAiChatSession();
        s.setSessionId(UUID.randomUUID().toString().replace("-", ""));
        s.setTenantCode(tenantCode);
        s.setUserId(userId);
        s.setTitle("新会话");
        s.setMsgCount(0);
        s.setStatus("10");
        s.setActiveFlag("1");
        fillAudit(s);
        sessionMapper.insert(s);
        return s;
    }

    /** 当前用户会话列表（最后消息时间倒序） */
    public List<MicroAiChatSession> list() {
        return sessionMapper.selectListByUser(tenantCode(), userId());
    }

    /** 会话消息列表（含归属校验） */
    public List<MicroAiChatMessage> messages(String sessionId) {
        checkOwner(sessionId);
        return messageMapper.selectListBySession(sessionId);
    }

    /** 对话上下文（最近 N 条 user/ai 消息文本，时间升序；LLM 理解追问/新话题用） */
    public List<Map<String, String>> recentContext(String sessionId, int n) {
        List<Map<String, String>> ctx = new java.util.ArrayList<>();
        if (!StringUtils.hasText(sessionId)) {
            return ctx;
        }
        try {
            List<MicroAiChatMessage> list = messageMapper.selectRecentBySession(sessionId, n);
            java.util.Collections.reverse(list); // 倒序取 → 反转成时间升序
            for (MicroAiChatMessage m : list) {
                if (m.getRole() == null || !StringUtils.hasText(m.getContent())) {
                    continue;
                }
                Map<String, String> item = new java.util.HashMap<>();
                item.put("role", m.getRole());
                item.put("content", m.getContent().length() > 120 ? m.getContent().substring(0, 120) : m.getContent());
                ctx.add(item);
            }
        } catch (Exception e) {
            // 上下文失败不影响回答
        }
        return ctx;
    }

    /** 最近一次 AI 消息的完整意图（追问槽位继承；旧数据为纯 code，则构造 code-only 对象） */
    public AskIntentResult getLastIntent(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return null;
        }
        try {
            String raw = messageMapper.selectLastAiIntent(sessionId);
            if (!StringUtils.hasText(raw)) {
                return null;
            }
            String t = raw.trim();
            if (t.startsWith("{")) {
                return com.alibaba.fastjson.JSON.parseObject(t, AskIntentResult.class);
            }
            AskIntentResult r = new AskIntentResult();
            r.setIntent(t);
            return r;
        } catch (Exception e) {
            return null;
        }
    }

    /** 重命名（含归属校验） */
    public void rename(String sessionId, String title) {
        checkOwner(sessionId);
        if (!StringUtils.hasText(title)) {
            throw new CustomException("会话名称不能为空");
        }
        sessionMapper.updateTitle(sessionId, title.length() > MAX_TITLE ? title.substring(0, MAX_TITLE) : title,
                username());
    }

    /** 删除（软删，含归属校验） */
    public void remove(String sessionId) {
        checkOwner(sessionId);
        sessionMapper.deleteBySessionId(sessionId, username());
    }

    /* ---------------- 消息写入（供 /ai/ask 编排层使用） ---------------- */

    /**
     * 追加一条消息；达到上限返回 false（调用方引导用户开启新会话）。
     *
     * @return 是否写入成功
     */
    public boolean appendMessage(String sessionId, String role, String content, String routeJson,
                                 String evidenceJson, String clarifyJson, String intent, Integer elapsedMs) {
        MicroAiChatSession s = checkOwner(sessionId);
        if (s.getMsgCount() != null && s.getMsgCount() >= MAX_MSGS) {
            log.info("[AI会话] 已达上限: session={} count={}", sessionId, s.getMsgCount());
            return false;
        }
        Date now = new Date();
        MicroAiChatMessage m = new MicroAiChatMessage();
        m.setSessionId(sessionId);
        m.setTenantCode(s.getTenantCode());
        m.setRole(role);
        m.setContent(content);
        m.setRouteJson(routeJson);
        m.setEvidenceJson(evidenceJson);
        m.setClarifyJson(clarifyJson);
        m.setIntent(intent);
        m.setElapsedMs(elapsedMs);
        m.setMsgTime(now);
        m.setCreatedBy(username());
        m.setCreatedDate(now);
        messageMapper.insert(m);

        // 首次提问（user 消息）时命名会话：首问截断 12 字（单活：仅当仍为"新会话"）
        if ("user".equals(role) && "新会话".equals(s.getTitle()) && StringUtils.hasText(content)) {
            String t = content.length() > 12 ? content.substring(0, 12) + "…" : content;
            s.setTitle(t);
            sessionMapper.updateTitle(sessionId, t, username());
        }

        int count = (s.getMsgCount() == null ? 0 : s.getMsgCount()) + 1;
        sessionMapper.updateMsgInfo(sessionId, count, now, username());
        return true;
    }

    /* ---------------- 内部工具 ---------------- */

    private MicroAiChatSession checkOwner(String sessionId) {
        MicroAiChatSession s = sessionMapper.selectBySessionId(sessionId);
        if (s == null || !"1".equals(s.getActiveFlag())) {
            throw new CustomException("会话不存在或已删除");
        }
        if (!tenantCode().equals(s.getTenantCode()) || !userId().equals(s.getUserId())) {
            throw new CustomException("无权访问该会话");
        }
        return s;
    }

    private void fillAudit(MicroAiChatSession s) {
        String who = username();
        Date now = new Date();
        s.setCreatedBy(who);
        s.setCreatedDate(now);
        s.setLastUpdBy(who);
        s.setLastUpdDate(now);
    }

    private String tenantCode() {
        Object v = ThreadContext.get(Constants.TARGET_CUSTOMER);
        return v == null ? "" : v.toString();
    }

    private Long userId() {
        Object v = ThreadContext.get(CacheConstants.DETAILS_USER_ID);
        if (v == null) {
            throw new CustomException("获取用户信息失败");
        }
        return v instanceof Long ? (Long) v : Long.valueOf(v.toString());
    }

    private String username() {
        Object v = ThreadContext.get(CacheConstants.DETAILS_USERNAME);
        return v == null ? String.valueOf(userId()) : v.toString();
    }
}
