/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.sms.cosmo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.sms4j.provider.config.BaseConfig;

/**
 * 卡奥斯短信网关配置（网关地址由 yml 配置注入）。
 *
 * <p>supplier 固定为 {@code cosmo}，对应 {@link CosmoFactory#getSupplier()}，
 * Sms4j 在解析 yml 时按 supplier 字段匹配工厂。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CosmoConfig extends BaseConfig {

    public static final String SUPPLIER = "cosmo";

    /**
     * 卡奥斯短信网关地址（由配置注入，示例：https://your-sms-gateway/v1/sms/sendsms）
     */
    private String requestUrl = "please_set_cosmo_sms_url";

    @Override
    public String getSupplier() {
        return SUPPLIER;
    }
}
