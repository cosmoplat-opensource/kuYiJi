/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.sms.cosmo;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 卡奥斯短信网关 HTTP 调用工具。
 *
 * <p>对接卡奥斯短信网关（网关地址由配置注入），请求体形如：
 * <pre>
 * { "receiver":"...", "accessKey":"...", "tempId":"...", "params":{...} }
 * </pre>
 */
@Slf4j
public final class CosmoSender {

    private static final HttpClient HTTP_CLIENT = HttpClients.createDefault();

    private CosmoSender() {
    }

    /**
     * @param url          卡奥斯网关地址（取自 {@link CosmoConfig#getRequestUrl()}）
     * @param accessKey    接入凭证
     * @param receiver     接收手机号
     * @param templateId   短信模板 ID
     * @param messages     模板占位符参数
     * @return 网关原始响应 JSON（受理成功时含 uid/count/message，供上层透传追溯）
     * @throws IOException 当 HTTP 调用失败、业务返回码非 0、或网关受理但发送计数为 0 时
     */
    public static JSONObject send(String url, String accessKey, String receiver, String templateId,
                            LinkedHashMap<String, String> messages) throws IOException {
        JSONObject body = new JSONObject();
        body.put("receiver", receiver);
        body.put("accessKey", accessKey);
        body.put("tempId", templateId);
        body.put("params", messages);

        log.info("[cosmo-sms-debug] request: url={}, body={}", url, body.toJSONString());

        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(body.toJSONString(), StandardCharsets.UTF_8);
        entity.setContentEncoding("UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");

        HttpResponse response = HTTP_CLIENT.execute(httpPost);
        int status = response.getStatusLine().getStatusCode();
        String raw = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        log.info("[cosmo-sms-debug] response: status={}, body={}", status, raw);

        if (status != 200) {
            throw new IOException("cosmo sms http status=" + status + ", body=" + raw);
        }
        JSONObject resp = JSONObject.parseObject(raw);
        String code = resp.getString("code");
        String msg = resp.getString("msg");
        if (!"0".equals(code)) {
            log.warn("[cosmo-sms-debug] biz rejected: code={}, msg={}, data={}", code, msg, resp.getString("data"));
            throw new IOException("cosmo sms biz error, code=" + code + ", msg=" + msg);
        }
        // 网关受理成功但发送计数为 0：短信实际未发出（如号码/模板被网关侧拦截、通道异常等），
        // 按业务失败处理，避免页面提示"发送成功"但手机收不到（正常发送时 count>=1）
        Integer count = resp.getInteger("count");
        if (count != null && count <= 0) {
            log.warn("[cosmo-sms-debug] accepted but count=0: msg={}, data={}", msg, raw);
            throw new IOException("cosmo sms accepted but count=0 (短信未实际发出), msg=" + msg);
        }
        return resp;
    }

    /**
     * Map → LinkedHashMap 转换（保留调用方传入顺序）
     */
    public static LinkedHashMap<String, String> toLinkedHashMap(Map<String, String> src) {
        LinkedHashMap<String, String> dst = new LinkedHashMap<>();
        if (src != null) {
            dst.putAll(src);
        }
        return dst;
    }
}
