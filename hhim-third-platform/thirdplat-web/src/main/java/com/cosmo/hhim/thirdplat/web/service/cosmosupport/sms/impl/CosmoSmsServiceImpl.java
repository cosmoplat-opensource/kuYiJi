/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.cosmosupport.sms.impl;

import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.CheckStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendSmsParam;
import com.cosmo.hhim.thirdplat.api.cosmosupport.domain.in.SendStandardCodeSmsParam;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.web.constants.RedisKeys;
import com.cosmo.hhim.thirdplat.web.service.cosmosupport.sms.ICosmoSmsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.dromara.sms4j.api.SmsBlend;
import org.dromara.sms4j.api.entity.SmsResponse;
import org.dromara.sms4j.core.factory.SmsFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现（Sms4j 重构版）
 *
 * <p>原实现基于 Forest + 自封装 cosmosupport 层（CosmoSmsApi / SmsAccessKeyInterceptor / CosmoResponseResult 等）。
 * 现改用 Sms4j 统一 API，业务参数（receiver / 逻辑模板 ID / params）通过 {@link SendSmsParam} 透传，
 * 服务商选择与凭证由 yml 的 sms.blends.* 配置驱动；切换服务商仅改 yml 不改代码。
 *
 * <p>业务侧（micro-interface）零改动：仍通过 Feign 调用 {@code RemoteSmsService.sendSms(SendSmsParam)}。
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
@Service
public class CosmoSmsServiceImpl implements ICosmoSmsService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 业务逻辑 ID → 服务商真实模板 ID 的映射。
     * <p>yml 配置示例：
     * <pre>
     * sms:
     *   template-id-map:
     *     LOGIN_CODE:    SMS_123456789    # 登录验证码（服务商后台审核通过的真实模板 ID）
     *     WEEK_REPORT:   SMS_234567890    # 周报推送（占位符需对齐 {productModelNum} {submitTotalNum} 等）
     * </pre>
     * <p>如果业务方传的是服务商真实模板 ID（向后兼容），映射表找不到时原样下发，由 Sms4j 自行处理。
     * <p>注意：嵌套 Map 走 @Value + SpEL 在某些 Spring 版本下绑定会失败为 null，这里兜底走 getter。
     */
    @Value("#{${sms.template-id-map:{}}}")
    private Map<String, String> templateIdMap;

    private Map<String, String> templateIdMapSafe() {
        return templateIdMap != null ? templateIdMap : Collections.emptyMap();
    }

    public CosmoSmsServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取当前启用的 Sms4j Blend。
     * <p>Sms4j 把 Blend 实例托管在 {@code BLENDS} 静态 Map 里，未暴露为 Spring Bean，
     * 不能用 {@code @Autowired} 注入，必须走 {@link SmsFactory#getSmsBlend()} 静态拿。
     */
    private SmsBlend smsBlend() {
        SmsBlend blend = SmsFactory.getSmsBlend();
        if (blend == null) {
            throw new IllegalStateException("Sms4j 未注册任何 Blend，请检查 sms.blends.* 配置与供应商 factory 声明");
        }
        return blend;
    }

    /**
     * 发送短信（透传，业务方传啥发啥）
     *
     * <p>业务方传 tempId 是「逻辑 ID」（如 LOGIN_CODE / WEEK_REPORT），本方法通过 {@code templateIdMap}
     * 翻译为服务商真实模板 ID 后调用 Sms4j。
     */
    @Override
    public APIResponse<Boolean> sendSms(SendSmsParam param) {
        if (param == null || !StringUtils.hasText(param.getReceiver())
                || !StringUtils.hasText(param.getTempId())) {
            return APIResponse.fail("接收者/模板ID不允许为空", 400);
        }

        String logicalTempId = param.getTempId();
        String realTemplateId = templateIdMapSafe().getOrDefault(logicalTempId, logicalTempId);

        log.info("[sms-debug] templateIdMap={}, logicalTempId={}, realTemplateId={}, receiver={}, params={}",
                templateIdMapSafe(), logicalTempId, realTemplateId, param.getReceiver(), param.getParams());

        // SmsBlend.sendMessage 签名固定为 LinkedHashMap<String,String>，不能用 Map<String,Object>
        LinkedHashMap<String, String> templateParams = new LinkedHashMap<>();
        if (param.getParams() != null) {
            templateParams.putAll(param.getParams());
        }

        try {
            SmsResponse resp = smsBlend().sendMessage(param.getReceiver(), realTemplateId, templateParams);
            if (resp == null || !resp.isSuccess()) {
                String errMsg = resp == null ? "Sms4j 返回 null" : String.valueOf(resp);
                log.warn("sendSms: 短信发送失败 receiver={}, logicalTempId={}, realTemplateId={}, resp={}",
                        param.getReceiver(), logicalTempId, realTemplateId, errMsg);
                return APIResponse.fail("短信发送失败: " + errMsg, 500);
            }
            log.info("sendSms: 短信已发送 receiver={}, logicalTempId={}, realTemplateId={}",
                    param.getReceiver(), logicalTempId, realTemplateId);
            return APIResponse.success(true);
        } catch (Exception e) {
            log.error("sendSms: 短信发送异常 receiver={}, logicalTempId={}, realTemplateId={}",
                    param.getReceiver(), logicalTempId, realTemplateId, e);
            return APIResponse.fail("短信发送异常: " + e.getMessage(), 500);
        }
    }

    /**
     * 发送标准短信验证码（生成 6 位数字 + Redis 缓存 + 调用 Sms4j）
     *
     * <p>固定使用逻辑模板 ID {@code LOGIN_CODE}，由 yml 映射到服务商真实模板。
     */
    @Override
    public APIResponse<Boolean> sendStandardCodeSms(SendStandardCodeSmsParam param) {
        if (param == null || !StringUtils.hasText(param.getReceiver())) {
            return APIResponse.fail("接收者不允许为空", 400);
        }

        String code = StringUtils.hasText(param.getCode())
                ? param.getCode()
                : RandomStringUtils.randomNumeric(6);
        String expireIn = StringUtils.hasText(param.getExpireIn()) ? param.getExpireIn() : "6";

        Map<String, String> codeParams = new HashMap<>();
        codeParams.put("code", code);
        codeParams.put("expire", expireIn);

        SendSmsParam sendSmsParam = new SendSmsParam();
        sendSmsParam.setReceiver(param.getReceiver());
        sendSmsParam.setTempId("LOGIN_CODE");
        sendSmsParam.setParams(codeParams);

        APIResponse<Boolean> resp = sendSms(sendSmsParam);
        if (resp == null || !resp.isSuccess()) {
            return resp;
        }

        // 缓存验证码（与原逻辑一致，第三方通过 /send/checkcode 校验）
        redisTemplate.opsForValue().set(
                RedisKeys.CosmoSupport.SMS.value(param.getReceiver()),
                code,
                Integer.parseInt(expireIn),
                TimeUnit.MINUTES);
        log.info("sendStandardCodeSms: 验证码已缓存 receiver={}, code={}, expireIn={}min",
                param.getReceiver(), code, expireIn);
        return APIResponse.success(true);
    }

    /**
     * 校验短信验证码合法性（不变：纯 Redis 校验）
     */
    @Override
    public APIResponse<Boolean> checkStandardCodeSms(CheckStandardCodeSmsParam param) {
        if (param == null || !StringUtils.hasText(param.getPhone())
                || !StringUtils.hasText(param.getCode())) {
            return APIResponse.success(false);
        }
        String redisKey = RedisKeys.CosmoSupport.SMS.value(param.getPhone());
        if (!redisTemplate.hasKey(redisKey)) {
            log.warn("checkStandardCodeSms: 手机号：{}, 验证码不存在或已过期", param.getPhone());
            return APIResponse.success(false);
        }
        String code = redisTemplate.opsForValue().get(redisKey);
        if (!StringUtils.hasText(code) || !param.getCode().equals(code)) {
            log.warn("checkStandardCodeSms: 手机号：{}, 验证码错误", param.getPhone());
            return APIResponse.success(false);
        }
        return APIResponse.success(true);
    }
}
