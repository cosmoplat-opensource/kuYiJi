/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.cosmo.hhim.common.core.constant.Constants;
import com.cosmo.hhim.common.core.constant.HttpStatus;
import com.cosmo.hhim.common.core.domain.WmsFeignResult;
import com.cosmo.hhim.common.core.threadlocal.ThreadContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;

/**
 * --发送get请求的api
 * CloseableHttpClient类 ，client实现类
 * HttpClients类 ，client工具类，用于创建客户端对象。
 * CloseableHttpResponse接口，请求的响应对象
 * URIBuilder类 ：url构建类，用于设置get请求的路径变量
 * HttpGet类 ：get请求的发送对象
 * EntityUtils类 实体处理类
 * <p>
 * --发送post 请求使用的api
 * CloseableHttpClient类
 * HttpClientBuilder client构建对象，用于创建客户端对象。
 * LaxRedirectStrategy类，post请求重定向的策略
 * CloseableHttpResponse 请求的响应对象
 * HttpPost post请求的发送对象
 * NameValuePair 类，用于设置参数值
 * UrlEncodedFormEntity：用于设置表单参数给发送对象HttpPost
 *
 * @author cosmo-hhim-open Team
 */
@Slf4j
public class HttpClientUtils {

    /**
     * 无参数的get请求
     */
    public static <T> WmsFeignResult<T> doGetSopElt(String url) {
        return doGetSopElt(url, null);
    }

    /**
     * 带参数Object的get请求
     */
    public static <T> WmsFeignResult<T> doGetSopElt(String url, Object object) {
        JSONObject params = JSONObject.parseObject(StringUtils.getObjectString(object));
        params.put("identifyCode", ThreadContext.get(Constants.TARGET_CUSTOMER));
        Map map = JSONObject.parseObject(JSONObject.toJSONString(params), Map.class);
        return doGetSopElt(url, map);
    }

    /**
     * 带参数Map的get请求
     */
    public static <T> WmsFeignResult<T> doGetSopElt(String url, Map<String, Object> params) {

        //获取httpclient客户端
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resultString = "";
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(url);
            if (null != params) {
                for (String key : params.keySet()) {
                    StringBuffer stringBuffer = new StringBuffer(params.get(key).toString());
                    builder.setParameter(key, stringBuffer.toString());
                }
            }
            HttpGet get = new HttpGet(builder.build());
            for (Map.Entry<String, String> entry : ThreadContext.getThreadMap().entrySet()) {
                get.addHeader(entry.getKey().toString(), StringUtils.getObjectString(entry.getValue()));
            }
            response = httpclient.execute(get);
            if (HttpStatus.SUCCESS == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                resultString = EntityUtils.toString(entity, "utf-8");
            }
        } catch (Exception e) {
            log.error("{}",e);
        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
            if (null != httpclient) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }
        return JSON.parseObject(resultString, WmsFeignResult.class, Feature.OrderedField);
    }


    /**
     * 无参的Post请求
     */
    public static <T> WmsFeignResult<T> doPostSopElt(String url) {
        return doPostSopElt(url, null);
    }

    /**
     * 带参数Object的Post请求
     */
    public static <T> WmsFeignResult<T> doPostSopElt(String url, Object object) {

        if (object instanceof List) {
            return doPostSopEltArray(url,object);
        }
        /**
         * 在4.0及以上httpclient版本中，post需要指定重定向的策略，如果不指定则按默认的重定向策略。
         *
         * 获取httpclient客户端
         */
        CloseableHttpClient httpclient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        String resultString = "";
        CloseableHttpResponse response = null;
        JSONObject params = JSONObject.parseObject(StringUtils.getObjectString(object));
        params.put("identifyCode", ThreadContext.get(Constants.TARGET_CUSTOMER));
        String jsonString = JSON.toJSONString(params);
        StringEntity entityObj = new StringEntity(jsonString, "UTF-8");
        try {
            HttpPost post = new HttpPost(url);
            if (null != params) {
                post.setEntity(entityObj);
            }
            /**
             * HTTP/1.1 403 Forbidden
             *   原因：
             *      有些网站，设置了反爬虫机制
             *   解决的办法：
             *      设置请求头，伪装浏览器
             */
            post.addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            post.addHeader("Content-Type", "application/json; charset=UTF-8");
            for (Map.Entry<String, String> entry : ThreadContext.getThreadMap().entrySet()) {
                post.addHeader(entry.getKey().toString(), StringUtils.getObjectString(entry.getValue()));
            }
            response = httpclient.execute(post);
            if (200 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                resultString = EntityUtils.toString(entity, "utf-8");
            }
        } catch (Exception e) {


        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
            if (null != httpclient) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }

        return JSON.parseObject(resultString, WmsFeignResult.class, Feature.OrderedField);
    }

    /**
     * 带参数Object的Post请求
     */
    public static <T> WmsFeignResult<T> doPostSopEltArray(String url, Object object) {
        /**
         * 在4.0及以上httpclient版本中，post需要指定重定向的策略，如果不指定则按默认的重定向策略。
         *
         * 获取httpclient客户端
         */
        CloseableHttpClient httpclient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        String resultString = "";
        CloseableHttpResponse response = null;
        JSONArray jsonArray = JSONArray.parseArray(StringUtils.getObjectString(object));
        jsonArray.forEach(t -> {
            JSONObject obj = (JSONObject) t;
            obj.put("identifyCode", ThreadContext.get(Constants.TARGET_CUSTOMER));
        });
        String jsonString = JSON.toJSONString(jsonArray);
        StringEntity entityObj = new StringEntity(jsonString, "UTF-8");
        try {
            HttpPost post = new HttpPost(url);
            post.setEntity(entityObj);
            /**
             * HTTP/1.1 403 Forbidden
             *   原因：
             *      有些网站，设置了反爬虫机制
             *   解决的办法：
             *      设置请求头，伪装浏览器
             */
            post.addHeader("user-agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36");
            post.addHeader("Content-Type", "application/json; charset=UTF-8");
            for (Map.Entry<String, String> entry : ThreadContext.getThreadMap().entrySet()) {
                post.addHeader(entry.getKey().toString(), StringUtils.getObjectString(entry.getValue()));
            }
            response = httpclient.execute(post);
            if (200 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                resultString = EntityUtils.toString(entity, "utf-8");
            }
        } catch (Exception e) {


        } finally {
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
            if (null != httpclient) {
                try {
                    httpclient.close();
                } catch (IOException e) {
                    log.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }
        return JSON.parseObject(resultString, WmsFeignResult.class, Feature.OrderedField);
    }

}
