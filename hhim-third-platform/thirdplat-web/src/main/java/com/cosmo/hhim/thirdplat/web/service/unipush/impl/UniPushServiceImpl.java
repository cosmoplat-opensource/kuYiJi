/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.unipush.impl;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.AppPushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.CreatePushMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.ListPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.QueryPushDetailMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SingleBatchPushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.SinglePushCidMsg;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Audience;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.Setting;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.AppPushResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.CidPushResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.CreatePushMsgResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.ListPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.PushDetailResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SingleBatchPushCidResult;
import com.cosmo.hhim.thirdplat.api.unipush.domain.out.SinglePushCidResult;
import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import com.cosmo.hhim.thirdplat.modules.unipush.api.UniPushApi;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.BatchSingleCidPushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.ListPushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.in.push.PushParam;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push.PushQueryResult;
import com.cosmo.hhim.thirdplat.modules.unipush.dto.out.push.PushResult;
import com.cosmo.hhim.thirdplat.modules.unipush.response.UniPushResponseResult;
import com.cosmo.hhim.thirdplat.web.annotation.UniPushRequestId;
import com.cosmo.hhim.thirdplat.web.service.unipush.IUniPushService;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.cosmo.hhim.thirdplat.web.utils.ResponseParserUtil.unipushResponseExceptionHandler;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Service
public class UniPushServiceImpl implements IUniPushService {

    @Autowired
    private UniPushApi uniPushApi;

    @UniPushRequestId
    @Override
    public APIResponse<SinglePushCidResult> singlePush(SinglePushCidMsg message) {

        // 组装请求参数
        Audience audience = new Audience();
        audience.setCid(Arrays.asList(message.getCid()));

        PushParam<Audience> pushParam = new PushParam<>();
        pushParam.setRequestId(message.getRequestId());
        pushParam.setAudience(audience);
        pushParam.setPushMessage(message.getPushMessage());
        pushParam.setPushChannel(message.getPushChannel());

        // 发起请求
        ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> response = uniPushApi.singleCidPush(pushParam);
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        SinglePushCidResult singlePushCidResult = new SinglePushCidResult();
        Map<String, Map<String, String>> responseData = response.getResult().getData();
        Iterator<Map.Entry<String, Map<String, String>>> iterator = responseData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Map<String, String>> mapEntry = iterator.next();
            singlePushCidResult.setTaskId(mapEntry.getKey());

            Iterator<Map.Entry<String, String>> iterator1 = mapEntry.getValue().entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, String> entry = iterator1.next();
                singlePushCidResult.setCid(entry.getKey());
                singlePushCidResult.setResult(entry.getValue());
            }
        }
        return APIResponse.success(singlePushCidResult);
    }


    @UniPushRequestId
    @Override
    public APIResponse<SingleBatchPushCidResult> singleBatchPush(SingleBatchPushCidMsg message) {

        // 组装请求参数
        List<PushParam<Audience>> msgList = Lists.newArrayList();

        List<SinglePushCidMsg> pushMsgList = message.getMsgList();
        if (!CollectionUtils.isEmpty(pushMsgList)) {
            for (SinglePushCidMsg msg : pushMsgList) {
                Audience audience = new Audience();
                audience.setCid(Arrays.asList(msg.getCid()));

                PushParam<Audience> param = new PushParam<>();
                param.setRequestId(msg.getRequestId());
                param.setAudience(audience);
                param.setPushMessage(msg.getPushMessage());
                param.setPushChannel(msg.getPushChannel());
                msgList.add(param);
            }
        }

        BatchSingleCidPushParam pushParam = new BatchSingleCidPushParam();
        pushParam.setAsync(message.getAsync());
        pushParam.setMsgList(msgList);

        // 发起请求
        ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> response = uniPushApi.batchSingleCidPush(pushParam);
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        if (!message.getAsync()) {
            SingleBatchPushCidResult singleBatchPushCidResult = new SingleBatchPushCidResult();

            for (Iterator<Map.Entry<String, Map<String, String>>> iterator = response.getResult().getData().entrySet().iterator(); iterator.hasNext(); ) {
                List<CidPushResult> cidPushResults = Lists.newArrayList();
                Map.Entry<String, Map<String, String>> mapEntry = iterator.next();
                for (Iterator<Map.Entry<String, String>> iterator1 = mapEntry.getValue().entrySet().iterator(); iterator1.hasNext(); ) {
                    Map.Entry<String, String> entry = iterator1.next();

                    CidPushResult cidPushResult = new CidPushResult();
                    cidPushResult.setCid(entry.getKey());
                    cidPushResult.setResult(entry.getValue());
                    cidPushResults.add(cidPushResult);
                }
                singleBatchPushCidResult.setTaskId(mapEntry.getKey());
                singleBatchPushCidResult.setCidPushResults(cidPushResults);
            }

            return APIResponse.success(singleBatchPushCidResult);
        } else {
            return APIResponse.success(null);
        }
    }

    @UniPushRequestId
    @Override
    public APIResponse<CreatePushMsgResult> createMessage(CreatePushMsg message) {

        // 组装请求参数
        Setting setting = new Setting();
        setting.setTtl(message.getMessageTtl());

        PushParam<Audience> pushParam = new PushParam<>();
        pushParam.setRequestId(message.getRequestId());
        pushParam.setGroupName(message.getGroupName());
        pushParam.setSettings(setting);
        pushParam.setPushMessage(message.getPushMessage());
        pushParam.setPushChannel(message.getPushChannel());

        // 发起请求
        ForestResponse<UniPushResponseResult<PushResult>> response = uniPushApi.listCreateMsg(pushParam);
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        CreatePushMsgResult createPushMsgResult = new CreatePushMsgResult();
        createPushMsgResult.setTaskId(response.getResult().getData().getTaskId());
        return APIResponse.success(createPushMsgResult);
    }

    @Override
    public APIResponse<ListPushCidResult> listPush(ListPushCidMsg message) {
        // 组装请求参数
        Audience audience = new Audience();
        audience.setCid(message.getCids());

        ListPushParam pushParam = new ListPushParam();
        pushParam.setAsync(message.getAsync());
        pushParam.setTaskid(message.getTaskId());
        pushParam.setAudience(audience);

        // 发起请求
        ForestResponse<UniPushResponseResult<Map<String, Map<String, String>>>> response = uniPushApi.listCidPush(pushParam);
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        if (!message.getAsync()) {
            ListPushCidResult listPushCidResult = new ListPushCidResult();

            for (Iterator<Map.Entry<String, Map<String, String>>> iterator = response.getResult().getData().entrySet().iterator(); iterator.hasNext(); ) {
                List<CidPushResult> cidPushResults = Lists.newArrayList();
                Map.Entry<String, Map<String, String>> mapEntry = iterator.next();
                for (Iterator<Map.Entry<String, String>> iterator1 = mapEntry.getValue().entrySet().iterator(); iterator1.hasNext(); ) {
                    Map.Entry<String, String> entry = iterator1.next();

                    CidPushResult cidPushResult = new CidPushResult();
                    cidPushResult.setCid(entry.getKey());
                    cidPushResult.setResult(entry.getValue());
                    cidPushResults.add(cidPushResult);
                }
                listPushCidResult.setTaskId(mapEntry.getKey());
                listPushCidResult.setCidPushResults(cidPushResults);
            }

            return APIResponse.success(listPushCidResult);
        } else {
            return APIResponse.success(null);
        }

    }

    @UniPushRequestId
    @Override
    public APIResponse<AppPushResult> appPush(AppPushMsg message) {

        // 组装请求参数
        Setting setting = new Setting();
        setting.setTtl(message.getMessageTtl());
        setting.setSpeed(message.getSpeedLimit());
        setting.setScheduleTime(message.getScheduleTime());

        PushParam<String> pushParam = new PushParam<>();
        pushParam.setRequestId(message.getRequestId());
        pushParam.setGroupName(message.getGroupName());
        pushParam.setAudience("all");
        pushParam.setSettings(setting);
        pushParam.setPushMessage(message.getPushMessage());
        pushParam.setPushChannel(message.getPushChannel());

        // 发起请求
        ForestResponse<UniPushResponseResult<PushResult>> response = uniPushApi.appAllPush(pushParam);
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        AppPushResult appPushResult = new AppPushResult();
        appPushResult.setTaskId(response.getResult().getData().getTaskId());
        return APIResponse.success(appPushResult);
    }

    @Override
    public APIResponse<PushDetailResult> queryPushDetail(QueryPushDetailMsg message) {

        // 发起请求
        ForestResponse<UniPushResponseResult<PushQueryResult>> response = uniPushApi.queryPushResult(message.getCid(), message.getTaskId());
        APIResponse errorResponse = unipushResponseExceptionHandler(response);
        if (null != errorResponse) {
            return errorResponse;
        }

        // 解析响应结果
        List<PushDetailResult.PushDetail> pushDetails = Lists.newArrayList();

        List<PushQueryResult.PushDetail> details = response.getResult().getData().getDetail();
        if (!CollectionUtils.isEmpty(details)) {
            for (PushQueryResult.PushDetail detail : details) {
                PushDetailResult.PushDetail pushDetail = new PushDetailResult.PushDetail();
                pushDetail.setTime(detail.getTime());
                pushDetail.setEvent(detail.getEvent());
                pushDetails.add(pushDetail);
            }
        }

        PushDetailResult pushDetailResult = new PushDetailResult();
        pushDetailResult.setPushDetails(pushDetails);
        return APIResponse.success(pushDetailResult);
    }


}
