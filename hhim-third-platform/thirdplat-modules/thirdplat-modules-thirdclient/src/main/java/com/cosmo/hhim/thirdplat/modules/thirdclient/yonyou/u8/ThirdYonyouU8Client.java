/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientRequest;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdClientEnum;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdHTTPMethodEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.mapping.ThirdClientMethodMapping;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdClientStrategy;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.common.TokenService;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.common.TraceService;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.exception.OpenAPIException;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.model.Record;
import com.cosmo.hhim.thirdplat.modules.thirdclient.yonyou.u8.util.YonYouHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;

/**
 * U8系统对接海云智造三方客户端
 */
@Component
@Slf4j
public class ThirdYonyouU8Client extends ThirdClientAbstract {
    public static final String U8_RES_SUCCESS = "0";
    @Autowired
    private TokenService tokenService;
    @Autowired
    private TraceService traceService;

    /**
     * @return 是否需要与海云智造接口映射
     */
    @Override
    protected boolean isMapping() {
        return true;
    }

    /**
     * 构建本次请求参数,由于每个外部系统的请求参数不一致
     * 这里提供模板方法, 自定义实现请求参数的构建
     * (请求头/地址/方法/编码...)
     *
     * @param clientTenant
     * @param method       真实请求的方法路径
     * @param message
     * @return 封装好的完整请求参数
     */
    @Override
    protected ThirdClientRequest buildRequestParam(ThirdClientTenant clientTenant, String method, ThirdInterfaceMessage message) throws OpenAPIException {
        String toKenId = tokenService.getToKenId(clientTenant);
        String tradeId = traceService.getTradeId(clientTenant);
        String url = createUrl(clientTenant.getAuthInfo(), toKenId, tradeId, method);
        ThirdClientRequest request = new ThirdClientRequest();
        request.setRequestUrl(url);
        request.setRequestType(ThirdHTTPMethodEnum.convertEnum(clientTenant.getRequestType()));
        request.setMediaType(MediaType.APPLICATION_JSON_VALUE);
        request.setCharset(StandardCharsets.UTF_8.name());
        //TODO 转换字段映射关系
        request.setRequestBody(message.getMessageContent().getBizContent());
        return request;
    }

    private String createUrl(JSONObject authInfo, String toKenId, String traceId, String method) {
        String baseURL = authInfo.getString("base_url");
        Assert.notNull(baseURL, "base_url is null, please check the authInfo");
        authInfo.remove("base_url");
        authInfo.put("token", toKenId);
        authInfo.put("tradeid", traceId);
        StringBuilder urlAppend = new StringBuilder();
        urlAppend.append(baseURL)
                .append(method)
                .append("?");
        int index = 0;
        for (String key : authInfo.keySet()) {
            Object value = authInfo.get(key);
            if (value != null) {
                urlAppend.append(index == 0 ? "" : "&")
                        .append(key)
                        .append("=")
                        .append(value);
                index++;
            }
        }
        return urlAppend.toString();
    }

    /**
     * 触发请求
     *
     * @param clientRequest 完整请求参数
     * @return 请求响应体
     */
    @Override
    protected ThirdResponse invokeRequest(ThirdClientRequest clientRequest) {
        log.info("yonyouU8 reqeust param:{}", clientRequest.toString());
        String res = null;
        String requestUrl = clientRequest.getRequestUrl();
        try {
            res = YonYouHttpUtil.post(requestUrl, clientRequest.getRequestBody());
        } catch (Exception e) {
            log.error("Failed to request YONYOU_U8 server--->:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            throw new RuntimeException(e);
        }
        log.info("yonyouU8 repsonse: {}", res);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.warn("thread sleep is interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("thread sleep is interrupted");
        }
        JSONObject realRes;
        String sync = "sync=1";
        if (requestUrl.contains(sync)) {
            realRes = Record.parseObject(res);
        }else {
            JSONObject resultRecord = Record.parseObject(res);
            realRes = Record.parseObject(HttpUtil.get(resultRecord.getString("url")));
        }
        String errcode = realRes.getString("errcode");
        String errmsg = realRes.getString("errmsg");
        if (U8_RES_SUCCESS.equals(errcode)) {
            return ThirdResponse.success(errmsg, realRes.toJSONString(), "");
        }
        return ThirdResponse.error(errmsg, realRes.toJSONString(), "");
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        ThirdClientStrategy.register(ThirdClientEnum.YONYOU_U8, this);
        //这里去新增方法映射
        //        入库单创建
        ThirdClientMethodMapping.setClientMethodMapping("poRefund/uploadReceipts", "v1", ThirdClientEnum.YONYOU_U8, "poRefund/uploadReceipts");
//        入库单创建
        ThirdClientMethodMapping.setClientMethodMapping("EntryRecordCreat", "v1", ThirdClientEnum.YONYOU_U8, "/otherin/add");
//       入库单审核
        ThirdClientMethodMapping.setClientMethodMapping("EntryRecordApproved", "v1", ThirdClientEnum.YONYOU_U8, "/otherin/verify");
//        出库单创建
        ThirdClientMethodMapping.setClientMethodMapping("OutRecordCreat", "v1", ThirdClientEnum.YONYOU_U8, "/otherout/add");
//       出库单审核
        ThirdClientMethodMapping.setClientMethodMapping("OutRecordApproved", "v1", ThirdClientEnum.YONYOU_U8, "/otherout/verify");
//        领料单创建
        ThirdClientMethodMapping.setClientMethodMapping("CallCreat", "v1", ThirdClientEnum.YONYOU_U8, "/materialout/add");
//       领料单审核
        ThirdClientMethodMapping.setClientMethodMapping("CallApproved", "v1", ThirdClientEnum.YONYOU_U8, "/materialout/verify");
//        调拨单创建
        ThirdClientMethodMapping.setClientMethodMapping("TransferCreat", "v1", ThirdClientEnum.YONYOU_U8, "/transvouch/add");
//       调拨单审核
        ThirdClientMethodMapping.setClientMethodMapping("TransferApproved", "v1", ThirdClientEnum.YONYOU_U8, "/transvouch/verify");
//        销售退单审核
        ThirdClientMethodMapping.setClientMethodMapping("returnorder/verify", "v1", ThirdClientEnum.YONYOU_U8, "/returnorder/verify");
//        销售退单新增
        ThirdClientMethodMapping.setClientMethodMapping("returnorder/add", "v1", ThirdClientEnum.YONYOU_U8, "/returnorder/add");
//        销售单审核
        ThirdClientMethodMapping.setClientMethodMapping("saleorder/audit", "v1", ThirdClientEnum.YONYOU_U8, "/saleorder/audit");
//        销售单新增
        ThirdClientMethodMapping.setClientMethodMapping("saleorder/add", "v1", ThirdClientEnum.YONYOU_U8, "/saleorder/add");

    }
}
