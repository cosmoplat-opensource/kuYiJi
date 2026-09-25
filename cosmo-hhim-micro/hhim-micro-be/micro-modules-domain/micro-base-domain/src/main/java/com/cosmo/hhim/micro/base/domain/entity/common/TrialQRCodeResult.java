/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.base.domain.entity.common;

import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2023-01-06
 */
@Data
public class TrialQRCodeResult { 

    // 成功时返回的图片 Buffer base64编码 
    private byte[] buffer;

}
