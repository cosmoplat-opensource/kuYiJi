/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign.base;

import com.cosmo.hhim.common.core.enums.SignatureStrategyEnum;
import com.cosmo.hhim.common.core.third.ThirdInterfaceTenant;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

public abstract class SignatureService extends BaseSignatureService implements InitializingBean {

    public abstract String encryptSign(Map<String, String> params, String privateKey) throws Exception;

    public abstract boolean verifySign(Map<String, String> params, String publicKey);

    public abstract ThirdInterfaceTenant createSecret(SignatureStrategyEnum strategy) throws Exception;
}
