/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.thirdclient.base.client;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessage;
import com.cosmo.hhim.common.core.third.ThirdInterfaceMessageHeader;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientRequest;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdClientTenant;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdInterfaceEntity;
import com.cosmo.hhim.thirdplat.api.thirdclient.domain.ThirdResponse;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdClientEnum;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdFlushEnum;
import com.cosmo.hhim.thirdplat.api.thirdclient.enums.ThirdStatusEnum;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.flush.ThirdFlushAbstract;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.mapping.ThirdClientMethodMapping;
import com.cosmo.hhim.thirdplat.modules.thirdclient.base.strategy.ThirdFlushStrategy;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.Objects;

@Data
@Slf4j
public abstract class ThirdClientAbstract implements InitializingBean {

    @Autowired
    protected RestTemplate restTemplate;

    public final void call(ThirdInterfaceEntity entity) {
        ThirdClientTenant clientTenant = entity.getClientTenant();
        ThirdClientRequest clientRequest = new ThirdClientRequest();
        ThirdResponse res = new ThirdResponse();
        ThirdFlushAbstract flushService = ThirdFlushStrategy.getThirdClient(ThirdFlushEnum.HYZZ_COMMON_MYSQL.getStrategy());
        try {
            ThirdInterfaceMessageHeader messageHeader = entity.getInterfaceMessage().getMessageHeader();
            String method = isMapping() ? this.mappingMethod(messageHeader.getMethod(), messageHeader.getVersion(), clientTenant.getClientSupport()) : messageHeader.getMethod();
            Assert.notNull(method, "method is null, please check the method mapping");
            clientRequest = this.buildRequestParam(clientTenant, method, entity.getInterfaceMessage());
        } catch (Exception e) {
            log.error("逻辑处理错误,记录日志");
            res.setCode(ThirdResponse.FAIL_STATUS);
            res.setMsg("逻辑处理错误:Error:" + e.getClass().getName() + e.getLocalizedMessage() + e.getStackTrace()[0].toString() + (Objects.isNull(e.getCause()) ? "" : "Caused by:" + e.getCause().toString()));
            entity.setResponse(res);
            flushService.flushDB(ThirdStatusEnum.CODE_ERROR, entity, null);
        }
        Date requestDate = new Date();
        try {
            log.info("clientRequest param:{}", clientRequest.toString());
            res = this.invokeRequest(clientRequest);
            if (res == null) {
                res = new ThirdResponse();
                res.setCode(ThirdResponse.FAIL_STATUS);
                res.setMsg("请求失败:Error:null");
            }
            entity.setResponse(res);
        } catch (Exception e) {
            res = new ThirdResponse();
            res.setCode(ThirdResponse.FAIL_STATUS);
            res.setMsg("请求失败:Error:" + e.getClass().getName() + e.getLocalizedMessage() + e.getStackTrace()[0].toString() + (Objects.isNull(e.getCause()) ? "" : "Caused by:" + e.getCause().toString()));
            entity.setResponse(res);
            flushService.flushDB(ThirdStatusEnum.REQUEST_ERROR, entity, requestDate);
            log.error("请求失败, 记录日志");
            throw new RuntimeException();
        }
        flushService.flushDB(ThirdStatusEnum.REQUEST_SUCCESS, entity, requestDate);
    }

    /**
     * 获取本次请求目标的详细租户信息
     * 海云智造本身会提供一些基础接口, 要与目标系统的接口方法映射
     *
     * @return 映射的具体请求方法路径
     */
    private String mappingMethod(String method, String version, String clientSupport) {
        return ThirdClientMethodMapping.getClientMethod(method, version, ThirdClientEnum.getStrategy(clientSupport));
    }

    /**
     * @return 是否需要与海云智造接口映射
     */
    protected abstract boolean isMapping();

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
    protected abstract ThirdClientRequest buildRequestParam(ThirdClientTenant clientTenant, String method, ThirdInterfaceMessage message) throws Exception;

    /**
     * 触发请求
     *
     * @param clientRequest 完整请求参数
     * @return 请求响应体
     */
    protected abstract ThirdResponse invokeRequest(ThirdClientRequest clientRequest);

}