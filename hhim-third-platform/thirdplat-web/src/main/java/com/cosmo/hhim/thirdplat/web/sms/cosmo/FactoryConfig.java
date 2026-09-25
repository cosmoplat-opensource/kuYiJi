/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.thirdplat.web.sms.cosmo;

import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.starter.config.SmsBlendsInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 将 {@link CosmoFactory} 注册为 Spring Bean，并强制触发 Sms4j 的 {@link SmsBlendsInitializer} 实例化。
 *
 * <p>Sms4j 的 {@code SupplierConfig}（位于 sms4j-spring-boot-starter）声明了 {@code SmsBlendsInitializer} bean，
 * 但 {@code SupplierConfig} 本身不是 {@code @Configuration} 类，且没有别的 bean 依赖
 * {@code SmsBlendsInitializer}，导致 Spring 默认 lazy-instantiate 它。一旦它不被实例化，
 * 它的构造函数里同步调用的 {@code onApplicationEvent()} 不会执行，
 * 所有 sms.blends.* 配置都不会被注册到 {@code SmsFactory.BLENDS}，
 * 业务代码调 {@code SmsFactory.getSmsBlend()} 永远返回 null。
 *
 * <p>这里通过显式依赖强制它跑起来，注册链路：
 * <pre>
 *   SmsBlendsInitializer.onApplicationEvent()
 *     → ProviderFactoryHolder.registerFactory(cosmo)
 *     → 遍历 sms.blends，按 supplier 匹配 factory
 *     → SmsFactory.createSmsBlend(supplierConfig)
 *     → SmsFactory.register(blend)  // BLENDS.put(configId, blend)
 *     → SmsLoad.starConfig(blend, weight=1)
 * </pre>
 */
@Slf4j
@Configuration
public class FactoryConfig {

    @Bean
    public CosmoFactory cosmoFactory() {
        return CosmoFactory.instance();
    }

    /**
     * 显式引用 {@link SmsBlendsInitializer}，触发其实例化与 blends 注册。
     * <p>注入到本地 holder bean 后，Spring 会把整个依赖链（factoryList → blends → SmsConfig）一并装配好。
     */
    @Bean
    public SmsBlendsInitializer smsBlendInitializerHolder(SmsBlendsInitializer initializer) {
        log.info("[sms-init] SmsBlendsInitializer 已实例化，cosmo Blend 注册链路已激活");
        return initializer;
    }
}
