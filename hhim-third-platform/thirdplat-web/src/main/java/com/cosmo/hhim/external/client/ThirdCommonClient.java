/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.external.client;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageHeader;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureService;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureStrategyFactory;
import com.cosmo.hhim.common.redis.service.RedisService;
import com.cosmo.hhim.external.service.ThirdRequestLogService;
import com.cosmo.hhim.thirdplat.api.thirdclient.common.ThirdParamConstant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientRequest;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdRequestParam;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdClientEnum;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdHTTPMethodEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.client.ThirdClientAbstract;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdClientStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ThirdCommonClient extends ThirdClientAbstract {


    @Autowired
    protected ThirdRequestLogService requestLogService;
    @Autowired
    protected RedisService redisService;


    @Override
    protected boolean isMapping() {
        return false;
    }

    @Override
    protected ThirdClientRequest buildRequestParam(ThirdClientTenant clientTenant, String method, ThirdInterfaceMessage message) throws Exception {
        ThirdClientRequest request = new ThirdClientRequest();
        String requestUrl = clientTenant.getRequestUrl();
        request.setRequestUrl(requestUrl);
        request.setRequestType(ThirdHTTPMethodEnum.convertEnum(clientTenant.getRequestType()));
        request.setMediaType(MediaType.APPLICATION_JSON_VALUE);
        ThirdRequestParam param = new ThirdRequestParam();
        //1.解析MQ消息体
        convertRequestParam(message, param);
        //2.根据租户编码从缓存中获取目标请求详细信息
        ThirdInterfaceTenant tenant = getTenantInfoFromCache(message.getMessageHeader().getTenantCode(), param, ThirdClientEnum.THIRD_COMMON_CLIENT);
        //3.根据签名类型选择签名执行器进行签名
        SignatureService signature = SignatureStrategyFactory.getSignatureActuator(tenant.getSignType());
        getSign(signature, param, tenant.getPrivateKey());
        request.setRequestBody(JSONObject.toJSONString(param));
        return request;
    }

    @Override
    protected ThirdResponse invokeRequest(ThirdClientRequest clientRequest) {
        String requestUrl = clientRequest.getRequestUrl();
        ThirdHTTPMethodEnum requestType = clientRequest.getRequestType();
        String requestBody = clientRequest.getRequestBody();
        if (ThirdHTTPMethodEnum.POST.equals(requestType)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf(clientRequest.getMediaType()));
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            String res = restTemplate.postForObject(requestUrl, request, String.class);
            return JSONObject.parseObject(res, ThirdResponse.class);
        } else {
            return restTemplate.getForObject(requestUrl, ThirdResponse.class, requestBody);
        }
    }

    /**
     * 根据MQ消息体转换成请求参数体
     *
     * @param mqBody MQ消息体
     * @param param  请求参数体
     */
    protected void convertRequestParam(ThirdInterfaceMessage mqBody, ThirdRequestParam param) {
        ThirdInterfaceMessageHeader messageHeader = mqBody.getMessageHeader();
        param.setRequestId(messageHeader.getRequestId());
        param.setMethod(messageHeader.getMethod());
        param.setVersion(messageHeader.getVersion());
        param.setBizContent(mqBody.getMessageContent().getBizContent());
    }

    /**
     * 根据租户从缓存中获取要发起的请求参数
     *
     * @param tenantCode 租户编码
     * @param param      请求参数体
     * @param thirdCommonClient
     * @return 租户对应的请求参数体(请求地址, 秘钥等)
     */
    protected ThirdInterfaceTenant getTenantInfoFromCache(String tenantCode, ThirdRequestParam param, ThirdClientEnum thirdCommonClient) {
        String tenantInfoPrefix = CacheConstants.THIRD_INTERFACE_KEY + ThirdParamConstant.THIRD_REDIS_TENANT_INTERFACE_KEY + thirdCommonClient.getStrategy() + ":";
        ThirdInterfaceTenant secretEntity = redisService.getCacheObject(tenantInfoPrefix + tenantCode);
        if (Objects.isNull(secretEntity)) {
            secretEntity = requestLogService.getTenantInfo(tenantCode, thirdCommonClient.getStrategy());
            redisService.setCacheObject(tenantInfoPrefix + tenantCode, secretEntity);
        }
        param.setSignType(secretEntity.getSignType());
        param.setAppId(secretEntity.getAppId());
        return secretEntity;
    }


    /**
     * 获取签名内容
     *
     * @param signature  签名执行器
     * @param param      请求参数体
     * @param privateKey 秘钥
     * @throws Exception 使用签名工具时可能会抛出流异常
     */
    protected void getSign(SignatureService signature, ThirdRequestParam param, String privateKey) throws Exception {
        param.setTimestamp(new SimpleDateFormat(ThirdParamConstant.TIMESTAMP_PATTERN).format(new Date()));
        Map<String, String> params = buildParamsMap(param);
        String sign = signature.encryptSign(params, privateKey);
        param.setSign(sign);
    }

    /**
     * 构建签名参数体
     *
     * @param param 请求参数体
     * @return 签名参数体
     */
    protected Map<String, String> buildParamsMap(ThirdRequestParam param) {
        HashMap<String, String> sortedParams = new HashMap<>(16);
        sortedParams.put(ThirdParamConstant.APP_ID, param.getAppId());
        sortedParams.put(ThirdParamConstant.METHOD, param.getMethod());
        sortedParams.put(ThirdParamConstant.VERSION, param.getVersion());
        sortedParams.put(ThirdParamConstant.SIGN_TYPE, param.getSignType());
        sortedParams.put(ThirdParamConstant.TIMESTAMP, param.getTimestamp());
        sortedParams.put(ThirdParamConstant.BIZ_CONTENT, param.getBizContent());
        return sortedParams;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ThirdClientStrategy.register(ThirdClientEnum.THIRD_COMMON_CLIENT, this);
        //TODO 这里去新增方法映射
//        ThirdClientMethodMapping.setClientMethodMapping("saleOrderAdd", "v1", ThirdClientEnum.YONYOU_U8, "/saleorder/add");
    }
}
