/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.factory;

import com.cosmo.hhim.thirdplat.api.unipush.RemotePushService;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.AppPushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.CreatePushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.ListPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.QueryPushDetailMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SingleBatchPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SinglePushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.AppPushResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.CreatePushMsgResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.ListPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.PushDetailResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SingleBatchPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SinglePushCidResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@Slf4j
@Component
public class RemotePushFallbackFactory implements FallbackFactory<RemotePushService> {

    @Override
    public RemotePushService create(Throwable throwable) {
        log.error("thirdplat push服务调用失败:{}", throwable);
        return new RemotePushService() {
            @Override
            public APIResponse<SinglePushCidResult> singlePush(SinglePushCidMsg message) {
                return APIResponse.fail("根据设备ID单个推送失败：" + throwable, 500);
            }

            @Override
            public APIResponse<SingleBatchPushCidResult> singleBatchPush(SingleBatchPushCidMsg message) {
                return APIResponse.fail("根据设备ID批量单推失败：" + throwable, 500);
            }

            @Override
            public APIResponse<CreatePushMsgResult> createMessage(CreatePushMsg message) {
                return APIResponse.fail("创建消息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<ListPushCidResult> listPush(ListPushCidMsg message) {
                return APIResponse.fail("批量推送消息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<AppPushResult> appPush(AppPushMsg message) {
                return APIResponse.fail("对所有用户群发推送消息失败：" + throwable, 500);
            }

            @Override
            public APIResponse<PushDetailResult> queryPushDetail(QueryPushDetailMsg message) {
                return APIResponse.fail("查询消息推送结果失败：" + throwable, 500);
            }
        };
    }
}
