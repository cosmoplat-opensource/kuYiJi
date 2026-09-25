/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-24
 */
@Data
public class Aps {
    /**
     * 通知消息
     */
    private Alert alert;
    /**
     * 推送直接带有透传数据，content-available=1表示静默推送，静默推送时不需要填写其他参数，详细参数填写见示例，苹果建议1小时最多推送3条静默消息
     */
    @JSONField(name = "content-available")
    private Integer contentAvailable;
    /**
     * 通知铃声文件名，无声设置为“com.gexin.ios.silence”
     */
    private String sound;
    /**
     * 在客户端通知栏触发特定的action和button显示
     */
    private String category;

    /**
     * ios的远程通知通过该属性对通知进行分组，仅支持iOS 12.0以上版本
     */
    @JSONField(name = "thread-id")
    private String threadId;
}
