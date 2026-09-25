/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thridplat.api.wechatminiapp.domain.out;

import com.cosmo.hhim.thridplat.api.wechatminiapp.response.WxAppResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-10-20
 */
@Data
public class QRCodeInfo extends WxAppResponseResult {

    // 成功时返回的图片 Buffer
    private byte[] buffer;

}
