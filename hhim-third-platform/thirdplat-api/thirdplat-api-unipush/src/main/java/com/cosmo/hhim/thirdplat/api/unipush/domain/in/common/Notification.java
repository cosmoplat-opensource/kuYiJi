/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author cosmo-hhim-open Team
 * @description 通知消息内容，仅支持安卓系统，iOS系统不展示个推通知消息，与transmission、revoke三选一，都填写时报错
 * @createTime 2021-09-22
 */
@Data
public class Notification {
    // 通知消息标题，长度 ≤ 50
    @NotBlank(message = "通知消息标题不允许为空")
    private String title;

    // 通知消息内容，长度 ≤ 256
    @NotBlank(message = "通知消息内容不允许为空")
    private String body;

    // 长文本消息内容，通知消息+长文本样式，与big_image二选一，两个都填写时报错，长度 ≤ 512
    @JSONField(name = "big_text")
    private String bigText;

    // 大图的URL地址，通知消息+大图样式， 与big_text二选一，两个都填写时报错，长度 ≤ 1024
    @JSONField(name = "big_image")
    private String bigImage;

    // 通知的图标名称，包含后缀名（需要在客户端开发时嵌入），如“push.png”，长度 ≤ 64
    private String logo;

    // 通知图标URL地址，长度 ≤ 256
    @JSONField(name = "logo_url")
    private String logoUrl;

    // 通知渠道id，长度 ≤ 64
    @JSONField(name = "channel_id")
    private String channelId;

    // 通知渠道名称，长度 ≤ 64
    @JSONField(name = "channel_name")
    private String channelName;

    /**
     * 设置通知渠道重要性（可以控制响铃，震动，浮动，闪灯等等）
     * android8.0以下
     * 0，1，2:无声音，无振动，不浮动
     * 3:有声音，无振动，不浮动
     * 4:有声音，有振动，有浮动
     * android8.0以上
     * 0：无声音，无振动，不显示；
     * 1：无声音，无振动，锁屏不显示，通知栏中被折叠显示，导航栏无logo;
     * 2：无声音，无振动，锁屏和通知栏中都显示，通知不唤醒屏幕;
     * 3：有声音，无振动，锁屏和通知栏中都显示，通知唤醒屏幕;
     * 4：有声音，有振动，亮屏下通知悬浮展示，锁屏通知以默认形式展示且唤醒屏幕;
     */
    @JSONField(name = "channel_level")
    private Integer channelLevel;

    /**
     * 点击通知后续动作，
     * 目前支持以下后续动作，
     * intent：打开应用内特定页面，
     * url：打开网页地址，
     * payload：自定义消息内容启动应用，
     * payload_custom：自定义消息内容不启动应用，
     * startapp：打开应用首页，
     * none：纯通知，无后续动作
     */
    @NotBlank(message = "点击通知后续动作类型不允许为空")
    @JSONField(name = "click_type")
    private String clickType;

    /**
     * 点击通知打开应用特定页面，长度 ≤ 4096;
     * 示例：intent:#Intent;component=你的包名/你要打开的 activity 全路径;S.parm1=value1;S.parm2=value2;end
     * intent生成请参考
     */
    private String intent;

    // 点击通知打开链接，长度 ≤ 1024
    private String url;

    // 点击通知加自定义消息，长度 ≤ 3072
    private String payload;

    // 覆盖任务时会使用到该字段，两条消息的notify_id相同，新的消息会覆盖老的消息，范围：0-2147483647
    @JSONField(name = "notify_id")
    private Integer notifyId;

    /**
     * 自定义铃声，请填写文件名，不包含后缀名(需要在客户端开发时嵌入)，个推通道下发有效
     * 客户端SDK最低要求 v2_14_0_0
     */
    @JSONField(name = "ring_name")
    private String ringName;

    /**
     * 角标, 必须大于0, 个推通道下发有效
     * 此属性目前仅针对华为 EMUI 系统 4 系列及以上版本设备有效
     * 角标数字数据会和之前角标数字进行叠加；
     * 举例：角标数字配置1，应用之前角标数为2，发送此角标消息后，应用角标数显示为3。
     * 客户端SDK最低要求 v2_14_0_0
     */
    @JSONField(name = "badge_add_num")
    private Integer badgeAddNum;
}
