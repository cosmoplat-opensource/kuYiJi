/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.wechatmp.domain.out;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-08
 */
@Data
public class MsgTemplateInfoResult {
    // 模板ID
    private String templateId;

    // 模板标题
    private String title;

    // 模板所属行业的一级行业
    private String primaryIndustry;

    // 模板所属行业的二级行业
    private String deputyIndustry;

    // 模板内容
    private String content;

    // 模板示例
    private String example;
}
