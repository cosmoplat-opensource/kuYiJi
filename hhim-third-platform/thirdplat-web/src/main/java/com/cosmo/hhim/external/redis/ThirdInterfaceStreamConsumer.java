/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.redis.service.RedisService;
import com.cosmo.hhim.common.redis.stream.AbstractRedisStreamListener;
import com.cosmo.hhim.common.redis.stream.RedisStreamConstant;
import com.cosmo.hhim.common.redis.stream.RedisStreamContainer;
import com.cosmo.hhim.thirdplat.api.thirdclient.common.ThirdParamConstant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceMethodMapping;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdInterfaceEntity;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdClientStrategy;
import com.cosmo.hhim.external.service.ThirdInterfaceMethodMappingService;
import com.cosmo.hhim.external.service.ThirdRequestLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 三方接口异步调用消息消费者（替代原 RocketMQ ThirdInterfaceOrderedListener / ThirdInterfaceUnorderedListener）。
 *
 * <p>Redis Stream 单消费者按序处理，天然满足原"有序队列"语义；
 * 调用失败由 {@link com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract#call}
 * 落库（REQUEST_ERROR），由 /external/retryTask 补偿扫描重试。
 */
@Slf4j
@Component
public class ThirdInterfaceStreamConsumer extends AbstractRedisStreamListener {

    @Resource
    private RedisStreamContainer redisStreamContainer;
    @Resource
    private ThirdRequestLogService requestLogService;
    @Resource
    private RedisService redisService;
    @Resource
    private ThirdInterfaceMethodMappingService methodMappingService;

    @PostConstruct
    public void init() {
        redisStreamContainer.register(getStreamKey(), getGroup(), this);
    }

    @Override
    protected String getStreamKey() {
        return RedisStreamConstant.THIRD_INTERFACE_STREAM;
    }

    @Override
    protected String getGroup() {
        return RedisStreamConstant.THIRD_INTERFACE_GROUP;
    }

    @Override
    protected void handle(String payload) throws Exception {
        ThirdInterfaceMessage message = JSON.parseObject(payload, ThirdInterfaceMessage.class);
        if (message == null || message.getMessageHeader() == null) {
            log.warn("第三方接口消息解析失败, payload:{}", payload);
            return;
        }
        String requestId = message.getMessageHeader().getRequestId();
        String method = message.getMessageHeader().getMethod();
        String version = message.getMessageHeader().getVersion();
        String tenantCode = message.getMessageHeader().getTenantCode();

        // 根据 method/version/tenantCode 查询出所有 client_support
        List<HyzzThirdInterfaceMethodMapping> clients = methodMappingService.getClientSupportList(method, version, tenantCode);
        if (CollectionUtils.isEmpty(clients)) {
            log.warn("第三方接口未配置租户/方法映射, method:{}, version:{}, tenant:{}, requestId:{}", method, version, tenantCode, requestId);
            return;
        }

        for (HyzzThirdInterfaceMethodMapping client : clients) {
            String clientSupport = client.getClientSupport();
            String redisRequestKey = CacheConstants.THIRD_INTERFACE_KEY + ThirdParamConstant.THIRD_REDIS_REQUEST_SUCCESS_KEY + requestId + clientSupport;
            // 幂等处理：同一 request_id + client_support 重复消费直接跳过
            if (!Objects.isNull(redisService.getCacheObject(redisRequestKey)) || requestLogService.verifyOnlyOnce(requestId, clientSupport)) {
                log.warn("第三方接口消费消息幂等预警, 请求ID:{}, 渠道:{}", requestId, clientSupport);
                redisService.setCacheObject(redisRequestKey, payload, 180L, TimeUnit.MINUTES);
                continue;
            }

            ThirdClientAbstract thirdClient = ThirdClientStrategy.getThirdClient(clientSupport);
            ThirdClientTenant clientTenant = new ThirdClientTenant();
            clientTenant.setTenantCode(client.getTenantCode());
            clientTenant.setRequestUrl(client.getRequestUrl());
            clientTenant.setClientSupport(clientSupport);
            clientTenant.setRequestType(client.getRequestType());
            clientTenant.setCallback(client.getCallback());
            clientTenant.setCallbackTopic(client.getCallbackTopic());
            clientTenant.setCallbackTag(client.getCallbackTag());
            // 获取第三方平台所需的额外信息（前置 token 接口所需秘钥等，运维事先配置）
            JSONObject authInfo;
            try {
                authInfo = JSONObject.parseObject(client.getAuthInfo());
                log.info("client authInfo is {}", authInfo);
            } catch (JSONException e) {
                authInfo = new JSONObject();
            }
            clientTenant.setAuthInfo(authInfo);

            ThirdInterfaceEntity interfaceEntity = new ThirdInterfaceEntity(null, null, 0, message, clientTenant);
            try {
                thirdClient.call(interfaceEntity);
            } catch (Exception e) {
                // 失败已由 ThirdClientAbstract 落库（REQUEST_ERROR），由 /external/retryTask 补偿扫描重试
                log.error("第三方接口调用失败:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e);
            }
        }
    }
}
