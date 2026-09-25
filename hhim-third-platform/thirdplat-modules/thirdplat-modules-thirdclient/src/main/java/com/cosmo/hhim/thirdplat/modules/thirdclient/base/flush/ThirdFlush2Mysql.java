/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.thirdplat.api.thirdclient.common.ThirdParamConstant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.HyzzThirdInterfaceLog;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdInterfaceEntity;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdFlushEnum;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdStatusEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdFlushStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ThirdFlush2Mysql extends ThirdFlushAbstract {
    @Autowired
    private FlushService flushService;

    @Override
    public String flushDB(ThirdStatusEnum statusEnum, ThirdInterfaceEntity interfaceEntity, Date requestDate) {
        HyzzThirdInterfaceLog interfaceLog = new HyzzThirdInterfaceLog();
        ThirdClientTenant clientTenant = interfaceEntity.getClientTenant();
        ThirdInterfaceMessage message = interfaceEntity.getInterfaceMessage();
        ThirdResponse response = interfaceEntity.getResponse();
        interfaceLog.setRequestId(message.getMessageHeader().getRequestId());
        interfaceLog.setTenantCode(message.getMessageHeader().getTenantCode());
        interfaceLog.setMethod(message.getMessageHeader().getMethod());
        interfaceLog.setVersion(message.getMessageHeader().getVersion());
        interfaceLog.setContent(message.getMessageContent().getBizContent());
        if (message.getMessageExpand() != null) {
            interfaceLog.setExpandContent(JSONObject.toJSONString(message.getMessageExpand()));
        }
        interfaceLog.setMessageKey(interfaceEntity.getMessageKeys());
        interfaceLog.setMessageId(interfaceEntity.getMessageId());
        interfaceLog.setRequestUrl(clientTenant.getRequestUrl());
        interfaceLog.setRequestTime(requestDate);
        if (!"-1".equals(statusEnum.getStatus())) {
            interfaceLog.setResponseTime(new Date());
        }
        interfaceLog.setSendStatus(statusEnum.getStatus());
        interfaceLog.setRetryCount((long) interfaceEntity.getReconsumeTimes());
        interfaceLog.setClientSupport(clientTenant.getClientSupport());
        interfaceLog.setClientStatus(ThirdParamConstant.THIRD_RESPONSE_SUCCESS.equals(response.getCode()) ? "1" : "0");
        flushService.saveLog(statusEnum, interfaceLog, response);
        return "ok";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ThirdFlushStrategy.register(ThirdFlushEnum.HYZZ_COMMON_MYSQL, this);
    }
}
