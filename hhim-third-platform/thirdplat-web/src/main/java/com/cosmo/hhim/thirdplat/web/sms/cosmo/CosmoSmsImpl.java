/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.sms.cosmo;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.provider.service.AbstractSmsBlend;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 卡奥斯短信发送实现（Sms4j 自定义 Blend）。
 *
 * <p>对接卡奥斯自研短信网关（网关地址由配置注入）。
 * <p>本类遵循 Sms4j 扩展规范：
 * <ul>
 *   <li>SUPPLIER = "cosmo"</li>
 *   <li>Factory = {@link CosmoFactory}（实现 {@code BaseProviderFactory}）</li>
 *   <li>Config = {@link CosmoConfig}（继承 {@code BaseConfig}）</li>
 * </ul>
 */
@Slf4j
public class CosmoSmsImpl extends AbstractSmsBlend<CosmoConfig> {

    public CosmoSmsImpl(CosmoConfig config) {
        super(config);
    }

    @Override
    public String getSupplier() {
        return CosmoConfig.SUPPLIER;
    }

    @Override
    public SmsResponse sendMessage(String phone, String message) {
        throw new UnsupportedOperationException("cosmo: 固定内容短信不支持，请使用模板方式");
    }

    @Override
    public SmsResponse sendMessage(String phone, LinkedHashMap<String, String> messages) {
        return sendMessage(phone, getConfig().getTemplateId(), messages);
    }

    @Override
    public SmsResponse sendMessage(String phone, String templateId, LinkedHashMap<String, String> messages) {
        CosmoConfig cfg = getConfig();
        JSONObject gatewayResp;
        try {
            gatewayResp = CosmoSender.send(cfg.getRequestUrl(), cfg.getAccessKeyId(), phone, templateId, messages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SmsResponse resp = new SmsResponse();
        // "发送成功" = 网关已受理（code=0 且发送计数>0，失败场景 CosmoSender 已抛异常）；
        // 网关为异步受理制，不保证最终送达，投递结果需短信回执（本项目未接入）
        resp.setSuccess(true);
        resp.setConfigId(getConfigId());
        // 透传网关原始响应（uid/count/message），便于日志排查
        resp.setData(gatewayResp);
        return resp;
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String message) {
        throw new UnsupportedOperationException("cosmo: 群发暂未实现");
    }

    @Override
    public SmsResponse massTexting(List<String> phones, String templateId, LinkedHashMap<String, String> messages) {
        throw new UnsupportedOperationException("cosmo: 群发暂未实现");
    }
}
