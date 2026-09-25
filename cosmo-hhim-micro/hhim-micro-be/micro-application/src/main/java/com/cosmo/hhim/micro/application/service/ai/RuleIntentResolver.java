/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.cosmo.hhim.micro.application.dto.ai.AskIntentResult;
import com.cosmo.hhim.micro.application.dto.ai.OntologyCapabilityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * AI 问数 · 规则意图解析器（降级实现，不依赖 LLM）
 *
 * <p>基于本体能力清单做"包含匹配"（问题模板 → 别名 → 编码），
 * 并做时间语义识别与日期换算；实体遗留执行层解析。LLM 故障时保证问数仍可用。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RuleIntentResolver implements IntentResolver {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final OntologyService ontologyService;
    private final TimeParser timeParser;

    @Override
    public AskIntentResult resolve(String question) {
        AskIntentResult r = new AskIntentResult();
        r.setSource("RULE");
        if (!StringUtils.hasText(question)) {
            r.setIntent("NOT_SUPPORTED");
            return r;
        }
        String q = question.trim();
        // 口语报工汇总（"报了多工吗/有活干吗/报工了吗"）：SUMMARY（配合 raw 口径）
        if (q.contains("报了多工") || q.contains("报工了吗") || q.contains("有活干吗")
                || q.contains("有活没有") || q.contains("有没有活")) {
            r.setIntent("SUMMARY");
            return r;
        }
        // 记工排名类（"谁记的工/记工多少/谁报工最多"）：SUBMIT_RANK 专有映射，避免落到其它指标或追问继承
        if (q.contains("记工") || q.contains("报工")) {
            if (q.contains("谁") || q.contains("最多") || q.contains("排名") || q.contains("多少") || q.contains("哪个")) {
                r.setIntent("SUBMIT_RANK");
                return r;
            }
        }
        // 实体清单类（ENTITY_LIST）：专有规则（"有什么/有哪些 + 实体词"），避免与其它指标别名混淆
        String entityType = matchEntityList(q);
        if (entityType != null) {
            r.setIntent("ENTITY_LIST");
            r.getEntities().put("entityType", entityType);
            return r;
        }
        r.setIntent(matchIntent(q));
        if ("NOT_SUPPORTED".equals(r.getIntent())) {
            return r;
        }
        timeParser.apply(q, r);
        resolveOrder(q, r);
        // 组合式：日期粒度词 → groupBy=day（SUMMARY/PRODUCT_PASS_RATE 等 dims 含 day 的指标由白名单校验）
        if (q.contains("哪天") || q.contains("哪一天") || q.contains("按天")
                || q.contains("每天") || q.contains("每日") || q.contains("逐日")) {
            r.setGroupBy("day");
        }
        return r;
    }

    /** 实体清单模式匹配：返回产品/工序/员工类型，不匹配返回 null */
    private String matchEntityList(String q) {
        boolean listMode = q.contains("有哪些") || q.contains("有什么") || q.contains("有几个")
                || q.contains("哪些）") || q.endsWith("哪些") || q.endsWith("有什么");
        if (!listMode) {
            return null;
        }
        if (q.contains("零件") || q.contains("产品") || q.contains("物料") || q.contains("工件") || q.contains("物品")) {
            return "product";
        }
        if (q.contains("工序") || q.contains("工艺")) {
            return "process";
        }
        if (q.contains("员工") || q.contains("人员") || q.contains("工人")) {
            return "employee";
        }
        return null;
    }

    /** 模板/别名包含匹配，取第一个命中（本体顺序即优先级） */
    private String matchIntent(String q) {
        List<OntologyCapabilityDTO> caps = ontologyService.capabilities();
        for (OntologyCapabilityDTO c : caps) {
            for (String ex : c.getExamples()) {
                if (q.contains(ex) || ex.contains(q)) {
                    return c.getCode();
                }
            }
        }
        for (OntologyCapabilityDTO c : caps) {
            for (String alias : c.getAliases()) {
                if (q.contains(alias)) {
                    return c.getCode();
                }
            }
        }
        return "NOT_SUPPORTED";
    }

    /** 排序方向：最低/最差/最少 → asc；最高/最多/最好/最 → desc */
    private void resolveOrder(String q, AskIntentResult r) {
        if (q.contains("最低") || q.contains("最差") || q.contains("最少") || q.contains("垫底")) {
            r.setOrderDir("asc");
        } else if (q.contains("最高") || q.contains("最多") || q.contains("最好") || q.contains("第一") || q.contains("前十")) {
            r.setOrderDir("desc");
        }
        if (q.contains("前十") || q.contains("前10")) {
            r.setLimit(10);
        }
    }
}
