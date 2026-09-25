/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 厂商下发策略选择
 * @createTime 2021-09-22
 */
@Data
public class Strategy {

    /**
     * 默认所有通道的策略选择1-4
     * 1: 表示该消息在用户在线时推送个推通道，用户离线时推送厂商通道;
     * 2: 表示该消息只通过厂商通道策略下发，不考虑用户是否在线;
     * 3: 表示该消息只通过个推通道下发，不考虑用户是否在线；
     * 4: 表示该消息优先从厂商通道下发，若消息内容在厂商通道代发失败后会从个推通道下发。
     * 其中名称可填写: ios、st、hw、xm、vv、mz、op，如有疑问请点击右侧“技术咨询”了解详情。
     */
    @JSONField(name = "default")
    private Integer defaultValue;

    // ios通道策略1-4，表示含义同上，要推送ios通道，需要在个推开发者中心上传ios证书，建议填写2或4，否则可能会有消息不展示的问题
    private Integer ios;

    // 通道策略1-4，表示含义同上，需要开通st厂商使用该通道推送消息
    private Integer st;

    private Integer hw;

    private Integer xm;

    private Integer vv;

    private Integer mz;

    private Integer op;

}
