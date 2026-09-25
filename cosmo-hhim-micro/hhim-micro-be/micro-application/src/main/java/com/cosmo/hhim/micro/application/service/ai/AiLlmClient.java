/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.application.service.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 问数 · LLM 客户端（OpenAI 兼容 /chat/completions，JDK 8 零依赖实现）
 *
 * <p>配置（application-local.yml / 环境变量）：
 * ai.base-url / ai.api-key / ai.chat-model / ai.timeout-ms。
 * 仅用于"口语→意图映射"与"答案润色"两个环节；数字与 SQL 永不经过此处。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Component
public class AiLlmClient {

    @Value("${ai.base-url:}")
    private String baseUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.chat-model:}")
    private String chatModel;

    @Value("${ai.timeout-ms:10000}")
    private int timeoutMs;

    /**
     * 启动时打印生效的 LLM 配置（**不打印 api-key**，只打印是否存在与长度）。
     *
     * <p>用途：排查"改了配置没生效"时一眼可见（与问数链路版本横幅同理）。
     * 尤其 timeout-ms：偏小时会把正常但较慢的回答判成超时（历史多次踩到）。
     */
    @javax.annotation.PostConstruct
    public void logEffectiveConfig() {
        String host = baseUrl;
        try {
            java.net.URI uri = java.net.URI.create(baseUrl);
            host = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() > 0 ? ":" + uri.getPort() : "");
        } catch (Exception ignore) {
            // base-url 非法时原样打印，便于发现配置错误
        }
        log.info("[AI-LLM] 生效配置：base-url={} model={} timeout={}ms api-key={}",
                StringUtils.hasText(baseUrl) ? host : "(未配置)",
                StringUtils.hasText(chatModel) ? chatModel : "(未配置)",
                timeoutMs,
                StringUtils.hasText(apiKey) ? "已配置(" + apiKey.length() + "字符)" : "未配置");
    }

    /**
     * 对话补全（同步）；失败返回 null（由调用方降级）。
     *
     * @param system system prompt（能力清单/约束）
     * @param user   用户消息
     * @return assistant 回复文本，失败返回 null
     */
    public String chat(String system, String user) {
        if (!StringUtils.hasText(baseUrl) || !StringUtils.hasText(apiKey)) {
            log.warn("[AI-LLM] 未配置 base-url/api-key，跳过 LLM 调用");
            return null;
        }
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl.replaceAll("/+$", "") + "/chat/completions");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", chatModel);
            body.put("temperature", 0.1);
            JSONArray messages = new JSONArray();
            if (StringUtils.hasText(system)) {
                JSONObject s = new JSONObject();
                s.put("role", "system");
                s.put("content", system);
                messages.add(s);
            }
            JSONObject u = new JSONObject();
            u.put("role", "user");
            u.put("content", user);
            messages.add(u);
            body.put("messages", messages);

            try (DataOutputStream dos = new DataOutputStream(conn.getOutputStream())) {
                dos.write(JSON.toJSONString(body).getBytes(StandardCharsets.UTF_8));
                dos.flush();
            }

            int code = conn.getResponseCode();
            InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String resp = readStream(is);
            if (code >= 400) {
                log.warn("[AI-LLM] 调用失败: code={} resp={}", code, shortResp(resp));
                return null;
            }
            JSONObject root = JSON.parseObject(resp);
            JSONArray choices = root.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                log.warn("[AI-LLM] 无 choices: {}", shortResp(resp));
                return null;
            }
            String content = choices.getJSONObject(0).getJSONObject("message").getString("content");
            return content == null ? null : content.trim();
        } catch (Exception e) {
            log.warn("[AI-LLM] 调用异常: {}", e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readStream(InputStream is) throws Exception {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private String shortResp(String s) {
        return s == null ? "" : (s.length() > 300 ? s.substring(0, 300) : s);
    }
}
