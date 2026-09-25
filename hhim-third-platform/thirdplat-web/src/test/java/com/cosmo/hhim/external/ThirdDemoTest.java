/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external;

import com.alibaba.fastjson.JSON;
import com.cosmo.hhim.ThirdpartWebApplication;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageContent;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageHeader;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.core.utils.sign.ThirdSecretUtil;
import com.cosmo.hhim.common.redis.stream.RedisStreamConstant;
import com.cosmo.hhim.common.redis.stream.RedisStreamProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ThirdpartWebApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ThirdDemoTest {

    @Autowired
    private RedisStreamProducer redisStreamProducer;

    @Test
    public void secretCreate() {
        ThirdInterfaceTenant tenant = ThirdSecretUtil.createThirdIdAndSecret("RSA2");
    }

    /**
     * 端到端验证 RocketMQ → Redis Streams 替代链路（零副作用）：
     * 投递一条"方法映射必然为空"的测试消息（method 使用不存在的测试名，
     * 不会命中任何真实租户/方法配置、不会触发任何外部调用），
     * 观察消费者日志出现："第三方接口未配置租户/方法映射, method:redis_stream_test_v1 ..."
     * 即证明：生产（XADD）→ Redis Stream → 消费者（XREADGROUP）→ 业务处理入口 整条链路已打通。
     * 注意：消费组可能被同组其他在线实例（公司环境）竞争消费，如本进程日志未出现该 warn，
     * 可重跑一次或查看公司实例日志。
     */
    @Test
    public void redisStreamSubstitutionTest() throws Exception {
        ThirdInterfaceMessageHeader header = new ThirdInterfaceMessageHeader();
        header.setRequestId(IdUtils.fastUUID());
        header.setMethod("redis_stream_test_v1");
        header.setVersion("v1");
        header.setTenantCode("A9K3Q7");
        ThirdInterfaceMessageContent content = new ThirdInterfaceMessageContent();
        content.setBizContent("{\"hello\":\"redis-stream\"}");
        ThirdInterfaceMessage message = ThirdInterfaceMessage.builder()
                .messageHeader(header)
                .messageContent(content)
                .build();
        redisStreamProducer.send(RedisStreamConstant.THIRD_INTERFACE_STREAM, JSON.toJSONString(message));
        System.out.println("[redis-stream-test] 消息已投递, requestId=" + header.getRequestId()
                + ", 等待消费者处理, 请观察日志: 第三方接口未配置租户/方法映射");
        Thread.sleep(3000);
    }
}
