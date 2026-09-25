/*
 * Copyright (c) 2026 海尔卡奥斯物联科技有限公司
 * Licensed under the MIT License.
 */
package com.cosmo.hhim.micro.infrastructure.designPattern.factory; 

import com.cosmo.hhim.micro.infrastructure.designPattern.strategy.SubmitStrategy;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cosmo-hhim-open Team
 * @description: 策略工厂
 * @date 2023/3/24 13:38
 */
public class SubmitStrategyFactory {

    public static ConcurrentHashMap<String, SubmitStrategy> map = new ConcurrentHashMap<>(16);

    public static SubmitStrategy getSubmitStrategy(String applicationType) {
        return map.get(applicationType);
    }

    public static void register(String applicationType, SubmitStrategy submitStrategy) {
        map.put(applicationType, submitStrategy);
    }
}
