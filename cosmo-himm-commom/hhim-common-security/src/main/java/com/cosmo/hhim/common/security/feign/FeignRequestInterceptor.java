/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.security.feign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.cosmo.hhim.common.core.constant.Constants;

import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import com.cosmo.hhim.common.core.utils.CheckObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cosmo.hhim.common.core.constant.CacheConstants;
import com.cosmo.hhim.common.core.utils.ServletUtils;
import com.cosmo.hhim.common.core.utils.StringUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * feign 请求拦截器
 *
 * @author cosmo-hhim-open Team
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignRequestInterceptor.class);

    @Autowired
    private ObjectMapper objectMapper;
    private final String url1= "/operation/api/customer-user";
    private final String url2= "/operation/api/data-customer";
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try{
            //1.RequestContextHolder 拿到刚进来的这个请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes!=null){
                HttpServletRequest request1 = attributes.getRequest();//老请求
                if (request1 != null) {
                    //同步请求头数据，token
                    String token = request1.getHeader(CacheConstants.HEADER);
                    String username = request1.getHeader(CacheConstants.DETAILS_USERNAME);
                    String factoryCode = request1.getHeader(CacheConstants.FACTORY_CODE);
                    //给新请求同步老请求的token
                    requestTemplate.header(CacheConstants.HEADER, token);
                    requestTemplate.header(CacheConstants.DETAILS_USERNAME, username);
                    requestTemplate.header(CacheConstants.FACTORY_CODE, factoryCode);

                }
            }
            //如果是调运营中心是不需要header放数据库的
//            if(url1.equals(requestTemplate.path()) || url2.equals(requestTemplate.path())){
//                return;
//            }
            HttpServletRequest request = ServletUtils.getRequest();
            if (StringUtils.isNotNull(request)) {
                log.info("通过http访问请求");
                //通过http访问请求
                Map<String, String> headers = ServletUtils.getHeaders(request);
                // 传递用户信息请求头，防止丢失
                String userId = headers.get(CacheConstants.DETAILS_USER_ID);
                if (StringUtils.isNotEmpty(userId)) {
                    requestTemplate.header(CacheConstants.DETAILS_USER_ID, userId);
                } else {
                    requestTemplate.header(CacheConstants.DETAILS_USER_ID,(String) ThreadContext.get(CacheConstants.DETAILS_USER_ID));
                }
                String userName = headers.get(CacheConstants.DETAILS_USERNAME);
                if (StringUtils.isNotEmpty(userName)) {
                    requestTemplate.header(CacheConstants.DETAILS_USERNAME, userName);
                }
                String authentication = headers.get(CacheConstants.AUTHORIZATION_HEADER);
                if (StringUtils.isNotEmpty(authentication)) {
                    requestTemplate.header(CacheConstants.AUTHORIZATION_HEADER, authentication);
                }
                String nickname = headers.get(CacheConstants.NICK_NAME);
                if (StringUtils.isNotEmpty(nickname)) {
                    requestTemplate.header(CacheConstants.NICK_NAME, nickname);
                }
                String ds = request.getHeader(Constants.TARGET_DS);
                if (StringUtils.isEmpty(ds)) {
                    ds = (String) ThreadContext.get(Constants.TARGET_DS);
                }
                requestTemplate.header(Constants.TARGET_DS, ds);


                String schema = request.getHeader(Constants.TARGET_SCHEMA);
                if (StringUtils.isEmpty(schema)) {
                    schema = (String) ThreadContext.get(Constants.TARGET_SCHEMA);
                }
                requestTemplate.header(Constants.TARGET_SCHEMA, schema);


                String traceId = request.getHeader(Constants.TRACEID);
                if (StringUtils.isEmpty(traceId)) {
                    traceId = (String) ThreadContext.get(Constants.TRACEID);

                }
                if(traceId==null){
                    log.error("traceId为空{}", request.getRequestURI());
                }
                requestTemplate.header(Constants.TRACEID, traceId);


                String customer =headers.get(Constants.TARGET_CUSTOMER);
                if (StringUtils.isEmpty(customer)) {
                    customer = (String) ThreadContext.get(Constants.TARGET_CUSTOMER);
                }
                requestTemplate.header(Constants.TARGET_CUSTOMER, customer);

                String wxAppId = headers.get(Constants.WXMP_APPID);
                if (StringUtils.isEmpty(wxAppId)) {
                    wxAppId = (String) ThreadContext.get(Constants.WXMP_APPID);
                }
                requestTemplate.header(Constants.WXMP_APPID, wxAppId);

                String applicationSign = headers.get(Constants.APPLICATION_SIGN);
                if (StringUtils.isEmpty(applicationSign)) {
                    applicationSign = (String) ThreadContext.get(Constants.APPLICATION_SIGN);
                }
                requestTemplate.header(Constants.APPLICATION_SIGN, applicationSign);

                String deviceType = headers.get(CacheConstants.DETAILS_TYPE);
                if (StringUtils.isEmpty(deviceType)) {
                    deviceType = (String) ThreadContext.get(CacheConstants.DETAILS_TYPE);
                }
                requestTemplate.header(CacheConstants.DETAILS_TYPE, deviceType);

                String trialPhone = headers.get(CacheConstants.TRIAL_PHONE);
                if (StringUtils.isNotEmpty(trialPhone)) {
                    requestTemplate.header(CacheConstants.TRIAL_PHONE, trialPhone);
                }

                if(CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_NUM_FEIGN))){
                    requestTemplate.header(Constants.PAGE_NUM_FEIGN, ThreadContext.get(Constants.PAGE_NUM_FEIGN).toString());
                    //使用结束后就销毁
                    ThreadContext.remove(Constants.PAGE_NUM_FEIGN);
                }
                if(CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_SIZE_FEIGN))){
                    requestTemplate.header(Constants.PAGE_SIZE_FEIGN, ThreadContext.get(Constants.PAGE_SIZE_FEIGN).toString());
                    ThreadContext.remove(Constants.PAGE_SIZE_FEIGN);
                }
                if(CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.MAIN_ACCOUNT_FLAG))){
                    requestTemplate.header(Constants.MAIN_ACCOUNT_FLAG, ThreadContext.get(Constants.MAIN_ACCOUNT_FLAG).toString());
                }
            } else {
                log.info("从threadlocal取 用于不通过HTTP请求");
                //从threadlocal取 用于不通过HTTP请求
                for (Object key : ThreadContext.getThreadMap().keySet()){
                    requestTemplate.header((String)key,(String)ThreadContext.get(key));
                }
                if(CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_NUM_FEIGN))){
                    //使用结束后就销毁
                    ThreadContext.remove(Constants.PAGE_NUM_FEIGN);
                }
                if(CheckObjectUtils.isNotEmpty(ThreadContext.get(Constants.PAGE_SIZE_FEIGN))){
                    ThreadContext.remove(Constants.PAGE_SIZE_FEIGN);
                }
            }
           log.info("feign拦截器请求头:{}", JSONObject.toJSONString(requestTemplate.headers()));
        }catch (Exception e){
            log.error("feign拦截器异常",e);
        }


    }

    private void buildQuery(JsonNode jsonNode, String path, Map<String, Collection<String>> queries) {
        if (!jsonNode.isContainerNode()) {   // 叶子节点
            if (jsonNode.isNull()) {
                return;
            }
            Collection<String> values = queries.get(path);
            if (null == values) {
                values = new ArrayList<>();
                queries.put(path, values);
            }
            values.add(jsonNode.asText());
            return;
        }
        if (jsonNode.isArray()) {   // 数组节点
            Iterator<JsonNode> it = jsonNode.elements();
            while (it.hasNext()) {
                buildQuery(it.next(), path, queries);
            }
        } else {
            Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                if (org.springframework.util.StringUtils.hasText(path)) {
                    buildQuery(entry.getValue(), path + "." + entry.getKey(), queries);
                } else {  // 根节点
                    buildQuery(entry.getValue(), entry.getKey(), queries);
                }
            }
        }
    }



}
