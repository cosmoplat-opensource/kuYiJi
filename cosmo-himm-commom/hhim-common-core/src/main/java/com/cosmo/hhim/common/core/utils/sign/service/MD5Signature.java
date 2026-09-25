/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign.service;

import com.cosmo.hhim.common.core.enums.SignatureStrategyEnum;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.StringUtils;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureService;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureStrategyFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class MD5Signature extends SignatureService {

    @Override
    public ThirdInterfaceTenant createSecret(SignatureStrategyEnum strategy) {
        String random = StringUtils.usingRandom(32);
        ThirdInterfaceTenant tenant = new ThirdInterfaceTenant();
        tenant.setPublicKey(random);
        tenant.setPrivateKey(random);
        return tenant;
    }

    @Override
    public String encryptSign(Map<String, String> params, String privateKey) {
        String signContent = buildSignContent(params);
        return md5Sign(signContent, privateKey);
    }

    @Override
    public boolean verifySign(Map<String, String> params, String publicKey) {
        String sign = params.get("sign");
        String content = buildVerifySignContent(params);
        return sign.equals(DigestUtils.md5DigestAsHex((publicKey + content + publicKey).getBytes(StandardCharsets.UTF_8)));
    }


    private String md5Sign(String signContent, String privateKey) {
        return DigestUtils.md5DigestAsHex((privateKey + signContent + privateKey).getBytes());
    }

    @Override
    public void afterPropertiesSet() {
        SignatureStrategyFactory.register("MD5", this);
    }
}
