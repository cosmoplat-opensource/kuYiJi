/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out;

import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-05-12
 */
@Data
public class GetJsSdkTicketOutDto extends WxResponseResult {

    // js-sdk票据
    private String ticket;

    // 票据有效时间
    private Long expires_in;

}
