/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.common;

/**
 * 请求参数名定义
 * <p>
 * 参数	            类型	   是否必填	最大长度	    描述	                        示例值
 * app_id	        String	是	    32	    应用ID	        2014072300007148
 * method	        String	是	    128	    接口名称	                        alipay.trade.fastpay.refund.query
 * sign_type	    String	是	    10	    商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2	RSA2
 * sign	            String	是	    344	    商户请求参数的签名串，详见签名	详见示例
 * timestamp	    String	是	    19	    发送请求的时间，格式"yyyy-MM-dd HH:mm:ss"	2014-07-24 03:07:50
 * version	        String	是	    3	    调用的接口版本，固定为：1.0	1.0
 * biz_content	    String	是		        请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递，具体参照各产品快速接入文档
 *
 * @author cosmo-hhim-open Team
 */
public class ThirdParamConstant {
    /**
     * 分配给开发者的应用ID
     */
    public static String APP_ID = "app_id";
    /**
     * 接口名称
     */
    public static String METHOD = "method";
    /**
     * 商户生成签名字符串所使用的签名算法类型，目前支持RSA2和RSA，推荐使用RSA2
     */
    public static String SIGN_TYPE = "sign_type";
    /**
     * 发送请求的时间
     */
    public static String TIMESTAMP = "timestamp";
    /**
     * 调用的接口版本
     */
    public static String VERSION = "version";
    /**
     * 请求参数的集合
     */
    public static String BIZ_CONTENT = "biz_content";

    /**
     * 时间戳格式
     */
    public static String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 调用第三方接口已经成功标记
     */
    public static String THIRD_REDIS_REQUEST_SUCCESS_KEY = "request_already_success:";
    /**
     * 第三方接口详情
     */
    public static String THIRD_REDIS_TENANT_INTERFACE_KEY = "tenant_interface_info:";
    /**
     *
     */
    public static String THIRD_RESPONSE_SUCCESS = "S";

}
