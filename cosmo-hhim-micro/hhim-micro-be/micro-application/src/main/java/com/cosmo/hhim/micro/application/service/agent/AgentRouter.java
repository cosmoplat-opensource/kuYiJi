/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.agent;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Agent 循环 · 确定性路由（无 LLM）
 *
 * <p>分流：空/超长、封禁词（工资/结算/金额类）、闲聊/礼貌语、其余放行。
 * 判不定 → 放行进核心循环（由循环内反思兜底）。
 *
 * @author cosmo-hhim-open Team
 */
@Component
public class AgentRouter {

    /** 路由类型 */
    public static final String ASK = "ASK";
    public static final String BOUNDARY = "BOUNDARY";
    public static final String SMALLTALK = "SMALLTALK";

    private static final int MAX_LEN = 200;

    private static final String[] FORBIDDEN = {"工资", "薪资", "薪酬", "结算", "罚款", "报酬", "提成"};

    private static final Pattern SMALLTALK_PATTERN = Pattern.compile(
            "(你好|您好|早上好|下午好|晚上好|谢谢|感谢|再见|拜拜|在吗|你是谁|你叫什么|能干什么|你会什么|"
                    + "能做什么|干嘛|有什么用|嗯|好的|.ok.*)");

    public String route(String question) {
        if (!StringUtils.hasText(question)) {
            return BOUNDARY;
        }
        String q = question.trim();
        if (q.length() > MAX_LEN) {
            return BOUNDARY;
        }
        for (String w : FORBIDDEN) {
            if (q.contains(w)) {
                return BOUNDARY;
            }
        }
        if (SMALLTALK_PATTERN.matcher(q).find() && q.length() <= 12) {
            return SMALLTALK;
        }
        return ASK;
    }
}
