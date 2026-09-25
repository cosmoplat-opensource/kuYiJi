/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.operation.domain;

import lombok.Data;

import java.util.List;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-07
 */
@Data
public class WechatUserInfoResult extends HyzzWechatUser{

    // openId所绑定的账户信息列表
    private List<HyzzWechatUserBind> wechatUserBindList;
}
