/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.response;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class WxResponseResult {
    // 成功或失败code码
    private Integer errcode;

    // 成功或失败信息
    private String errmsg;

    public boolean isSuccess(){
        return null == errcode || errcode == 0;
    }
}
