/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.in;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-06-06
 */
@Data
public class CimMsgTemplateParam {
    // 接收者openid（Y）
    @NotBlank(message = "接收者openid不允许为空！")
    private String toUser;

    // 模板ID（Y）
    @NotBlank(message = "消息模板ID不允许为空！")
    private String templateId;

    // 模板跳转链接（海外帐号没有跳转能力）（N）
    private String url;

    // 标题
    private String first;

    // 字段1
    private String keyword1;

    // 字段2
    private String keyword2;

    // 字段3
    private String keyword3;

    // 备注
    private String remark;
}
