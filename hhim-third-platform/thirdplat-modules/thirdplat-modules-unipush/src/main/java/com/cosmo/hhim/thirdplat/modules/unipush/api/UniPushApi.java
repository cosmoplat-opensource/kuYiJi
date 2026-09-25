/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.api;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Audience;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.BatchSingleCidPushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.ListPushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.PushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push.PushQueryResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push.PushResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push.ScheduleTaskQueryResult;
import com.cosmo.hhim.thirdplat.modules.unipush.interceptor.TokenHeaderInterceptor;
import com.cosmo.hhim.thirdplat.modules.unipush.proxy.annotation.UniPushResponseException;
import com.dtflys.forest.annotation.BaseRequest;
import com.dtflys.forest.annotation.Delete;
import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @description 推送API
 * @createTime 2021-09-22
 */
@BaseRequest(
        baseURL = "${uniBaseUrl}${uniAppId}",
        headers = {
                "Accept-Charset: ${uniEncoding}",
                "Content-Type: ${uniContentType}"
        },
        interceptor = TokenHeaderInterceptor.class
)
@UniPushResponseException
public interface UniPushApi {

    /**
     * 【toSingle】执行cid单推
     * 向单个用户推送消息，可根据cid指定用户
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/single/cid")
    ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> singleCidPush(@JSONBody PushParam<Audience> pushParam);

    /**
     * 【toSingle】执行cid批量单推
     * 批量发送单推消息，每个cid用户的推送内容都不同的情况下，使用此接口，可提升推送效率。
     *
     * @return
     */
    @Post(url = "/push/single/batch/cid")
    ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> batchSingleCidPush(@JSONBody BatchSingleCidPushParam pushParam);

    /**
     * 【toList】创建消息
     * 此接口用来创建消息体，并返回taskid，为批量推的前置步骤
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/list/message")
    ForestResponse<UniPushResponseResult<PushResult>> listCreateMsg(@JSONBody PushParam<Audience> pushParam);

    /**
     * 【toList】执行cid批量推
     * 对列表中所有cid进行消息推送。调用此接口前需调用创建消息接口设置消息内容。
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/list/cid")
    ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> listCidPush(@JSONBody ListPushParam pushParam);

    /**
     * 【toApp】执行群推
     * 对指定应用的所有用户群发推送消息。支持定时、定速功能，查询任务推送情况请见接口查询定时任务。
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/all")
    ForestResponse<UniPushResponseResult<PushResult>> appAllPush(@JSONBody PushParam<String> pushParam);

    /**
     * 【toApp】根据条件筛选用户推送
     * 对指定应用的符合筛选条件的用户群发推送消息。支持定时、定速功能。
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/tag")
    ForestResponse<UniPushResponseResult<PushResult>> appTagPush(@JSONBody PushParam<Audience> pushParam);

    /**
     * 【toApp】使用标签快速推送
     * 根据标签过滤用户并推送。支持定时、定速功能。
     *
     * @param pushParam
     * @return
     */
    @Post(url = "/push/fast_custom_tag")
    ForestResponse<UniPushResponseResult<PushResult>> appFastTagPush(@JSONBody PushParam<Audience> pushParam);

    /**
     * 【推送】查询消息明细
     * 调用此接口可以查询某任务下某cid的具体实时推送路径情况
     *
     * @param cId
     * @param taskId
     * @return
     */
    @Get(url = "/task/detail/${cid}/${taskid}")
    ForestResponse<UniPushResponseResult<PushQueryResult>> queryPushResult(@Var("cid") String cId, @Var("taskid") String taskId);

    /**
     * 【任务】停止任务
     * 对正处于推送状态，或者未接收的消息停止下发（只支持批量推和群推任务）
     *
     * @param taskId 任务id (格式RASL-MMdd_XXXXXX或RASA-MMdd_XXXXXX)
     * @return
     */
    @Delete(url = "/task/${taskid}")
    ForestResponse<UniPushResponseResult<String>> stopScheduleTask(@Var("taskid") String taskId);

    /**
     * 【任务】查询定时任务
     * 该接口支持在推送完定时任务之后，查看定时任务状态，定时任务是否发送成功。
     * 创建定时任务请见接口执行群推
     *
     * @param taskId
     * @return
     */
    @Get(url = "/task/schedule/${taskid}")
    ForestResponse<UniPushResponseResult<Map<String, ScheduleTaskQueryResult>>> queryScheduleTask(@Var("taskid") String taskId);

    /**
     * 【任务】删除定时任务
     * 用来删除还未下发的任务，删除后定时任务不再触发(距离下发还有一分钟的任务，将无法删除，后续可以调用停止任务接口。)
     *
     * @param taskId
     * @return
     */
    @Delete(url = "/task/schedule/${taskid}")
    ForestResponse<UniPushResponseResult<String>> deleteScheduleTask(@Var("taskid") String taskId);
}
