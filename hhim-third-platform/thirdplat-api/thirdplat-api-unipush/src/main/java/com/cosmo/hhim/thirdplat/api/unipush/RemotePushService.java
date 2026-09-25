/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush;

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
import com.cosmo.hhim.thirdplat.api.unipush.factory.RemotePushFallbackFactory;
import com.cosmo.hhim.thirdplat.common.constants.Constant;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-26
 */
@FeignClient(contextId = "remotePushService", url = "${feignDebug.thirdplatUrl}", value = Constant.THIRDPLAT_SERVICE, fallbackFactory = RemotePushFallbackFactory.class)
public interface RemotePushService {

    /**
     * 根据设备ID单个推送
     * @param message
     * @return
     */
    @PostMapping("/uniapp/push/single")
    APIResponse<SinglePushCidResult> singlePush(SinglePushCidMsg message);

    /**
     * 根据设备ID批量单推
     * @param message
     * @return
     */
    @PostMapping("/uniapp/push/singlebatch")
    APIResponse<SingleBatchPushCidResult> singleBatchPush(SingleBatchPushCidMsg message);

    /**
     * 创建消息（是批量推的前置接口）
     * @param message
     * @return
     */
    @PostMapping("/uniapp/push/createmessage")
    APIResponse<CreatePushMsgResult> createMessage(CreatePushMsg message);

    /**
     * 批量推送消息（前提已创建消息）
     * @param message
     * @return
     */
    @PostMapping("/uniapp/push/list")
    APIResponse<ListPushCidResult> listPush(ListPushCidMsg message);

    /**
     * 对所有用户群发推送消息
     * @param message
     * @return
     */
    @PostMapping("/uniapp/push/all")
    APIResponse<AppPushResult> appPush(AppPushMsg message);

    /**
     * 查询消息推送结果
     * @param message
     * @return
     */
    @GetMapping("/uniapp/push/pushdetail")
    APIResponse<PushDetailResult> queryPushDetail(@SpringQueryMap QueryPushDetailMsg message);
}
