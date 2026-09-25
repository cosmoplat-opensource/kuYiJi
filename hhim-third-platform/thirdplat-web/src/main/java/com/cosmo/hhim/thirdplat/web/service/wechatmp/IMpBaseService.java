/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.service.wechatmp;

import com.cosmo.hhim.thirdplat.common.domain.APIResponse;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author cosmo-hhim-open Team
 * @createTime 2022-06-29
 */
public interface IMpBaseService {

    /**
     * 微信公众号服务器接入验签
     * @param timestamp
     * @param nonce
     * @param signature
     * @return
     */
    APIResponse<Boolean> checkSignature(@RequestParam String timestamp, @RequestParam String nonce, @RequestParam String signature);

}
