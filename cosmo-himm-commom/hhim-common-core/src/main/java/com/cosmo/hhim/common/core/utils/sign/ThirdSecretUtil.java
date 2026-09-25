/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign;

import com.cosmo.hhim.common.core.enums.SignatureStrategyEnum;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import com.cosmo.hhim.common.core.utils.IdUtils;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureService;
import com.cosmo.hhim.common.core.utils.sign.base.SignatureStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class ThirdSecretUtil {

    private static final Logger log = LoggerFactory.getLogger(ThirdSecretUtil.class);

    /**
     * 根据签名策略生成appId和公私钥
     *
     * @return
     */
    public static ThirdInterfaceTenant createThirdIdAndSecret(String secretStrategy) {
        SignatureStrategyEnum strategy = SignatureStrategyEnum.getStrategy(secretStrategy);
        if (Objects.isNull(strategy)) {
            return null;
        }
        try {
            SignatureService signature = SignatureStrategyFactory.getSignatureActuator(secretStrategy);
            String appId = "HY" + new SimpleDateFormat("yyMMdd").format(new Date()) + IdUtils.nextId();
            ThirdInterfaceTenant secret = signature.createSecret(strategy);
            secret.setAppId(appId);
            return secret;
        } catch (Exception e) {
            log.error("Failed to create third appId and secret:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return null;
        }
    }

    /**
     * 根据签名策略单独生成公私钥
     *
     * @return
     */
    public static ThirdInterfaceTenant createThirdOnlySecret(String secretStrategy) {
        SignatureStrategyEnum strategy = SignatureStrategyEnum.getStrategy(secretStrategy);
        if (Objects.isNull(strategy)) {
            return null;
        }
        try {
            SignatureService signature = SignatureStrategyFactory.getSignatureActuator(secretStrategy);
            return signature.createSecret(strategy);
        } catch (Exception e) {
            log.error("Failed to create third secret:{}:{}, Error:{}", e.getClass().getName(), e.getLocalizedMessage(), e.getStackTrace()[0].toString(), e);
            return null;
        }
    }
}
