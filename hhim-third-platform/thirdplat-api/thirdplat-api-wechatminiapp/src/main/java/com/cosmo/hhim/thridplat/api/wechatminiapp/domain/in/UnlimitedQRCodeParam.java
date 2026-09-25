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
public class UnlimitedQRCodeParam {

    // 最大32个可见字符，只支持数字，大小写英文以及部分特殊字符：!#$&'()*+,/:;=?@-._~，其它字符请自行编码为合法字符（因不支持%，中文无法使用 urlencode 处理，请使用其他编码方式）(Y)
    private String scene;

    // 默认是主页，页面 page，例如 pages/index/index，根路径前不要填加 /，不能携带参数（参数请放在 scene 字段里），如果不填写这个字段，默认跳主页面。(N)
    private String page;

    // 默认是true，检查page 是否存在，为 true 时 page 必须是已经发布的小程序存在的页面（否则报错）；为 false 时允许小程序未发布或者 page 不存在， 但page 有数量上限（60000个）请勿滥用。(N)
    private Boolean check_path;

    // 要打开的小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"。默认是正式版。(N)
    private String env_version;

    // 默认430，二维码的宽度，单位 px，最小 280px，最大 1280px(N)
    private Integer width;

    // 自动配置线条颜色，如果颜色依然是黑色，则说明不建议配置主色调，默认 false(N)
    private Boolean auto_color;

    // 默认是{"r":0,"g":0,"b":0} 。auto_color 为 false 时生效，使用 rgb 设置颜色 例如 {"r":"xxx","g":"xxx","b":"xxx"} 十进制表示(N)
    private LineColor line_color;

    // 默认是false，是否需要透明底色，为 true 时，生成透明底色的小程序(N)
    private Boolean is_hyaline;

}
