/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.modules.wechatmp.dto.out;

import com.cosmo.hhim.thirdplat.modules.wechatmp.response.WxResponseResult;
import lombok.Data;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-03-02
 */
@Data
public class SendMsgTemplateOutDto extends WxResponseResult {
    // 消息ID
    private String msgid;
}
