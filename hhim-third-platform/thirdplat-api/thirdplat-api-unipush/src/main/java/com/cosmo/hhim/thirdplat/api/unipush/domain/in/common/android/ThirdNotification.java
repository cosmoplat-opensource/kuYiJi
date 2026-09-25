/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class ThirdNotification {
    /**
     * 第三方厂商通知标题，长度 ≤ 50
     * 通知栏标题（长度建议取最小集）
     * 小米：title长度限制为50字
     * 华为：title长度限制40字
     * 魅族：title长度限制32字
     * OPPO：title长度限制32字
     * VIVO：title长度限制40英文字符
     */
    private String title;
    /**
     * 第三方厂商通知内容，长度 ≤ 256
     * 通知栏内容(长度建议取最小集)
     * 小米：content长度限制128字
     * 华为：content长度小于1024字
     * 魅族：content长度限制100字
     * OPPO：content长度限制200字
     * VIVO：content长度限制100个英文字符
     */
    private String body;
    /**
     * 点击通知后续动作,
     * 目前支持以下后续动作，
     * intent：打开应用内特定页面(厂商都支持)，
     * url：打开网页地址(厂商都支持，华为要求https协议)，
     * startapp：打开应用首页(厂商都支持)
     */
    @JSONField(name = "click_type")
    private String clickType;

    /**
     * 点击通知打开应用特定页面，intent格式必须正确且不能为空，长度 ≤ 4096;【注意：vivo侧厂商限制 ≤ 1024】
     * 示例：intent:#Intent;component=你的包名/你要打开的 activity 全路径;S.parm1=value1;S.parm2=value2;end
     * intent生成请参考
     */
    private String intent;
    /**
     * 点击通知打开链接，长度 ≤ 1024
     */
    private String url;

    /**
     * 消息覆盖使用，两条消息的notify_id相同，新的消息会覆盖老的消息，范围：0-2147483647
     */
    @JSONField(name = "notify_id")
    private String notifyId;
}
