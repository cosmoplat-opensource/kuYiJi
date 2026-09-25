/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.api.unipush.domain.in.common;

import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.android.AndroidChannel;
import com.cosmo.hhim.thirdplat.api.unipush.domain.in.common.ios.IosChannel;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @description 厂商通道消息内容
 * @createTime 2021-09-22
 */
@Data
public class PushChannel {

    // ios通道推送消息内容
    private IosChannel ios;

    // android通道推送消息内容
    private AndroidChannel android;
}
