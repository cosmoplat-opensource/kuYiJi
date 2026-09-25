/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class WxAppResponseResult {

    // 成功或失败code码
    @JsonIgnore
    private Integer errcode;

    // 成功或失败信息
    @JsonIgnore
    private String errmsg;

    @JsonIgnore
    public boolean isSuccess(){
        return null == errcode || errcode == 0;
    }
}
