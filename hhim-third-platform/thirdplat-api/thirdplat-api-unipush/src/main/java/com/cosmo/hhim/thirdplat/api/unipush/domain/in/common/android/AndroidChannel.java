/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description android厂商通道消息
 * @createTime 2021-09-22
 */
@Data
public class AndroidChannel {
    /**
     * android厂商通道推送消息内容
     */
    private Ups ups;
}
