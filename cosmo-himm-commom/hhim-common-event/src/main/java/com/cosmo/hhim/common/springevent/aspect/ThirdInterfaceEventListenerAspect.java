/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.springevent.aspect;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.common.core.text.UUID;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageContent;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageExpand;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageHeader;
import com.cosmo.hhim.common.redis.stream.RedisStreamConstant;
import com.cosmo.hhim.common.redis.stream.RedisStreamProducer;
import com.cosmo.hhim.common.springevent.domain.ThirdInterfaceMQResult;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;



/**
 * @author cosmo-hhim-open Team
 * @description Spring @EventListener注解拦截器
 *              拦截业务处理返回结果--> 组装消息--> 发送 Redis Stream
 * @createTime 2022-07-21
 */
@Slf4j
@Aspect
@Component
public class ThirdInterfaceEventListenerAspect {

    @Autowired
    private RedisStreamProducer redisStreamProducer;

    @Around("execution(public * *(ThirdInterfaceMQEvent+,..)) && @annotation(org.springframework.context.event.EventListener)")
    public Object myAspect(ProceedingJoinPoint pjp) throws Throwable {
        ThirdInterfaceMQEvent event = (ThirdInterfaceMQEvent) pjp.getArgs()[0];
        log.info("common-springEvent--------->Thread-Name:{}, 事件信息：{}", Thread.currentThread().getName(), JSON.toJSONString(event));

        // 执行业务逻辑
        Object result = pjp.proceed();

        if (result instanceof ThirdInterfaceMQResult) {
            ThirdInterfaceMQResult thirdInterfaceMQResult = (ThirdInterfaceMQResult) result;

            // 业务响应结果校验
            if (!StringUtils.hasText(thirdInterfaceMQResult.getMethod())) {
                throw new RuntimeException("方法不允许为空！");
            }
            if (!StringUtils.hasText(thirdInterfaceMQResult.getVersion())) {
                throw new RuntimeException("方法版本不允许为空！");
            }
            if (null == thirdInterfaceMQResult.getBizMqContent()) {
               return result;
            }

            // 组装消息并发送 Redis Stream（替代原 RocketMQ；失败重试由 /external/retryTask 补偿扫描兜底）
            ThirdInterfaceMessageHeader thirdInterfaceMessageHeader = new ThirdInterfaceMessageHeader();
            thirdInterfaceMessageHeader.setRequestId(UUID.randomUUID().toString());
            thirdInterfaceMessageHeader.setTenantCode(event.getEventHeader().getTargetCustomer());
            thirdInterfaceMessageHeader.setTargetDs(event.getEventHeader().getTargetDs());
            thirdInterfaceMessageHeader.setTargetSchema(event.getEventHeader().getTargetSchema());
            thirdInterfaceMessageHeader.setMethod(thirdInterfaceMQResult.getMethod());
            thirdInterfaceMessageHeader.setVersion(thirdInterfaceMQResult.getVersion());
            thirdInterfaceMessageHeader.setHashKey(thirdInterfaceMQResult.getOrderByKey());

            ThirdInterfaceMessageContent thirdInterfaceMessageContent = new ThirdInterfaceMessageContent();
            thirdInterfaceMessageContent.setBizContent(JSON.toJSONString(thirdInterfaceMQResult.getBizMqContent()));

            ThirdInterfaceMessageExpand thirdInterfaceMessageExpand = new ThirdInterfaceMessageExpand();
            thirdInterfaceMessageExpand.setCallbackRawData(thirdInterfaceMQResult.getCallbackRawData());

            ThirdInterfaceMessage message = ThirdInterfaceMessage.builder()
                    .messageHeader(thirdInterfaceMessageHeader)
                    .messageContent(thirdInterfaceMessageContent)
                    .messageExpand(thirdInterfaceMessageExpand)
                    .build();

            log.info("common-springEvent--------->Thread-Name:{}, 消息发送内容：{}", Thread.currentThread().getName(), JSON.toJSONString(message));
            redisStreamProducer.send(RedisStreamConstant.THIRD_INTERFACE_STREAM, JSON.toJSONString(message));
        } else {
            log.error("common-springEvent--------->Thread-Name:{}, 响应结果类型错误！请返回ThirdInterfaceMQResult类型的结果！", Thread.currentThread().getName());
            throw new RuntimeException("spring事件监听器响应结果类型错误！");
        }


        return result;
    }
}
