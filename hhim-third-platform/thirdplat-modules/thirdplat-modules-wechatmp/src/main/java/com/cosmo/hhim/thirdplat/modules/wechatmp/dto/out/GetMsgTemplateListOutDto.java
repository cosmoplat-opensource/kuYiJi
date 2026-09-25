/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out;

import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Data
public class GetMsgTemplateListOutDto extends WxResponseResult {

    // 微信消息模版列表
    private List<TemplateInfo> template_list;

    @Data
    public static class TemplateInfo {

        // 模板ID
        private String template_id;

        // 模板标题
        private String title;

        // 模板所属行业的一级行业
        private String primary_industry;

        // 模板所属行业的二级行业
        private String deputy_industry;

        // 模板内容
        private String content;

        // 模板示例
        private String example;
    }
}
