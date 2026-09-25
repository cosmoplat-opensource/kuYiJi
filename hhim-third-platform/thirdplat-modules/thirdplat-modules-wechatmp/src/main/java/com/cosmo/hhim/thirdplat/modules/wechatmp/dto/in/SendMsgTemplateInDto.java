/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.in;

import com.cosmo.hhim.thirdplat.api.wechatmp.domain.in.DataItem;
import lombok.Data;

import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Data
public class SendMsgTemplateInDto {

    // 接收者openid（Y）
    private String touser;

    // 模板ID（Y）
    private String template_id;

    // 模板跳转链接（海外帐号没有跳转能力）（N）
    private String url;

    // 跳小程序所需数据，不需跳小程序可不用传该数据（N）
    private String miniprogram;

    // 所需跳转到的小程序appid（该小程序appid必须与发模板消息的公众号是绑定关联关系，暂不支持小游戏）（N）
    private String appid;

    // 所需跳转到小程序的具体页面路径，支持带参数,（示例index?foo=bar），要求该小程序已发布，暂不支持小游戏（N）
    private String pagepath;

    // 模版数据（Y）
    private Map<String, DataItem> data;

}
