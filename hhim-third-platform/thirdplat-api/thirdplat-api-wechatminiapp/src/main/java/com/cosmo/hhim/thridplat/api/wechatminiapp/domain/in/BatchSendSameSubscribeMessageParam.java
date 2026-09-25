/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023/2/21
 */
@Data
public class BatchSendSameSubscribeMessageParam {

    // 模版ID
    @NotBlank(message = "微信小程序消息模版ID不允许为空！")
    private String templateId;

    // 点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
    private String page;

    // 模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }的object
    @NotNull(message = "微信小程序模版消息内容不允许为空！")
    private Map<String, SendMessageDataItem> data;

    // 跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
    private String miniprogramState;

    // 进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
    private String lang;

    // 接收者（用户）的 openid集合
    @NotNull(message = "接受者openId集合不允许为空！")
    private List<String> openIds;

}
