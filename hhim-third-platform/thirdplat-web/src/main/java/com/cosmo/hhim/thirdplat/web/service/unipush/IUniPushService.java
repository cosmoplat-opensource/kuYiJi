/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.unipush;

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

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
public interface IUniPushService {

    /**
     * 执行cid单推
     * 向单个用户推送消息，可根据cid指定用户
     * @param message
     */
    APIResponse<SinglePushCidResult> singlePush(SinglePushCidMsg message);

    /**
     * 执行cid批量单推
     * 批量发送单推消息，每个cid用户的推送内容都不同的情况下，使用此接口，可提升推送效率。
     * @param message
     * @return
     */
    APIResponse<SingleBatchPushCidResult> singleBatchPush(SingleBatchPushCidMsg message);

    /**
     * 创建消息
     * 此接口用来创建消息体，并返回taskid，为批量推的前置步骤
     * @param message
     * @return
     */
    APIResponse<CreatePushMsgResult> createMessage(CreatePushMsg message);

    /**
     * 执行cid批量推
     * 对列表中所有cid进行消息推送。调用此接口前需调用创建消息接口设置消息内容。
     * @param message
     * @return
     */
    APIResponse<ListPushCidResult> listPush(ListPushCidMsg message);

    /**
     * 执行群推
     * 对指定应用的所有用户群发推送消息。支持定时、定速功能，查询任务推送情况请见接口查询定时任务。
     * @param message
     * @return
     */
    APIResponse<AppPushResult> appPush(AppPushMsg message);

    /**
     * 查询消息推送结果明细
     * 调用此接口可以查询某任务下某cid的具体实时推送路径情况
     * @param message
     * @return
     */
    APIResponse<PushDetailResult> queryPushDetail(QueryPushDetailMsg message);
}
