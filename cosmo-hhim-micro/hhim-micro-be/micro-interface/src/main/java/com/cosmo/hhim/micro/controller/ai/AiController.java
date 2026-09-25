/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.controller.ai;

import com.cosmo.hhim.common.core.web.controller.BaseController;
import com.cosmo.hhim.common.core.web.domain.AjaxResult;
import com.cosmo.hhim.common.security.annotation.AccessAuth;
import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import com.cosmo.hhim.micro.application.service.ai.AiAskService;
import com.cosmo.hhim.micro.application.service.ai.OntologyService;
import com.cosmo.hhim.micro.infrastructure.constant.MicroBusinessConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 问数 · 能力查询 Controller
 *
 * <p>GET /ai/capability：本体驱动的能力清单（入口开关 + 问题模板 + 快捷提问），
 * 与前端「问一问」空态示例/入口展示对齐。
 *
 * @author cosmo-hhim-open Team
 */
@RestController
@RequestMapping("/ai")
public class AiController extends BaseController {

    @Autowired
    private OntologyService ontologyService;

    @Autowired
    private AiAskService aiAskService;

    @Autowired
    private com.cosmo.hhim.micro.application.service.ai.OntologyGraphService ontologyGraphService;

    /**
     * 本体能力清单（管理员/审产员）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/capability")
    public AjaxResult capability() {
        List<OntologyCapabilityDTO> caps = ontologyService.capabilities();
        Map<String, Object> data = new HashMap<>();
        data.put("enabled", !caps.isEmpty());
        data.put("ontologyVersion", ontologyService.size() > 0 ? "1.0" : null);
        data.put("metrics", caps);
        data.put("quickQuestions", ontologyService.quickQuestions());
        return AjaxResult.success(data);
    }

    /**
     * 业务本体关系图（只读）：实体 + 关系 + 指标落点，供「本体图」页面渲染。
     *
     * <p>只输出 scope=workshop（当前产品范围=车间管理域）的实体与关系；计划域（订单/工单/任务）
     * 单独放在 planning 块里，前端折叠展示，避免画出"当前前端触达不到的业务"。
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/ontology/graph")
    public AjaxResult ontologyGraph() {
        return AjaxResult.success(ontologyGraphService.graph());
    }

    /**
     * **业务数据图（实例级）**：真实产品/工序/员工 + 真实发生的报工关系，数字实时从库统计。
     *
     * @param days 统计窗口天数（默认 30）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @GetMapping("/ontology/data-graph")
    public AjaxResult ontologyDataGraph(@org.springframework.web.bind.annotation.RequestParam(value = "days", required = false) Integer days) {
        String tenant = (String) com.cosmo.hhim.common.core.threadlocal.ThreadContext.get(
                com.cosmo.hhim.common.core.constant.Constants.TARGET_CUSTOMER);
        return AjaxResult.success(ontologyGraphService.dataGraph(tenant, days == null ? 30 : days));
    }

    /**
     * 问数主链路（LLM 意图映射 → 本体校验 → 登记接口执行 → 模板答案+evidence）
     */
    @AccessAuth(allowAccessRoles = {MicroBusinessConstants.MANAGER_ROLE, MicroBusinessConstants.AUDITOR_ROLE})
    @PostMapping("/ask")
    public AjaxResult ask(@RequestBody Map<String, String> body) {
        return AjaxResult.success(aiAskService.ask(body.get("question"), body.get("sessionId")));
    }
}
