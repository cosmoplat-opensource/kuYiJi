/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.in;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022/10/20
 */
@Data
public class SchemeCodeParam {

    // 跳转到的目标小程序信息(N)
    private JumpWxaInfo jump_wxa;

    // 默认值false。生成的 scheme 码类型，到期失效：true，永久有效：false。注意，永久有效 scheme 和有效时间超过180天的到期失效 scheme 的总数上限为10万个，详见获取 URL scheme，生成 scheme 码前请仔细确认。(N)
    private Boolean is_expire;

    // 到期失效的 scheme 码的失效时间，为 Unix 时间戳。生成的到期失效 scheme 码在该时间前有效。最长有效期为1年。is_expire 为 true 且 expire_type 为 0 时必填(N)
    private Integer expire_time;

    // 默认值0，到期失效的 scheme 码失效类型，失效时间：0，失效间隔天数：1(N)
    private Integer expire_type;

    // 到期失效的 scheme 码的失效间隔天数。生成的到期失效 scheme 码在该间隔时间到达前有效。最长间隔天数为365天。is_expire 为 true 且 expire_type 为 1 时必填(N)
    private Integer expire_interval;

    /**
     * 跳转到的目标小程序信息
     */
    @Data
    public static class JumpWxaInfo {

        // 通过 scheme 码进入的小程序页面路径，必须是已经发布的小程序存在的页面，不可携带 query。path 为空时会跳转小程序主页。(N)
        private String path;

        // 通过 scheme 码进入小程序时的 query，最大1024个字符，只支持数字，大小写英文以及部分特殊字符：`!#$&'()*+,/:;=?@-._~%``(N)
        private String query;

        // 默认值"release"。要打开的小程序版本。正式版为"release"，体验版为"trial"，开发版为"develop"，仅在微信外打开时生效。(N)
        private String env_version;
    }

}
