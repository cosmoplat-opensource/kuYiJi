/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.thirdclient.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * 请求第三方接口参数体
 */
@Data
public class ThirdRequestParam implements Serializable {
    /**
     * 应用ID
     */
    @JSONField(name = "app_id")
    private String appId;
    /**
     * 请求ID
     */
    @JSONField(name = "request_id")
    private String requestId;
    /**
     * 方法名
     */
    private String method;
    /**
     * 签名
     */
    private String sign;
    /**
     * 签名类型
     */
    @JSONField(name = "sign_type")
    private String signType;
    /**
     * 方法版本
     */
    private String version;
    /**
     * 编码方式
     */
    private String chartset = "utf-8";
    /**
     * 报文格式
     */
    private String format = "json";
    /**
     * 时间戳
     */
    private String timestamp;
    /**
     * 业务参数
     */
    @JSONField(name = "biz_content")
    private String bizContent;

}
