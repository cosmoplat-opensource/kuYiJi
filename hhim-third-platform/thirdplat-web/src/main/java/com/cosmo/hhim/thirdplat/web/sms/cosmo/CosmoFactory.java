/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.sms.cosmo;

import org.dromara.sms4j.provider.factory.BaseProviderFactory;

/**
 * 卡奥斯短信工厂实现，对应 {@link CosmoSmsImpl}。
 *
 * <p>supplier = {@code cosmo}，Sms4j 在解析 yml 时按 blends.*.supplier 匹配本工厂。
 */
public class CosmoFactory implements BaseProviderFactory<CosmoSmsImpl, CosmoConfig> {

    private static final CosmoFactory INSTANCE = new CosmoFactory();

    public static CosmoFactory instance() {
        return INSTANCE;
    }

    @Override
    public CosmoSmsImpl createSms(CosmoConfig c) {
        return new CosmoSmsImpl(c);
    }

    @Override
    public Class<CosmoConfig> getConfigClass() {
        return CosmoConfig.class;
    }

    @Override
    public String getSupplier() {
        return CosmoConfig.SUPPLIER;
    }
}
