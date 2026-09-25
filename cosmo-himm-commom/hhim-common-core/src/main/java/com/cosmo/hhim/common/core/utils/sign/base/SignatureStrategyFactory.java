/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.common.core.utils.sign.base;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 签名策略工厂
 *
 * @author cosmo-hhim-open Team
 */
public class SignatureStrategyFactory {

    private static Map<String, SignatureService> services = new ConcurrentHashMap<>();

    /**
     * 获取签名执行器
     *
     * @param strategy 签名策略(RSA2,MD5等)
     * @return SignatureService 签名执行器
     */
    public static SignatureService getSignatureActuator(String strategy) {
        return services.get(strategy);
    }

    public static void register(String strategy, SignatureService signatureService) {
        services.put(strategy, signatureService);
    }
}