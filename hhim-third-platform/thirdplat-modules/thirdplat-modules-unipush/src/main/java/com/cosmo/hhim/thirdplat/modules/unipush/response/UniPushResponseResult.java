/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.unipush.response;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2021-09-22
 */
@Data
public class UniPushResponseResult<T> {
    // 成功或失败code码
    private Integer code;

    // 失败时返回此说明
    private String msg;

    // 响应数据
    private T data;

    public boolean isSuccess() {
        return null != code && code == 0;
    }
}
