/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.service;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageContent;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageExpand;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageHeader;
import com.cosmo.hhim.common.redis.service.RedisService;
import com.cosmo.hhim.external.mapper.HyzzThirdInterfaceLogMapper;
import com.cosmo.hhim.thirdplat.api.thirdclient.common.ThirdParamConstant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceRetry;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdInterfaceEntity;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdClientStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ThirdTaskRetryService {
    private static final Logger logger = LoggerFactory.getLogger(ThirdTaskRetryService.class);

    @Resource
    private HyzzThirdInterfaceLogMapper interfaceLogMapper;
    @Resource
    private RedisService redisService;
    @Resource
    private ThirdRequestLogService requestLogService;


    public void taskRetry(Integer shardIndex, Integer shardTotal) {
        //取出需要补偿的任务
        List<HyzzThirdInterfaceRetry> list = interfaceLogMapper.getScheduleRetryTask(shardIndex, shardTotal);
        if (CollectionUtils.isEmpty(list)) {
            logger.info("没有需要补偿的任务");
            return;
        }
        String msgId = "";
        String keys = "";
        ThirdInterfaceMessageHeader header;
        ThirdInterfaceMessageContent messageContent;
        ThirdClientAbstract thirdClient;
        ThirdClientTenant clientTenant;
        String redisRequestKey;
        JSONObject authInfo;
        for (HyzzThirdInterfaceRetry interfaceLog : list) {
            String requestId = interfaceLog.getRequestId();
            String tenantCode = interfaceLog.getTenantCode();
            String content = interfaceLog.getContent();
            String version = interfaceLog.getVersion();
            String method = interfaceLog.getMethod();
            String expandContent = interfaceLog.getExpandContent();
            header = new ThirdInterfaceMessageHeader();
            header.setRequestId(requestId);
            header.setMethod(method);
            header.setVersion(version);
            header.setTenantCode(tenantCode);
            messageContent = new ThirdInterfaceMessageContent();
            messageContent.setBizContent(content);
            ThirdInterfaceMessageExpand messageExpand = new ThirdInterfaceMessageExpand();
            if (!StringUtils.isEmpty(expandContent)) {
                messageExpand = JSONObject.parseObject(expandContent, ThirdInterfaceMessageExpand.class);
            }
            ThirdInterfaceMessage message = ThirdInterfaceMessage.builder()
                    .messageHeader(header)
                    .messageContent(messageContent)
                    .messageExpand(messageExpand)
                    .build();
            int reconsumeTimes = Math.toIntExact(interfaceLog.getRetryCount()) + 1;

            String clientSupport = interfaceLog.getClientSupport();
            redisRequestKey = CacheConstants.THIRD_INTERFACE_KEY + ThirdParamConstant.THIRD_REDIS_REQUEST_SUCCESS_KEY + requestId + clientSupport;
            /*
            消息幂等处理,如果同一个request_id+client_support存在重复消费,打印预警log并直接放弃后续逻辑
             */
            if (!Objects.isNull(redisService.getCacheObject(redisRequestKey)) || requestLogService.verifyOnlyOnce(requestId, clientSupport)) {
                logger.warn("第三方接口消费消息幂等预警, 请求ID:{},渠道:{},消息ID{},KEYS{},", requestId, clientSupport, msgId, keys);
                redisService.setCacheObject(redisRequestKey, content, 180L, TimeUnit.MINUTES);
                continue;
            }
            thirdClient = ThirdClientStrategy.getThirdClient(clientSupport);
            clientTenant = new ThirdClientTenant();
            clientTenant.setTenantCode(interfaceLog.getTenantCode());
            clientTenant.setRequestUrl(interfaceLog.getRequestUrl());
            clientTenant.setRequestType(interfaceLog.getRequestType());
            clientTenant.setClientSupport(clientSupport);
            clientTenant.setCallback(interfaceLog.getCallback());
            clientTenant.setCallbackTopic(interfaceLog.getCallbackTopic());
            clientTenant.setCallbackTag(interfaceLog.getCallbackTag());
            /*
            这里获取第三方平台所需的额外信息,比如前置token接口所需的秘钥等,运维事先设置好
             */
            try {
                authInfo = JSONObject.parseObject(interfaceLog.getAuthInfo());
            } catch (JSONException e) {
                authInfo = new JSONObject();
            }
            clientTenant.setAuthInfo(authInfo);
            try {
                thirdClient.call(new ThirdInterfaceEntity(keys, msgId, reconsumeTimes, message, clientTenant));
            } catch (Exception e) {
                logger.error("第三方接口补偿失败:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            }
        }
    }
}
